package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InstagramProfileEntity
import com.example.data.model.ScheduledPostEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InstagramHubTab(
    profile: InstagramProfileEntity?,
    scheduledPosts: List<ScheduledPostEntity>,
    onLinkProfile: (username: String, fullName: String, bio: String, accountType: String, metaToken: String, metaAccountId: String) -> Unit,
    onDisconnectProfile: () -> Unit,
    onToggleAutoPublish: (Boolean) -> Unit,
    onVerifyMetaApi: (token: String, accountId: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onOpenScheduleDialog: () -> Unit,
    onPostNow: (ScheduledPostEntity) -> Unit,
    onDeleteScheduledPost: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var queueFilter by remember { mutableStateOf("ALL") } // "ALL", "SCHEDULED", "PUBLISHED"
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val filteredPosts = remember(scheduledPosts, queueFilter) {
        when (queueFilter) {
            "SCHEDULED" -> scheduledPosts.filter { it.status == ScheduledPostEntity.STATUS_SCHEDULED }
            "PUBLISHED" -> scheduledPosts.filter { it.status == ScheduledPostEntity.STATUS_PUBLISHED }
            else -> scheduledPosts
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Instagram Profile Section
        item {
            if (profile == null || showEditProfileDialog) {
                InstagramProfileLinkCard(
                    existingProfile = profile,
                    isEditing = showEditProfileDialog,
                    onLinkProfile = { u, fn, b, t, tk, id ->
                        onLinkProfile(u, fn, b, t, tk, id)
                        showEditProfileDialog = false
                    },
                    onCancelEdit = { showEditProfileDialog = false },
                    onVerifyMetaApi = onVerifyMetaApi
                )
            } else {
                InstagramProfileActiveCard(
                    profile = profile,
                    onEditClick = { showEditProfileDialog = true },
                    onDisconnect = onDisconnectProfile,
                    onToggleAutoPublish = onToggleAutoPublish,
                    onVerifyMetaApi = onVerifyMetaApi
                )
            }
        }

        // 2. Scheduled Queue Header with CTA
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF9BBCFF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Scheduled Queue",
                            color = Color(0xFFF5F6F8),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${scheduledPosts.count { it.status == ScheduledPostEntity.STATUS_SCHEDULED }} pending posts",
                            color = Color(0xFF98A1AE),
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = onOpenScheduleDialog,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9BBCFF),
                        contentColor = Color(0xFF07101B)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp).testTag("schedule_quote_cta_button")
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Schedule Post", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 3. Queue Filter Chips
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "ALL" to "All (${scheduledPosts.size})",
                    "SCHEDULED" to "Scheduled (${scheduledPosts.count { it.status == ScheduledPostEntity.STATUS_SCHEDULED }})",
                    "PUBLISHED" to "Published (${scheduledPosts.count { it.status == ScheduledPostEntity.STATUS_PUBLISHED }})"
                ).forEach { (key, label) ->
                    val isSelected = queueFilter == key
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF1E293B) else Color(0xFF151A22),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF9BBCFF) else Color(0xFF29313D)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { queueFilter = key }
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color(0xFF9BBCFF) else Color(0xFF98A1AE),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 4. Scheduled Posts List
        if (filteredPosts.isEmpty()) {
            item {
                ScheduledPostsEmptyState(onOpenScheduleDialog)
            }
        } else {
            items(filteredPosts, key = { it.id }) { post ->
                ScheduledPostCard(
                    post = post,
                    onPostNow = { onPostNow(post) },
                    onDelete = { onDeleteScheduledPost(post.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun InstagramProfileActiveCard(
    profile: InstagramProfileEntity,
    onEditClick: () -> Unit,
    onDisconnect: () -> Unit,
    onToggleAutoPublish: (Boolean) -> Unit,
    onVerifyMetaApi: (token: String, accountId: String, onResult: (Boolean, String) -> Unit) -> Unit
) {
    var isTestingApi by remember { mutableStateOf(false) }
    var testApiResult by remember { mutableStateOf<String?>(null) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111620)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29313D)),
        modifier = Modifier.fillMaxWidth().testTag("active_instagram_profile_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Profile Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Instagram Gradient Avatar Ring
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF833AB4),
                                        Color(0xFFFD1D1D),
                                        Color(0xFFFCB045)
                                    )
                                )
                            )
                            .padding(2.5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF0F131A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.username.take(1).uppercase(),
                                color = Color(0xFFF5F6F8),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "@${profile.username}",
                                color = Color(0xFFF5F6F8),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = profile.fullName.ifBlank { profile.accountType },
                            color = Color(0xFF98A1AE),
                            fontSize = 12.sp
                        )
                    }
                }

                Surface(
                    color = Color(0xFF064E3B),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF34D399))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LINKED",
                            color = Color(0xFF6EE7B7),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (profile.bio.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = profile.bio,
                    color = Color(0xFFCAD0DB),
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Metrics Row
            Surface(
                color = Color(0xFF161D28),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%,d", profile.followerCount),
                            color = Color(0xFFF5F6F8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "Followers", color = Color(0xFF98A1AE), fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${profile.postsCount}",
                            color = Color(0xFFF5F6F8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "Posts", color = Color(0xFF98A1AE), fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = profile.accountType,
                            color = Color(0xFF9BBCFF),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "Type", color = Color(0xFF98A1AE), fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Auto-Publishing Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Auto-Posting Engine",
                        color = Color(0xFFF5F6F8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (profile.autoPublishEnabled) "Enabled: Posts automatically at scheduled times" else "Disabled (Manual reminders only)",
                        color = Color(0xFF98A1AE),
                        fontSize = 11.sp
                    )
                }

                Switch(
                    checked = profile.autoPublishEnabled,
                    onCheckedChange = { onToggleAutoPublish(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF07101B),
                        checkedTrackColor = Color(0xFF9BBCFF),
                        uncheckedThumbColor = Color(0xFF98A1AE),
                        uncheckedTrackColor = Color(0xFF1E293B)
                    )
                )
            }

            // Meta Graph API status if provided
            if (profile.metaAccessToken.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF162234),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF23354E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Public, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Meta Graph API: Active", color = Color(0xFF9BBCFF), fontSize = 11.sp)
                        }

                        TextButton(
                            onClick = {
                                isTestingApi = true
                                testApiResult = null
                                onVerifyMetaApi(profile.metaAccessToken, profile.metaAccountId) { success, msg ->
                                    isTestingApi = false
                                    testApiResult = msg
                                }
                            },
                            enabled = !isTestingApi
                        ) {
                            if (isTestingApi) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = Color(0xFF9BBCFF))
                            } else {
                                Text("Test Connection", fontSize = 10.sp, color = Color(0xFF9BBCFF))
                            }
                        }
                    }
                }
            }

            testApiResult?.let { msg ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = msg, color = Color(0xFF34D399), fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF232A36), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF9BBCFF))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Profile & API", fontSize = 11.sp, color = Color(0xFF9BBCFF))
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(onClick = onDisconnect) {
                    Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFF87171))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Disconnect", fontSize = 11.sp, color = Color(0xFFF87171))
                }
            }
        }
    }
}

