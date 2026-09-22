package com.example.ui.components

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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ScheduledPostEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SchedulePostDialog(
    quote: String,
    author: String,
    brandHandle: String,
    isScheduling: Boolean,
    onDismiss: () -> Unit,
    onConfirmSchedule: (scheduledTimestamp: Long, postMode: String) -> Unit
) {
    var selectedDayOffset by remember { mutableIntStateOf(0) } // 0: Today, 1: Tomorrow, 2: +2 Days, 3: +3 Days
    var selectedHour by remember { mutableIntStateOf(19) } // 7 PM default
    var selectedMinute by remember { mutableIntStateOf(0) }
    var postMode by remember { mutableStateOf(ScheduledPostEntity.MODE_AUTO_POST) }

    // Calculate effective timestamp
    val calendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, selectedDayOffset)
        set(Calendar.HOUR_OF_DAY, selectedHour)
        set(Calendar.MINUTE, selectedMinute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        // If today and time has already passed, adjust to 1 hour from now
        if (selectedDayOffset == 0 && timeInMillis <= System.currentTimeMillis()) {
            timeInMillis = System.currentTimeMillis() + 60 * 60 * 1000
        }
    }

    val formattedDate = SimpleDateFormat("EEE, MMM d • hh:mm a", Locale.getDefault()).format(Date(calendar.timeInMillis))

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F131A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29313D)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("schedule_post_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF9BBCFF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Schedule Post",
                                color = Color(0xFFF5F6F8),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Auto-publish to $brandHandle",
                                color = Color(0xFF98A1AE),
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF98A1AE))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quote Preview Snip
                Surface(
                    color = Color(0xFF151A22),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232A36)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp, 36.dp)
                                .background(Color(0xFF9BBCFF), RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "\"$quote\"",
                                color = Color(0xFFF5F6F8),
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "— $author",
                                color = Color(0xFF98A1AE),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select Day
                Text(
                    text = "Select Day",
                    color = Color(0xFFCAD0DB),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val days = listOf("Today", "Tomorrow", "In 2 Days", "In 3 Days")
                    days.forEachIndexed { index, title ->
                        val isSelected = selectedDayOffset == index
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF1E293B) else Color(0xFF151A22),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF9BBCFF) else Color(0xFF29313D)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedDayOffset = index }
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color(0xFF9BBCFF) else Color(0xFF98A1AE),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select Time (Prime Posting Slots)
                Text(
                    text = "Optimal Instagram Time",
                    color = Color(0xFFCAD0DB),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val timeSlots = listOf(
                    Triple(9, 0, "09:00 AM (Morning Hook)"),
                    Triple(13, 0, "01:00 PM (Lunch Break)"),
                    Triple(19, 0, "07:00 PM (Evening Peak)"),
                    Triple(21, 30, "09:30 PM (Night Reflection)")
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    timeSlots.forEach { (h, m, label) ->
                        val isSelected = selectedHour == h && selectedMinute == m
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF1E293B) else Color(0xFF151A22),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF9BBCFF) else Color(0xFF29313D)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedHour = h
                                    selectedMinute = m
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF9BBCFF) else Color(0xFF6B7280),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color(0xFFF5F6F8) else Color(0xFF98A1AE),
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF9BBCFF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Post Execution Mode
                Text(
                    text = "Publishing Mode",
                    color = Color(0xFFCAD0DB),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isAuto = postMode == ScheduledPostEntity.MODE_AUTO_POST
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isAuto) Color(0xFF1E293B) else Color(0xFF151A22),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isAuto) Color(0xFF9BBCFF) else Color(0xFF29313D)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { postMode = ScheduledPostEntity.MODE_AUTO_POST }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Auto-Publish",
                                color = if (isAuto) Color(0xFF9BBCFF) else Color(0xFFF5F6F8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Posts via Meta API / Direct",
                                color = Color(0xFF98A1AE),
                                fontSize = 10.sp
                            )
                        }
                    }

                    val isReminder = postMode == ScheduledPostEntity.MODE_REMINDER_SHARE
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isReminder) Color(0xFF1E293B) else Color(0xFF151A22),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isReminder) Color(0xFF9BBCFF) else Color(0xFF29313D)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { postMode = ScheduledPostEntity.MODE_REMINDER_SHARE }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Notification Alert",
                                color = if (isReminder) Color(0xFF9BBCFF) else Color(0xFFF5F6F8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Alarm with 1-tap post",
                                color = Color(0xFF98A1AE),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Summary Badge
                Surface(
                    color = Color(0xFF172030),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF9BBCFF), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scheduled for: $formattedDate",
                            color = Color(0xFF9BBCFF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Button
                Button(
                    onClick = {
                        onConfirmSchedule(calendar.timeInMillis, postMode)
                    },
                    enabled = !isScheduling,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9BBCFF),
                        contentColor = Color(0xFF07101B)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_schedule_button")
                ) {
                    if (isScheduling) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF07101B))
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Confirm Schedule Post",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
