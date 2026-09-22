package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CaptionSection(
    captionText: String,
    punchyCaption: String,
    storyCaption: String,
    selectedStyleIndex: Int,
    hashtags: List<String>,
    isAiLoading: Boolean,
    onStyleChange: (Int) -> Unit,
    onTriggerAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val currentDisplayedCaption = when (selectedStyleIndex) {
        1 -> punchyCaption
        2 -> storyCaption
        else -> captionText
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // 1. Header with AI Trigger
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "INSTAGRAM CAPTION & TAGS",
                color = Color(0xFF98A1AE),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Button(
                onClick = onTriggerAi,
                enabled = !isAiLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFF9BBCFF)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("ai_enhance_caption_button")
            ) {
                if (isAiLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF9BBCFF)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Analyzing...", fontSize = 11.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF9BBCFF)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Refine", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Style Tabs
        val styleTabs = listOf("Reflective", "Punchy Hook", "Story")
        TabRow(
            selectedTabIndex = selectedStyleIndex,
            containerColor = Color(0xFF11151B),
            contentColor = Color(0xFF9BBCFF),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedStyleIndex]),
                    color = Color(0xFF9BBCFF),
                    height = 2.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFF29313D), RoundedCornerShape(10.dp))
        ) {
            styleTabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedStyleIndex == idx,
                    onClick = { onStyleChange(idx) },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedStyleIndex == idx) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedStyleIndex == idx) Color(0xFF9BBCFF) else Color(0xFF98A1AE)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Caption Box
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0B0E13),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29313D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = currentDisplayedCaption.ifBlank { "Click suggest to generate captions." },
                        color = Color(0xFFF5F6F8),
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Instagram Caption", currentDisplayedCaption)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Caption & hashtags copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF181E27),
                            contentColor = Color(0xFFF5F6F8)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29313D)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_caption_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF9BBCFF)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Caption & Tags", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Interactive Hashtags
        if (hashtags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "TARGETED NICHE HASHTAGS (TAP TO COPY ALL)",
                color = Color(0xFF98A1AE),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                hashtags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF151A21),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29313D)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Hashtag", tag)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied $tag", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Text(
                            text = tag,
                            color = Color(0xFF9BBCFF),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
