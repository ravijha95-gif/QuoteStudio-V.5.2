package com.example.data.generator

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.model.AspectRatioOption
import com.example.data.model.PostTemplate
import com.example.data.model.TemplateBackgroundType
import com.example.data.model.TypographyFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PostBitmapRenderer {

    suspend fun generateBitmap(
        context: Context,
        quote: String,
        author: String,
        brandHandle: String,
        categoryTag: String,
        template: PostTemplate,
        aspectRatio: AspectRatioOption,
        textSizeScale: Float = 42f,
        textAlign: Paint.Align = Paint.Align.CENTER,
        showTagline: Boolean = true,
        brandPosition: String = "bottom", // "bottom", "top", "none"
        typographyFamily: TypographyFamily = template.defaultFontFamily,
        customPhotoBitmap: Bitmap? = null,
        overlayOpacity: Float = 0.55f
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = aspectRatio.exportWidth
        val height = aspectRatio.exportHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Draw Background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        if (customPhotoBitmap != null) {
            // Scale and center-crop photo to fit aspect ratio
            val srcW = customPhotoBitmap.width.toFloat()
            val srcH = customPhotoBitmap.height.toFloat()
            val scale = maxOf(width / srcW, height / srcH)
            val scaledW = srcW * scale
            val scaledH = srcH * scale
            val left = (width - scaledW) / 2f
            val top = (height - scaledH) / 2f
            canvas.drawBitmap(customPhotoBitmap, null, android.graphics.RectF(left, top, left + scaledW, top + scaledH), bgPaint)

            // Draw Dimming Overlay
            val overlayPaint = Paint().apply {
                color = android.graphics.Color.argb((overlayOpacity * 255).toInt(), 0, 0, 0)
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        } else {
            drawTemplateBackground(canvas, width, height, template, bgPaint)
        }

        // 2. Category Tagline (Top-left or centered)
        val textColorInt = template.textColor.value.toLong().toInt()
        if (showTagline && categoryTag.isNotBlank()) {
            val tagPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColorInt
                alpha = 140
                textSize = 22f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                letterSpacing = 0.15f
            }
            canvas.drawText(categoryTag, 100f, 120f, tagPaint)
        }

        // 3. Brand Handle (Top or Bottom)
        if (brandPosition != "none" && brandHandle.isNotBlank()) {
            val brandPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColorInt
                alpha = 160
                textSize = 24f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                this.textAlign = Paint.Align.CENTER
                letterSpacing = 0.12f
            }
            val brandY = if (brandPosition == "top") 120f else (height - 90f)
            canvas.drawText(brandHandle, width / 2f, brandY, brandPaint)
        }

        // 4. Main Quote Typography
        val targetTypeface = when (typographyFamily) {
            TypographyFamily.SERIF -> Typeface.create(Typeface.SERIF, if (template.defaultFontWeight.weight >= 700) Typeface.BOLD else Typeface.NORMAL)
            TypographyFamily.SANS_SERIF -> Typeface.create(Typeface.SANS_SERIF, if (template.defaultFontWeight.weight >= 700) Typeface.BOLD else Typeface.NORMAL)
            TypographyFamily.MONOSPACE -> Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            TypographyFamily.CURSIVE -> Typeface.create("cursive", Typeface.NORMAL)
        }

        val textScaleFactor = width / 440f
        val calculatedTextSize = textSizeScale * textScaleFactor * 0.55f

        val quotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColorInt
            textSize = calculatedTextSize
            typeface = targetTypeface
        }

        val quoteText = if (template.isUppercase) quote.uppercase() else quote
        val maxTextWidth = (width * 0.78f).toInt()

        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(quoteText, 0, quoteText.length, quotePaint, maxTextWidth)
                .setAlignment(when (textAlign) {
                    Paint.Align.LEFT -> Layout.Alignment.ALIGN_NORMAL
                    Paint.Align.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
                    else -> Layout.Alignment.ALIGN_CENTER
                })
                .setLineSpacing(0f, 1.15f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                quoteText, quotePaint, maxTextWidth,
                when (textAlign) {
                    Paint.Align.LEFT -> Layout.Alignment.ALIGN_NORMAL
                    Paint.Align.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
                    else -> Layout.Alignment.ALIGN_CENTER
                },
                1.15f, 0f, false
            )
        }

        val quoteHeight = staticLayout.height
        val authorText = if (author.isNotBlank()) "— $author" else ""
        val authorExtra = if (authorText.isNotBlank()) 70f else 0f
        val dividerExtra = if (template.hasDividerLine) 40f else 0f
        val totalBlockHeight = quoteHeight + authorExtra + dividerExtra

        val startY = if (template.defaultVerticalBottom) {
            (height * 0.82f) - totalBlockHeight
        } else {
            (height - totalBlockHeight) / 2f
        }

        val startX = when (textAlign) {
            Paint.Align.LEFT -> width * 0.11f
            Paint.Align.RIGHT -> width * 0.89f - maxTextWidth
            else -> (width - maxTextWidth) / 2f
        }

        canvas.save()
        canvas.translate(startX, startY)
        staticLayout.draw(canvas)
        canvas.restore()

        var currentY = startY + quoteHeight

        // 5. Divider Line
        if (template.hasDividerLine) {
            currentY += 24f
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = template.accentLineColor.value.toLong().toInt()
                strokeWidth = 3f
                style = Paint.Style.STROKE
            }
            val lineLen = 120f
            val lineX1 = when (textAlign) {
                Paint.Align.LEFT -> startX
                Paint.Align.RIGHT -> startX + maxTextWidth - lineLen
                else -> (width - lineLen) / 2f
            }
            canvas.drawLine(lineX1, currentY, lineX1 + lineLen, currentY, linePaint)
            currentY += 20f
        }

        // 6. Author Line
        if (authorText.isNotBlank()) {
            currentY += 40f
            val authorColorInt = template.authorColor.value.toLong().toInt()
            val authorPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = authorColorInt
                textSize = maxOf(26f, calculatedTextSize * 0.40f)
                typeface = Typeface.create(targetTypeface, Typeface.NORMAL)
                this.textAlign = textAlign
            }
            val authorX = when (textAlign) {
                Paint.Align.LEFT -> startX
                Paint.Align.RIGHT -> startX + maxTextWidth
                else -> width / 2f
            }
            canvas.drawText(authorText, authorX, currentY, authorPaint)
        }

        bitmap
    }

    private fun drawTemplateBackground(
        canvas: Canvas,
        width: Int,
        height: Int,
        template: PostTemplate,
        paint: Paint
    ) {
        val w = width.toFloat()
        val h = height.toFloat()
        val c1 = template.primaryBgColor.value.toLong().toInt()
        val c2 = template.secondaryBgColor.value.toLong().toInt()

        when (template.bgType) {
            TemplateBackgroundType.SOLID -> {
                canvas.drawColor(c1)
            }
            TemplateBackgroundType.RADIAL_DARK -> {
                paint.shader = RadialGradient(
                    w * 0.65f, h * 0.35f, h * 0.75f,
                    c1, c2, Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)
                paint.shader = null
            }
            TemplateBackgroundType.RADIAL_GOLD -> {
                paint.shader = RadialGradient(
                    w * 0.5f, h * 0.45f, h * 0.7f,
                    c1, c2, Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)
                paint.shader = null
            }
            TemplateBackgroundType.CINEMA_GRADIENT -> {
                paint.shader = RadialGradient(
                    w * 0.5f, h * 0.22f, h * 0.8f,
                    intArrayOf(c1, 0xFF14110E.toInt(), c2),
                    floatArrayOf(0.0f, 0.45f, 1.0f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)
                paint.shader = null
            }
            TemplateBackgroundType.LINEAR_CREAM -> {
                paint.shader = LinearGradient(
                    0f, 0f, w, h,
                    c1, c2, Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)
                paint.shader = null
            }
            TemplateBackgroundType.SPLIT_CONTRAST -> {
                val splitX = w * 0.52f
                paint.color = c1
                canvas.drawRect(0f, 0f, splitX, h, paint)
                paint.color = c2
                canvas.drawRect(splitX, 0f, w, h, paint)
            }
            TemplateBackgroundType.TEAL_RADIAL -> {
                paint.shader = RadialGradient(
                    w * 0.5f, h * 0.4f, h * 0.7f,
                    c1, c2, Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)
                paint.shader = null
            }
        }
    }

    suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileNamePrefix: String): Uri? =
        withContext(Dispatchers.IO) {
            try {
                val fileName = "${fileNamePrefix}_${System.currentTimeMillis()}.png"
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QuoteStudio")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext null

                resolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
                uri
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    suspend fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? = withContext(Dispatchers.IO) {
        try {
            val cacheFolder = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(cacheFolder, "quote_post_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveBitmapToInternalFile(context: Context, bitmap: Bitmap, fileName: String): String? = withContext(Dispatchers.IO) {
        try {
            val folder = File(context.filesDir, "scheduled_posts").apply { mkdirs() }
            val file = File(folder, "$fileName.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
