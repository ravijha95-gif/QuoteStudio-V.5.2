package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatioOption
import com.example.data.model.PostTemplate
import com.example.data.model.TemplateBackgroundType
import com.example.data.model.TypographyFamily

@Composable
fun PostPreviewCanvas(
    quote: String,
    author: String,
    brandHandle: String,
    categoryTag: String,
    template: PostTemplate,
    aspectRatioOption: AspectRatioOption,
    textSizeScale: Float,
    textAlignOption: String,
    showTagline: Boolean,
    brandPosition: String,
    typographyFamily: TypographyFamily,
    customPhotoBitmap: Bitmap?,
    overlayOpacity: Float,
    modifier: Modifier = Modifier
) {
    val targetRatio = aspectRatioOption.aspectRatio
    val animatedRatio by animateFloatAsState(
        targetValue = targetRatio,
        animationSpec = tween(durationMillis = 250),
        label = "aspectRatio"
    )

    // Card Container with soft ambient shadow
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .aspectRatio(animatedRatio)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0x3394A3B8), RoundedCornerShape(16.dp))
                .testTag("post_preview_card")
        ) {
            val cardWidth = maxWidth
            val cardHeight = maxHeight

            // 1. Background Layer
            if (customPhotoBitmap != null) {
                Image(
                    bitmap = customPhotoBitmap.asImageBitmap(),
                    contentDescription = "Custom post background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Overlay Dimmer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = overlayOpacity))
                )
            } else {
                TemplateBackground(template = template)
            }

            // 2. Tagline (Top-left or top-center)
            if (showTagline && categoryTag.isNotBlank()) {
                Text(
                    text = categoryTag,
                    color = template.textColor.copy(alpha = 0.55f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 24.dp, top = 22.dp)
                )
            }

            // 3. Top Brand Handle (if selected)
            if (brandPosition == "top" && brandHandle.isNotBlank()) {
                Text(
                    text = brandHandle,
                    color = template.textColor.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 22.dp)
                )
            }

            // 4. Main Quote & Author Content
            val effectiveAlign: TextAlign = when (textAlignOption) {
                "left" -> TextAlign.Start
                "right" -> TextAlign.End
                "center" -> TextAlign.Center
                else -> template.defaultTextAlign
            }

            val fontFam = typographyFamily.fontFamily
            val fontWt = template.defaultFontWeight

            // Responsive font size calculation based on slider and canvas width
            val calculatedFontSize = (textSizeScale * 0.65f).coerceIn(16f, 44f).sp
            val quoteText = if (template.isUppercase) quote.uppercase() else quote

            val contentAlignment = if (template.defaultVerticalBottom) Alignment.BottomStart else Alignment.Center
            val verticalArrangement = if (template.defaultVerticalBottom) Arrangement.Bottom else Arrangement.Center

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 28.dp,
                        end = 28.dp,
                        top = if (showTagline || brandPosition == "top") 48.dp else 28.dp,
                        bottom = if (brandPosition == "bottom") 48.dp else 28.dp
                    ),
                verticalArrangement = verticalArrangement,
                horizontalAlignment = when (effectiveAlign) {
                    TextAlign.Start -> Alignment.Start
                    TextAlign.End -> Alignment.End
                    else -> Alignment.CenterHorizontally
                }
            ) {
                Text(
                    text = quoteText.ifBlank { "Your quote goes here." },
                    color = template.textColor,
                    fontSize = calculatedFontSize,
                    fontFamily = fontFam,
                    fontWeight = fontWt,
                    textAlign = effectiveAlign,
                    lineHeight = calculatedFontSize * 1.15f,
                    modifier = Modifier.fillMaxWidth()
                )

                // Divider Line
                if (template.hasDividerLine) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(2.dp)
                            .background(template.accentLineColor)
                    )
                }

                // Author
                if (author.isNotBlank()) {
                    Spacer(modifier = Modifier.height(if (template.hasDividerLine) 10.dp else 16.dp))
                    Text(
                        text = "— $author",
                        color = template.authorColor,
                        fontSize = (calculatedFontSize.value * 0.44f).coerceIn(12f, 20f).sp,
                        fontFamily = fontFam,
                        fontWeight = FontWeight.Normal,
                        textAlign = effectiveAlign,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 5. Bottom Brand Handle
            if (brandPosition == "bottom" && brandHandle.isNotBlank()) {
                Text(
                    text = brandHandle,
                    color = template.textColor.copy(alpha = 0.62f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 18.dp)
                )
            }
        }
    }
}

@Composable
private fun TemplateBackground(template: PostTemplate) {
    val c1 = template.primaryBgColor
    val c2 = template.secondaryBgColor

    when (template.bgType) {
        TemplateBackgroundType.SOLID -> {
            Box(modifier = Modifier.fillMaxSize().background(c1))
        }
        TemplateBackgroundType.RADIAL_DARK -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val radius = size.maxDimension * 0.75f
                        val center = Offset(size.width * 0.7f, size.height * 0.3f)
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(c1, c2),
                                center = center,
                                radius = radius
                            )
                        )
                    }
            )
        }
        TemplateBackgroundType.RADIAL_GOLD -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val radius = size.maxDimension * 0.7f
                        val center = Offset(size.width * 0.5f, size.height * 0.45f)
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(c1, c2),
                                center = center,
                                radius = radius
                            )
                        )
                    }
            )
        }
        TemplateBackgroundType.CINEMA_GRADIENT -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val radius = size.maxDimension * 0.8f
                        val center = Offset(size.width * 0.5f, size.height * 0.2f)
                        drawRect(
                            brush = Brush.radialGradient(
                                colorStops = arrayOf(
                                    0.0f to c1,
                                    0.45f to Color(0xFF14110E),
                                    1.0f to c2
                                ),
                                center = center,
                                radius = radius
                            )
                        )
                    }
            )
        }
        TemplateBackgroundType.LINEAR_CREAM -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(c1, c2)
                        )
                    )
            )
        }
        TemplateBackgroundType.SPLIT_CONTRAST -> {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val splitX = size.width * 0.52f
                            drawRect(color = c1, size = androidx.compose.ui.geometry.Size(splitX, size.height))
                            drawRect(
                                color = c2,
                                topLeft = Offset(splitX, 0f),
                                size = androidx.compose.ui.geometry.Size(size.width - splitX, size.height)
                            )
                        }
                )
            }
        }
        TemplateBackgroundType.TEAL_RADIAL -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val radius = size.maxDimension * 0.7f
                        val center = Offset(size.width * 0.5f, size.height * 0.4f)
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(c1, c2),
                                center = center,
                                radius = radius
                            )
                        )
                    }
            )
        }
    }
}