@Composable
fun InstagramProfileLinkCard(
    existingProfile: InstagramProfileEntity?,
    isEditing: Boolean,
    onLinkProfile: (username: String, fullName: String, bio: String, accountType: String, metaToken: String, metaAccountId: String) -> Unit,
    onCancelEdit: () -> Unit,
    onVerifyMetaApi: (token: String, accountId: String, onResult: (Boolean, String) -> Unit) -> Unit
) {
    var username by remember { mutableStateOf(existingProfile?.username ?: "ravijha95") }
    var fullName by remember { mutableStateOf(existingProfile?.fullName ?: "Ravi Jha") }
    var bio by remember { mutableStateOf(existingProfile?.bio ?: "Curating timeless wisdom & stoic quotes") }
    var accountType by remember { mutableStateOf(existingProfile?.accountType ?: "CREATOR") }
    var metaToken by remember { mutableStateOf(existingProfile?.metaAccessToken ?: "") }
    var metaAccountId by remember { mutableStateOf(existingProfile?.metaAccountId ?: "") }
    var showMetaApiSection by remember { mutableStateOf(existingProfile?.metaAccessToken?.isNotBlank() == true) }
    var isVerifying by remember { mutableStateOf(false) }
    var verifyFeedback by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111620)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29313D)),
        modifier = Modifier.fillMaxWidth().testTag("instagram_link_form_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isEditing) "Configure Instagram Profile" else "Link Instagram Profile",
                        color = Color(0xFFF5F6F8),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Connect to initiate auto-posting & scheduled publishing",
                        color = Color(0xFF98A1AE),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Username input
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Instagram Username / Handle") },
                placeholder = { Text("e.g. noble.thoughts or ravijha95") },
                prefix = { Text("@", color = Color(0xFF9BBCFF), fontWeight = FontWeight.Bold) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF9BBCFF),
                    unfocusedBorderColor = Color(0xFF29313D),
                    focusedContainerColor = Color(0xFF151A22),
                    unfocusedContainerColor = Color(0xFF151A22),
                    focusedTextColor = Color(0xFFF5F6F8),
                    unfocusedTextColor = Color(0xFFF5F6F8)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("input_instagram_username")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Full Name input
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Display Name") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF9BBCFF),
                    unfocusedBorderColor = Color(0xFF29313D),
                    focusedContainerColor = Color(0xFF151A22),
                    unfocusedContainerColor = Color(0xFF151A22),
                    focusedTextColor = Color(0xFFF5F6F8),
                    unfocusedTextColor = Color(0xFFF5F6F8)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bio input
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Profile Bio / Niche") },
                placeholder = { Text("Daily quotes, philosophical ideas...") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF9BBCFF),
                    unfocusedBorderColor = Color(0xFF29313D),
                    focusedContainerColor = Color(0xFF151A22),
                    unfocusedContainerColor = Color(0xFF151A22),
                    focusedTextColor = Color(0xFFF5F6F8),
                    unfocusedTextColor = Color(0xFFF5F6F8)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Account Type
            Text(text = "Account Type", color = Color(0xFFCAD0DB), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("CREATOR", "BUSINESS", "PERSONAL").forEach { type ->
                    val isSelected = accountType == type
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF1E293B) else Color(0xFF151A22),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF9BBCFF) else Color(0xFF29313D)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { accountType = type }
                    ) {
                        Text(
                            text = type,
                            color = if (isSelected) Color(0xFF9BBCFF) else Color(0xFF98A1AE),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expandable Meta Graph API configuration
            Surface(
                color = Color(0xFF161D28),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232F42)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMetaApiSection = !showMetaApiSection }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Direct Meta Graph API (Optional)", color = Color(0xFFF5F6F8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("For hands-off cloud background publishing", color = Color(0xFF98A1AE), fontSize = 10.sp)
                        }
                    }
                    Icon(
                        if (showMetaApiSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF98A1AE)
                    )
                }
            }

            AnimatedVisibility(visible = showMetaApiSection) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    OutlinedTextField(
                        value = metaToken,
                        onValueChange = { metaToken = it },
                        label = { Text("Meta Graph API Access Token") },
                        placeholder = { Text("EAABwz...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF29313D),
                            focusedContainerColor = Color(0xFF151A22),
                            unfocusedContainerColor = Color(0xFF151A22),
                            focusedTextColor = Color(0xFFF5F6F8),
                            unfocusedTextColor = Color(0xFFF5F6F8)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = metaAccountId,
                        onValueChange = { metaAccountId = it },
                        label = { Text("Instagram Business / Creator Account ID") },
                        placeholder = { Text("17841400...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF29313D),
                            focusedContainerColor = Color(0xFF151A22),
                            unfocusedContainerColor = Color(0xFF151A22),
                            focusedTextColor = Color(0xFFF5F6F8),
                            unfocusedTextColor = Color(0xFFF5F6F8)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (metaToken.isNotBlank() && metaAccountId.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = {
                                    isVerifying = true
                                    verifyFeedback = null
                                    onVerifyMetaApi(metaToken, metaAccountId) { success, msg ->
                                        isVerifying = false
                                        verifyFeedback = msg
                                    }
                                },
                                enabled = !isVerifying,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1E293B),
                                    contentColor = Color(0xFF9BBCFF)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                if (isVerifying) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = Color(0xFF9BBCFF))
                                } else {
                                    Text("Verify Credentials", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            verifyFeedback?.let { msg ->
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    color = if (msg.contains("Success", ignoreCase = true)) Color(0xFF34D399) else Color(0xFFF87171),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Connect & Cancel Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isEditing) {
                    Button(
                        onClick = onCancelEdit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color(0xFF98A1AE)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("Cancel", fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onLinkProfile(username, fullName, bio, accountType, metaToken, metaAccountId)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9BBCFF),
                        contentColor = Color(0xFF07101B)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(if (isEditing) 1.5f else 1f).height(44.dp).testTag("confirm_link_profile_button")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isEditing) "Save Profile" else "Link Profile & Enable Auto-Post", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ScheduledPostCard(
    post: ScheduledPostEntity,
    onPostNow: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedDate = remember(post.scheduledTimestamp) {
        SimpleDateFormat("EEE, MMM d • hh:mm a", Locale.getDefault()).format(Date(post.scheduledTimestamp))
    }

    val isPending = post.status == ScheduledPostEntity.STATUS_SCHEDULED
    val isPublished = post.status == ScheduledPostEntity.STATUS_PUBLISHED

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111620)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPending) Color(0xFF29313D) else Color(0xFF1D283A)
        ),
        modifier = Modifier.fillMaxWidth().testTag("scheduled_post_item_${post.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Category tag + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = post.category.uppercase(),
                        color = Color(0xFF9BBCFF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    color = if (isPublished) Color(0xFF064E3B) else Color(0xFF172554),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isPublished) Icons.Default.Check else Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (isPublished) Color(0xFF6EE7B7) else Color(0xFF93C5FD),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPublished) "PUBLISHED" else "SCHEDULED",
                            color = if (isPublished) Color(0xFF6EE7B7) else Color(0xFF93C5FD),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quote & Author
            Text(
                text = "\"${post.quote}\"",
                color = Color(0xFFF5F6F8),
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "— ${post.author}",
                color = Color(0xFF98A1AE),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Time & Mode Bar
            Surface(
                color = Color(0xFF161D28),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF9BBCFF), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedDate,
                            color = Color(0xFFCAD0DB),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = if (post.postMode == ScheduledPostEntity.MODE_AUTO_POST) "Auto-Post" else "Reminder",
                        color = Color(0xFF98A1AE),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                }

                if (isPending) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = onPostNow,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color(0xFF9BBCFF)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp).testTag("post_now_item_${post.id}")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Post to IG Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduledPostsEmptyState(onOpenScheduleDialog: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111620)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232A36)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color(0xFF9BBCFF),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "No Posts Scheduled",
                color = Color(0xFFF5F6F8),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Queue your inspirational quotes to publish automatically at prime engagement hours.",
                color = Color(0xFF98A1AE),
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onOpenScheduleDialog,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9BBCFF),
                    contentColor = Color(0xFF07101B)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Schedule Current Quote", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
