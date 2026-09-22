package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAiService
import com.example.data.db.QuoteStudioDatabase
import com.example.data.generator.PostBitmapRenderer
import com.example.data.generator.QuoteInspirations
import com.example.data.generator.SemanticAnalyzer
import com.example.data.generator.TemplateSuggestion
import com.example.data.model.AspectRatioOption
import com.example.data.model.PostTemplate
import com.example.data.model.QuoteCategory
import com.example.data.model.SavedPostEntity
import com.example.data.model.TemplateGroup
import com.example.data.model.TypographyFamily
import com.example.data.repository.SavedPostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.widget.Toast
import com.example.data.api.InstagramApiService
import com.example.data.model.InstagramProfileEntity
import com.example.data.model.ScheduledPostEntity
import com.example.data.repository.InstagramRepository
import com.example.data.repository.ScheduledPostRepository
import com.example.data.scheduler.PostScheduler

data class QuoteStudioUiState(
    val quoteText: String = "We must be doing something to be happy — action is no less necessary to us than thought.",
    val authorText: String = "William Hazlitt",
    val brandHandle: String = "@noble.thoughts",
    val category: QuoteCategory = QuoteCategory.PHILOSOPHY,
    val selectedTemplate: PostTemplate = PostTemplate.findById("dark"),
    val aspectRatio: AspectRatioOption = AspectRatioOption.PORTRAIT,
    val customPhotoUri: Uri? = null,
    val customPhotoBitmap: Bitmap? = null,
    val overlayOpacity: Float = 0.55f,
    val textSizeScale: Float = 42f,
    val textAlignOption: String = "auto", // "auto", "center", "left", "right"
    val showTagline: Boolean = true,
    val brandPosition: String = "bottom", // "bottom", "top", "none"
    val typographyFamily: TypographyFamily = TypographyFamily.SERIF,
    val captionText: String = "",
    val punchyCaption: String = "",
    val storyCaption: String = "",
    val selectedCaptionStyle: Int = 0, // 0: Reflective, 1: Punchy Hook, 2: Story
    val hashtags: List<String> = emptyList(),
    val isAiLoading: Boolean = false,
    val isExporting: Boolean = false,
    val isSchedulingPost: Boolean = false,
    val isPublishingToIg: Boolean = false,
    val isScheduleDialogVisible: Boolean = false,
    val isAutoPostDialogVisible: Boolean = false,
    val exportSuccessMessage: String? = null,
    val templateFilter: TemplateGroup = TemplateGroup.ALL,
    val suggestions: List<TemplateSuggestion> = emptyList(),
    val isAiAvailable: Boolean = GeminiAiService.isConfigured(),
    val activeTab: Int = 0 // 0: Editor/Stage, 1: Captions, 2: Instagram Hub & Queue, 3: Library
)

class QuoteStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SavedPostRepository
    private val instagramRepo: InstagramRepository
    private val scheduledRepo: ScheduledPostRepository

    val savedPosts: StateFlow<List<SavedPostEntity>>
    val instagramProfile: StateFlow<InstagramProfileEntity?>
    val scheduledPosts: StateFlow<List<ScheduledPostEntity>>

    private val _uiState = MutableStateFlow(QuoteStudioUiState())
    val uiState: StateFlow<QuoteStudioUiState> = _uiState.asStateFlow()

    init {
        val db = QuoteStudioDatabase.getInstance(application)
        repository = SavedPostRepository(db.savedPostDao())
        instagramRepo = InstagramRepository(db.instagramProfileDao())
        scheduledRepo = ScheduledPostRepository(db.scheduledPostDao())

        savedPosts = repository.allPosts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        instagramProfile = instagramRepo.profile.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        scheduledPosts = scheduledRepo.allScheduledPosts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Pre-populate demo linked profile if none exists
        viewModelScope.launch(Dispatchers.IO) {
            if (instagramRepo.getProfileDirect() == null) {
                instagramRepo.linkProfile(
                    username = "noble.thoughts",
                    fullName = "Noble Thoughts",
                    bio = "Timeless philosophy, stoic wisdom & daily reflections",
                    accountType = "CREATOR",
                    autoPublishEnabled = true
                )
            }
        }

        // Initialize with default analysis
        analyzeLocally()
    }

    private fun analyzeLocally() {
        val state = _uiState.value
        val result = SemanticAnalyzer.analyze(
            quote = state.quoteText,
            author = state.authorText,
            brand = state.brandHandle,
            categoryOverride = state.category
        )
        _uiState.update { current ->
            current.copy(
                suggestions = result.recommendedTemplates,
                captionText = result.defaultCaption,
                punchyCaption = result.punchyCaption,
                storyCaption = result.storyCaption,
                hashtags = result.hashtags
            )
        }
    }

    fun onQuoteChange(newQuote: String) {
        _uiState.update { it.copy(quoteText = newQuote) }
        analyzeLocally()
    }

    fun onAuthorChange(newAuthor: String) {
        _uiState.update { it.copy(authorText = newAuthor) }
        analyzeLocally()
    }

    fun onBrandChange(newBrand: String) {
        _uiState.update { it.copy(brandHandle = newBrand) }
        analyzeLocally()
    }

    fun onCategoryChange(category: QuoteCategory) {
        _uiState.update { it.copy(category = category) }
        analyzeLocally()
    }

    fun onTemplateSelect(templateId: String) {
        val tmpl = PostTemplate.findById(templateId)
        _uiState.update { it.copy(selectedTemplate = tmpl) }
    }

    fun onAspectRatioChange(ratio: AspectRatioOption) {
        _uiState.update { it.copy(aspectRatio = ratio) }
    }

    fun onTemplateFilterChange(group: TemplateGroup) {
        _uiState.update { it.copy(templateFilter = group) }
    }

    fun onOverlayOpacityChange(opacity: Float) {
        _uiState.update { it.copy(overlayOpacity = opacity) }
    }

    fun onTextSizeChange(size: Float) {
        _uiState.update { it.copy(textSizeScale = size) }
    }

    fun onTextAlignChange(align: String) {
        _uiState.update { it.copy(textAlignOption = align) }
    }

    fun onToggleTagline(show: Boolean) {
        _uiState.update { it.copy(showTagline = show) }
    }

    fun onBrandPositionChange(pos: String) {
        _uiState.update { it.copy(brandPosition = pos) }
    }

    fun onTypographyChange(family: TypographyFamily) {
        _uiState.update { it.copy(typographyFamily = family) }
    }

    fun onCaptionStyleChange(index: Int) {
        _uiState.update { it.copy(selectedCaptionStyle = index) }
    }

    fun setActiveTab(tab: Int) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun onCustomPhotoSelected(context: Context, uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            customPhotoUri = uri,
                            customPhotoBitmap = bitmap,
                            selectedTemplate = PostTemplate.findById("photo")
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeCustomPhoto() {
        _uiState.update { it.copy(customPhotoUri = null, customPhotoBitmap = null) }
    }

    fun inspireRandomQuote() {
        val sample = QuoteInspirations.getRandom()
        _uiState.update {
            it.copy(
                quoteText = sample.quote,
                authorText = sample.author,
                category = sample.category,
                brandHandle = sample.brand
            )
        }
        analyzeAndSuggest(useAi = false)
    }

    fun analyzeAndSuggest(useAi: Boolean = false) {
        val state = _uiState.value
        if (!useAi) {
            val result = SemanticAnalyzer.analyze(
                quote = state.quoteText,
                author = state.authorText,
                brand = state.brandHandle,
                categoryOverride = null
            )
            _uiState.update { current ->
                current.copy(
                    category = result.suggestedCategory,
                    selectedTemplate = PostTemplate.findById(result.topTemplateId),
                    suggestions = result.recommendedTemplates,
                    captionText = result.defaultCaption,
                    punchyCaption = result.punchyCaption,
                    storyCaption = result.storyCaption,
                    hashtags = result.hashtags
                )
            }
        } else {
            _uiState.update { it.copy(isAiLoading = true) }
            viewModelScope.launch {
                val res = GeminiAiService.analyzeWithAi(
                    quote = state.quoteText,
                    author = state.authorText,
                    brand = state.brandHandle,
                    currentCategory = state.category
                )
                _uiState.update { current ->
                    val result = res.getOrElse {
                        SemanticAnalyzer.analyze(state.quoteText, state.authorText, state.brandHandle, state.category)
                    }
                    current.copy(
                        isAiLoading = false,
                        category = result.suggestedCategory,
                        selectedTemplate = PostTemplate.findById(result.topTemplateId),
                        suggestions = result.recommendedTemplates,
                        captionText = result.defaultCaption,
                        punchyCaption = result.punchyCaption,
                        storyCaption = result.storyCaption,
                        hashtags = result.hashtags
                    )
                }
            }
        }
    }

    fun saveCurrentPost() {
        val state = _uiState.value
        val entity = SavedPostEntity(
            quote = state.quoteText,
            author = state.authorText,
            category = state.category.name,
            brandHandle = state.brandHandle,
            templateId = state.selectedTemplate.id,
            aspectRatio = state.aspectRatio.name,
            caption = when (state.selectedCaptionStyle) {
                1 -> state.punchyCaption
                2 -> state.storyCaption
                else -> state.captionText
            },
            textSize = state.textSizeScale,
            textAlign = state.textAlignOption,
            showTagline = state.showTagline,
            brandPosition = state.brandPosition,
            customPhotoUri = state.customPhotoUri?.toString(),
            overlayOpacity = state.overlayOpacity
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.savePost(entity)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(exportSuccessMessage = "Post saved to Library!") }
            }
        }
    }

    fun loadSavedPost(post: SavedPostEntity) {
        val tmpl = PostTemplate.findById(post.templateId)
        val cat = QuoteCategory.fromName(post.category)
        val ratio = AspectRatioOption.entries.firstOrNull { it.name == post.aspectRatio } ?: AspectRatioOption.PORTRAIT
        val uri = post.customPhotoUri?.let { Uri.parse(it) }

        _uiState.update { current ->
            current.copy(
                quoteText = post.quote,
                authorText = post.author,
                category = cat,
                brandHandle = post.brandHandle,
                selectedTemplate = tmpl,
                aspectRatio = ratio,
                captionText = post.caption,
                textSizeScale = post.textSize,
                textAlignOption = post.textAlign,
                showTagline = post.showTagline,
                brandPosition = post.brandPosition,
                customPhotoUri = uri,
                overlayOpacity = post.overlayOpacity,
                activeTab = 0 // return to preview
            )
        }
    }

    fun deleteSavedPost(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteById(id)
        }
    }

    fun clearExportMessage() {
        _uiState.update { it.copy(exportSuccessMessage = null) }
    }

    fun exportToGallery(context: Context, onResult: (Boolean, String) -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(isExporting = true) }
        viewModelScope.launch {
            val align = when (state.textAlignOption) {
                "left" -> Paint.Align.LEFT
                "right" -> Paint.Align.RIGHT
                "center" -> Paint.Align.CENTER
                else -> if (state.selectedTemplate.defaultTextAlign == androidx.compose.ui.text.style.TextAlign.Start) Paint.Align.LEFT else Paint.Align.CENTER
            }

            val bitmap = PostBitmapRenderer.generateBitmap(
                context = context,
                quote = state.quoteText,
                author = state.authorText,
                brandHandle = state.brandHandle,
                categoryTag = state.category.tag,
                template = state.selectedTemplate,
                aspectRatio = state.aspectRatio,
                textSizeScale = state.textSizeScale,
                textAlign = align,
                showTagline = state.showTagline,
                brandPosition = state.brandPosition,
                typographyFamily = state.typographyFamily,
                customPhotoBitmap = state.customPhotoBitmap,
                overlayOpacity = state.overlayOpacity
            )

            val uri = PostBitmapRenderer.saveBitmapToGallery(context, bitmap, "QuoteStudio_${state.selectedTemplate.id}")
            _uiState.update { it.copy(isExporting = false) }

            if (uri != null) {
                onResult(true, "Image saved to Gallery (Pictures/QuoteStudio)!")
            } else {
                onResult(false, "Failed to save image.")
            }
        }
    }

    fun prepareShare(context: Context, onReady: (Uri?, String) -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(isExporting = true) }
        viewModelScope.launch {
            val align = when (state.textAlignOption) {
                "left" -> Paint.Align.LEFT
                "right" -> Paint.Align.RIGHT
                "center" -> Paint.Align.CENTER
                else -> if (state.selectedTemplate.defaultTextAlign == androidx.compose.ui.text.style.TextAlign.Start) Paint.Align.LEFT else Paint.Align.CENTER
            }

            val bitmap = PostBitmapRenderer.generateBitmap(
                context = context,
                quote = state.quoteText,
                author = state.authorText,
                brandHandle = state.brandHandle,
                categoryTag = state.category.tag,
                template = state.selectedTemplate,
                aspectRatio = state.aspectRatio,
                textSizeScale = state.textSizeScale,
                textAlign = align,
                showTagline = state.showTagline,
                brandPosition = state.brandPosition,
                typographyFamily = state.typographyFamily,
                customPhotoBitmap = state.customPhotoBitmap,
                overlayOpacity = state.overlayOpacity
            )

            val uri = PostBitmapRenderer.saveBitmapToCache(context, bitmap)
            val caption = when (state.selectedCaptionStyle) {
                1 -> state.punchyCaption
                2 -> state.storyCaption
                else -> state.captionText
            }

            _uiState.update { it.copy(isExporting = false) }
            onReady(uri, caption)
        }
    }

    // --- Instagram Profile Actions ---
    fun linkInstagramProfile(
        username: String,
        fullName: String,
        bio: String,
        accountType: String,
        metaToken: String,
        metaAccountId: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            instagramRepo.linkProfile(
                username = username,
                fullName = fullName,
                bio = bio,
                accountType = accountType,
                metaAccessToken = metaToken,
                metaAccountId = metaAccountId,
                autoPublishEnabled = true
            )
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        brandHandle = "@${username.removePrefix("@")}",
                        exportSuccessMessage = "Instagram Profile @${username.removePrefix("@")} Linked!"
                    )
                }
            }
        }
    }

    fun disconnectInstagramProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            instagramRepo.disconnectProfile()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(exportSuccessMessage = "Instagram account disconnected.") }
            }
        }
    }

    fun toggleAutoPublish(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            instagramRepo.toggleAutoPublish(enabled)
        }
    }

    fun verifyMetaApi(token: String, accountId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = instagramRepo.verifyMetaConnection(token, accountId)
            if (res.isSuccess) {
                onResult(true, res.getOrNull() ?: "Verified successfully!")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Verification failed")
            }
        }
    }

    // --- Post Scheduling Actions ---
    fun showScheduleDialog(show: Boolean) {
        _uiState.update { it.copy(isScheduleDialogVisible = show) }
    }

    fun showAutoPostDialog(show: Boolean) {
        _uiState.update { it.copy(isAutoPostDialogVisible = show) }
    }

    fun scheduleCurrentPost(
        context: Context,
        scheduledTimestamp: Long,
        postMode: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val state = _uiState.value
        _uiState.update { it.copy(isSchedulingPost = true) }

        viewModelScope.launch {
            val align = when (state.textAlignOption) {
                "left" -> Paint.Align.LEFT
                "right" -> Paint.Align.RIGHT
                "center" -> Paint.Align.CENTER
                else -> if (state.selectedTemplate.defaultTextAlign == androidx.compose.ui.text.style.TextAlign.Start) Paint.Align.LEFT else Paint.Align.CENTER
            }

            val bitmap = PostBitmapRenderer.generateBitmap(
                context = context,
                quote = state.quoteText,
                author = state.authorText,
                brandHandle = state.brandHandle,
                categoryTag = state.category.tag,
                template = state.selectedTemplate,
                aspectRatio = state.aspectRatio,
                textSizeScale = state.textSizeScale,
                textAlign = align,
                showTagline = state.showTagline,
                brandPosition = state.brandPosition,
                typographyFamily = state.typographyFamily,
                customPhotoBitmap = state.customPhotoBitmap,
                overlayOpacity = state.overlayOpacity
            )

            val localImagePath = PostBitmapRenderer.saveBitmapToInternalFile(
                context = context,
                bitmap = bitmap,
                fileName = "scheduled_${System.currentTimeMillis()}"
            )

            val caption = when (state.selectedCaptionStyle) {
                1 -> state.punchyCaption
                2 -> state.storyCaption
                else -> state.captionText
            }

            val entity = ScheduledPostEntity(
                quote = state.quoteText,
                author = state.authorText,
                category = state.category.name,
                templateId = state.selectedTemplate.id,
                aspectRatio = state.aspectRatio.name,
                caption = caption,
                scheduledTimestamp = scheduledTimestamp,
                status = ScheduledPostEntity.STATUS_SCHEDULED,
                postMode = postMode,
                customPhotoUri = state.customPhotoUri?.toString(),
                imageLocalPath = localImagePath
            )

            val newId = scheduledRepo.schedulePost(entity)
            val scheduledEntity = entity.copy(id = newId)

            // Setup system AlarmManager
            PostScheduler.schedulePostAlarm(context, scheduledEntity)

            _uiState.update {
                it.copy(
                    isSchedulingPost = false,
                    isScheduleDialogVisible = false,
                    exportSuccessMessage = "Post scheduled successfully!"
                )
            }

            onResult(true, "Post scheduled successfully!")
        }
    }

    fun deleteScheduledPost(context: Context, postId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            PostScheduler.cancelPostAlarm(context, postId)
            scheduledRepo.deletePost(postId)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(exportSuccessMessage = "Scheduled post deleted.") }
            }
        }
    }

    fun postNowFromQueue(
        context: Context,
        post: ScheduledPostEntity,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val profile = instagramProfile.value
            val canAutoPublish = post.postMode == ScheduledPostEntity.MODE_AUTO_POST &&
                    profile != null &&
                    profile.metaAccessToken.isNotBlank() &&
                    profile.metaAccountId.isNotBlank()

            if (canAutoPublish) {
                val apiRes = InstagramApiService.publishPhotoPost(
                    accessToken = profile!!.metaAccessToken,
                    accountId = profile.metaAccountId,
                    imageUrl = post.customPhotoUri ?: "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1080",
                    caption = post.caption
                )
                if (apiRes.isSuccess) {
                    scheduledRepo.markPublished(post.id)
                    onResult(true, "Published to Instagram via Meta Graph API!")
                } else {
                    val err = apiRes.exceptionOrNull()?.message ?: "API error"
                    scheduledRepo.markFailed(post.id, err)
                    onResult(false, err)
                }
            } else {
                // Direct Instagram App Share
                prepareShare(context) { uri, caption ->
                    if (uri != null) {
                        shareToInstagramDirect(context, uri, caption)
                        viewModelScope.launch(Dispatchers.IO) {
                            scheduledRepo.markPublished(post.id)
                        }
                        onResult(true, "Shared to Instagram!")
                    } else {
                        onResult(false, "Could not render post image")
                    }
                }
            }
        }
    }

    fun shareToInstagramDirect(context: Context, imageUri: Uri, caption: String) {
        // 1. Copy caption to clipboard so user can paste it immediately into Instagram
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Instagram Caption", caption)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Caption copied to clipboard! Ready to paste.", Toast.LENGTH_SHORT).show()

        // 2. Prepare Intent targeting Instagram
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.instagram.android")
        }

        try {
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(fallbackIntent, "Share Quote to Instagram"))
        }
    }

    fun publishCurrentPostViaMetaApi(onResult: (Boolean, String) -> Unit) {
        val profile = instagramProfile.value
        if (profile == null || profile.metaAccessToken.isBlank() || profile.metaAccountId.isBlank()) {
            onResult(false, "Meta Graph API token or Account ID missing in profile settings.")
            return
        }

        val state = _uiState.value
        _uiState.update { it.copy(isPublishingToIg = true) }

        val caption = when (state.selectedCaptionStyle) {
            1 -> state.punchyCaption
            2 -> state.storyCaption
            else -> state.captionText
        }

        viewModelScope.launch {
            val result = InstagramApiService.publishPhotoPost(
                accessToken = profile.metaAccessToken,
                accountId = profile.metaAccountId,
                imageUrl = state.customPhotoUri?.toString() ?: "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1080",
                caption = caption
            )

            _uiState.update {
                it.copy(
                    isPublishingToIg = false,
                    isAutoPostDialogVisible = false
                )
            }

            if (result.isSuccess) {
                onResult(true, "✨ Successfully published to Instagram (@${profile.username})!")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Publishing failed")
            }
        }
    }
}
