package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ScheduledPostEntity
import com.example.ui.components.CaptionSection
import com.example.ui.components.EditorControls
import com.example.ui.components.InstagramAutoPostDialog
import com.example.ui.components.InstagramHubTab
import com.example.ui.components.PostPreviewCanvas
import com.example.ui.components.SavedPostsSheet
import com.example.ui.components.SchedulePostDialog
import com.example.ui.components.TemplateSelector
import kotlinx.coroutines.launch

@Composable
fun QuoteStudioScreen(
    viewModel: QuoteStudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedPosts by viewModel.savedPosts.collectAsStateWithLifecycle()
    val instagramProfile by viewModel.instagramProfile.collectAsStateWithLifecycle()
    val scheduledPosts by viewModel.scheduledPosts.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.exportSuccessMessage) {
        uiState.exportSuccessMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearExportMessage()
        }
    }

    // Modal: Schedule Post Dialog
    if (uiState.isScheduleDialogVisible) {
        SchedulePostDialog(
            quote = uiState.quoteText,
            author = uiState.authorText,
            brandHandle = uiState.brandHandle,
            isScheduling = uiState.isSchedulingPost,
            onDismiss = { viewModel.showScheduleDialog(false) },
            onConfirmSchedule = { timestamp, mode ->
                viewModel.scheduleCurrentPost(context, timestamp, mode) { success, msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            }
        )
    }

    // Modal: Instagram Auto-Post Dialog
    if (uiState.isAutoPostDialogVisible) {
        val currentCaption = when (uiState.selectedCaptionStyle) {
            1 -> uiState.punchyCaption
            2 -> uiState.storyCaption
            else -> uiState.captionText
        }
        InstagramAutoPostDialog(
            profile = instagramProfile,
            quote = uiState.quoteText,
            author = uiState.authorText,
            caption = currentCaption,
            isPublishing = uiState.isPublishingToIg,
            onDismiss = { viewModel.showAutoPostDialog(false) },
            onDirectAppShare = { ctx ->
                viewModel.prepareShare(ctx) { uri, caption ->
                    if (uri != null) {
                        viewModel.shareToInstagramDirect(ctx, uri, caption)
                        viewModel.showAutoPostDialog(false)
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("Failed to prepare image for Instagram") }
                    }
                }
            },
            onPublishViaApi = {
                viewModel.publishCurrentPostViaMetaApi { success, msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            },
            onOpenProfileSettings = {
                viewModel.showAutoPostDialog(false)
                viewModel.setActiveTab(2) // Navigate to Instagram & Schedule Hub
            }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090B0F)),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Surface(
                color = Color(0xFF0D1015),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29313D)),
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Quote",
                                color = Color(0xFFF5F6F8),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Studio",
                                color = Color(0xFF9BBCFF),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    color = Color(0xFF9BBCFF),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Visual Instagram Quote Creator",
                            color = Color(0xFF98A1AE),
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.inspireRandomQuote() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color(0xFF9BBCFF)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29313D)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Casino, contentDescription = "Inspire", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Inspire", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color(0xFF0D1015),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29313D)),
                modifier = Modifier.fillMaxWidth().navigationBarsPadding()
            ) {
                Column {
                    // Quick Export & Instagram Action Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. Save button
                        Button(
                            onClick = { viewModel.saveCurrentPost() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF151A21),
                                contentColor = Color(0xFFF5F6F8)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29313D)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(0.9f)
                                .height(44.dp)
                                .testTag("save_post_button")
                        ) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Save", modifier = Modifier.size(15.dp), tint = Color(0xFF9BBCFF))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Save", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // 2. Schedule button
                        Button(
                            onClick = { viewModel.showScheduleDialog(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF161E2E),
                                contentColor = Color(0xFF9BBCFF)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF273852)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(44.dp)
                                .testTag("schedule_post_button")
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = "Schedule", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Schedule", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // 3. Post to IG button
                        Button(
                            onClick = { viewModel.showAutoPostDialog(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF24152F),
                                contentColor = Color(0xFFF472B6)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5B2245)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp)
                                .testTag("post_to_ig_button")
                        ) {
                            Icon(Icons.Default.RocketLaunch, contentDescription = "Post to IG", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Post to IG", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // 4. Export PNG button
                        Button(
                            onClick = {
                                viewModel.exportToGallery(context) { success, msg ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                }
                            },
                            enabled = !uiState.isExporting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9BBCFF),
                                contentColor = Color(0xFF07101B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(44.dp)
                                .testTag("export_png_button")
                        ) {
                            if (uiState.isExporting) {
                                CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp, color = Color(0xFF07101B))
                            } else {
                                Icon(Icons.Default.Download, contentDescription = "Export", modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Export", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Bottom Navigation Tabs
                    NavigationBar(
                        containerColor = Color(0xFF090B0F),
                        contentColor = Color(0xFF9BBCFF),
                        modifier = Modifier.height(58.dp)
                    ) {
                        NavigationBarItem(
                            selected = uiState.activeTab == 0,
                            onClick = { viewModel.setActiveTab(0) },
                            icon = { Icon(Icons.Default.Tune, contentDescription = "Studio") },
                            label = { Text("Studio", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF07101B),
                                selectedTextColor = Color(0xFF9BBCFF),
                                indicatorColor = Color(0xFF9BBCFF),
                                unselectedIconColor = Color(0xFF98A1AE),
                                unselectedTextColor = Color(0xFF98A1AE)
                            )
                        )
                        NavigationBarItem(
                            selected = uiState.activeTab == 1,
                            onClick = { viewModel.setActiveTab(1) },
                            icon = { Icon(Icons.Default.FormatQuote, contentDescription = "Captions") },
                            label = { Text("Captions & AI", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF07101B),
                                selectedTextColor = Color(0xFF9BBCFF),
                                indicatorColor = Color(0xFF9BBCFF),
                                unselectedIconColor = Color(0xFF98A1AE),
                                unselectedTextColor = Color(0xFF98A1AE)
                            )
                        )
                        val pendingCount = scheduledPosts.count { it.status == ScheduledPostEntity.STATUS_SCHEDULED }
                        NavigationBarItem(
                            selected = uiState.activeTab == 2,
                            onClick = { viewModel.setActiveTab(2) },
                            icon = {
                                if (pendingCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = Color(0xFF38BDF8), contentColor = Color(0xFF07101B)) {
                                                Text("$pendingCount")
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = "Schedule")
                                    }
                                } else {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "Schedule")
                                }
                            },
                            label = { Text("Schedule & IG", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF07101B),
                                selectedTextColor = Color(0xFF9BBCFF),
                                indicatorColor = Color(0xFF9BBCFF),
                                unselectedIconColor = Color(0xFF98A1AE),
                                unselectedTextColor = Color(0xFF98A1AE)
                            )
                        )
                        NavigationBarItem(
                            selected = uiState.activeTab == 3,
                            onClick = { viewModel.setActiveTab(3) },
                            icon = { Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = "Library") },
                            label = { Text("Saved (${savedPosts.size})", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF07101B),
                                selectedTextColor = Color(0xFF9BBCFF),
                                indicatorColor = Color(0xFF9BBCFF),
                                unselectedIconColor = Color(0xFF98A1AE),
                                unselectedTextColor = Color(0xFF98A1AE)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.activeTab) {
                0, 1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Live Stage Preview Section (Visible in Studio and Captions tabs)
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFF12161D),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29313D)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "LIVE POST PREVIEW",
                                        color = Color(0xFF98A1AE),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Surface(
                                        color = Color(0xFF1E293B),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${uiState.aspectRatio.label} • ${uiState.selectedTemplate.name}",
                                            color = Color(0xFF9BBCFF),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                PostPreviewCanvas(
                                    quote = uiState.quoteText,
                                    author = uiState.authorText,
                                    brandHandle = uiState.brandHandle,
                                    categoryTag = uiState.category.tag,
                                    template = uiState.selectedTemplate,
                                    aspectRatioOption = uiState.aspectRatio,
                                    textSizeScale = uiState.textSizeScale,
                                    textAlignOption = uiState.textAlignOption,
                                    showTagline = uiState.showTagline,
                                    brandPosition = uiState.brandPosition,
                                    typographyFamily = uiState.typographyFamily,
                                    customPhotoBitmap = uiState.customPhotoBitmap,
                                    overlayOpacity = uiState.overlayOpacity,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        if (uiState.activeTab == 0) {
                            // Studio / Editor Tab
                            EditorControls(
                                quoteText = uiState.quoteText,
                                authorText = uiState.authorText,
                                brandHandle = uiState.brandHandle,
                                category = uiState.category,
                                aspectRatio = uiState.aspectRatio,
                                textSize = uiState.textSizeScale,
                                textAlignOption = uiState.textAlignOption,
                                showTagline = uiState.showTagline,
                                brandPosition = uiState.brandPosition,
                                typographyFamily = uiState.typographyFamily,
                                hasCustomPhoto = uiState.customPhotoBitmap != null,
                                overlayOpacity = uiState.overlayOpacity,
                                onQuoteChange = { viewModel.onQuoteChange(it) },
                                onAuthorChange = { viewModel.onAuthorChange(it) },
                                onBrandChange = { viewModel.onBrandChange(it) },
                                onCategoryChange = { viewModel.onCategoryChange(it) },
                                onAspectRatioChange = { viewModel.onAspectRatioChange(it) },
                                onTextSizeChange = { viewModel.onTextSizeChange(it) },
                                onTextAlignChange = { viewModel.onTextAlignChange(it) },
                                onToggleTagline = { viewModel.onToggleTagline(it) },
                                onBrandPositionChange = { viewModel.onBrandPositionChange(it) },
                                onTypographyChange = { viewModel.onTypographyChange(it) },
                                onCustomPhotoSelected = { viewModel.onCustomPhotoSelected(context, it) },
                                onRemoveCustomPhoto = { viewModel.removeCustomPhoto() },
                                onOverlayOpacityChange = { viewModel.onOverlayOpacityChange(it) },
                                onSuggestClick = { viewModel.analyzeAndSuggest(useAi = false) },
                                onInspireClick = { viewModel.inspireRandomQuote() }
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            TemplateSelector(
                                selectedTemplateId = uiState.selectedTemplate.id,
                                currentFilter = uiState.templateFilter,
                                suggestions = uiState.suggestions,
                                onFilterChange = { viewModel.onTemplateFilterChange(it) },
                                onTemplateSelect = { viewModel.onTemplateSelect(it) }
                            )
                        } else {
                            // Captions & AI Tab
                            CaptionSection(
                                captionText = uiState.captionText,
                                punchyCaption = uiState.punchyCaption,
                                storyCaption = uiState.storyCaption,
                                selectedStyleIndex = uiState.selectedCaptionStyle,
                                hashtags = uiState.hashtags,
                                isAiLoading = uiState.isAiLoading,
                                onStyleChange = { viewModel.onCaptionStyleChange(it) },
                                onTriggerAi = { viewModel.analyzeAndSuggest(useAi = true) }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                2 -> {
                    // Instagram Profile & Scheduled Queue Tab (Self-contained LazyColumn)
                    InstagramHubTab(
                        profile = instagramProfile,
                        scheduledPosts = scheduledPosts,
                        onLinkProfile = { u, fn, b, t, tk, id ->
                            viewModel.linkInstagramProfile(u, fn, b, t, tk, id)
                        },
                        onDisconnectProfile = {
                            viewModel.disconnectInstagramProfile()
                        },
                        onToggleAutoPublish = {
                            viewModel.toggleAutoPublish(it)
                        },
                        onVerifyMetaApi = { token, accountId, onResult ->
                            viewModel.verifyMetaApi(token, accountId, onResult)
                        },
                        onOpenScheduleDialog = {
                            viewModel.showScheduleDialog(true)
                        },
                        onPostNow = { post ->
                            viewModel.postNowFromQueue(context, post) { success, msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        },
                        onDeleteScheduledPost = { id ->
                            viewModel.deleteScheduledPost(context, id)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                3 -> {
                    // Saved Library Tab (Self-contained LazyColumn)
                    SavedPostsSheet(
                        savedPosts = savedPosts,
                        onLoadPost = { viewModel.loadSavedPost(it) },
                        onDeletePost = { viewModel.deleteSavedPost(it) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
