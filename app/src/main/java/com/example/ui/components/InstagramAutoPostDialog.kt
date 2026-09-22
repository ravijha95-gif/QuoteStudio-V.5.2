package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.InstagramProfileEntity

@Composable
fun InstagramAutoPostDialog(
    profile: InstagramProfileEntity?,
    quote: String,
    author: String,
    caption: String,
    isPublishing: Boolean,
    onDismiss: () -> Unit,
    onDirectAppShare: (Context) -> Unit,
    onPublishViaApi: () -> Unit,
    onOpenProfileSettings: () -> Unit
) {
    val context = LocalContext.current
    var selectedMethod by remember {
        mutableStateOf(
            if (profile?.metaAccessToken?.isNotBlank() == true) "API" else "DIRECT"
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F131A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29313D)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("instagram_auto_post_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header with Instagram Gradient
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF833AB4),
                                            Color(0xFFFD1D1D),
                                            Color(0xFFFCB045)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Post to Instagram",
                                color = Color(0xFFF5F6F8),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (profile != null) "@${profile.username} • Linked" else "Instant Sharing",
                                color = if (profile != null) Color(0xFF9BBCFF) else Color(0xFF98A1AE),
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF98A1AE))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quote Preview Card
                Surface(
                    color = Color(0xFF151A22),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232A36)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "\"$quote\"",
                            color = Color(0xFFF5F6F8),
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "— $author",
                            color = Color(0xFF9BBCFF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = caption.take(100) + if (caption.length > 100) "..." else "",
                            color = Color(0xFF98A1AE),
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Publishing Method Selector
                Text(
                    text = "Publishing Pipeline",
                    color = Color(0xFFCAD0DB),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Option 1: Direct Instagram App Launch (Zero Setup)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedMethod == "DIRECT") Color(0xFF1E293B) else Color(0xFF151A22),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (selectedMethod == "DIRECT") Color(0xFF9BBCFF) else Color(0xFF29313D)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedMethod = "DIRECT" }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF273549)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color(0xFF9BBCFF), modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Direct Instagram App Share",
                                color = Color(0xFFF5F6F8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Copies caption & launches Instagram feed composer",
                                color = Color(0xFF98A1AE),
                                fontSize = 11.sp
                            )
                        }
                        if (selectedMethod == "DIRECT") {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF9BBCFF), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Option 2: Meta Graph API Auto-Publish
                val hasApiConfig = profile?.metaAccessToken?.isNotBlank() == true
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedMethod == "API") Color(0xFF1E293B) else Color(0xFF151A22),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (selectedMethod == "API") Color(0xFF9BBCFF) else Color(0xFF29313D)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedMethod = "API" }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF273549)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Public, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Meta Graph API Auto-Publish",
                                    color = Color(0xFFF5F6F8),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (hasApiConfig) {
                                    Surface(
                                        color = Color(0xFF064E3B),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("CONFIGURED", color = Color(0xFF6EE7B7), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Text(
                                text = if (hasApiConfig) "Direct background cloud posting" else "Requires Meta Graph API token & ID",
                                color = Color(0xFF98A1AE),
                                fontSize = 11.sp
                            )
                        }
                        if (selectedMethod == "API") {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF9BBCFF), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                if (selectedMethod == "API" && !hasApiConfig) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().clickable { onOpenProfileSettings() }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Configure Meta API Token in Profile Settings →",
                                color = Color(0xFF9BBCFF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Button
                Button(
                    onClick = {
                        if (selectedMethod == "DIRECT") {
                            onDirectAppShare(context)
                        } else {
                            if (hasApiConfig) {
                                onPublishViaApi()
                            } else {
                                onOpenProfileSettings()
                            }
                        }
                    },
                    enabled = !isPublishing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9BBCFF),
                        contentColor = Color(0xFF07101B)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("initiate_auto_post_button")
                ) {
                    if (isPublishing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF07101B))
                    } else {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedMethod == "DIRECT") "Post Now via Instagram App" else if (hasApiConfig) "Initiate Meta Auto-Post" else "Setup API Token",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
