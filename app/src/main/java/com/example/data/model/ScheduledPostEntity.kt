package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_posts")
data class ScheduledPostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quote: String,
    val author: String,
    val category: String,
    val templateId: String,
    val aspectRatio: String,
    val caption: String,
    val scheduledTimestamp: Long,
    val status: String = STATUS_SCHEDULED,
    val postMode: String = MODE_AUTO_POST,
    val customPhotoUri: String? = null,
    val imageLocalPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val postedAt: Long? = null,
    val failureReason: String? = null
) {
    companion object {
        const val STATUS_SCHEDULED = "SCHEDULED"
        const val STATUS_PUBLISHED = "PUBLISHED"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_CANCELLED = "CANCELLED"

        const val MODE_AUTO_POST = "INSTAGRAM_AUTO_POST"
        const val MODE_REMINDER_SHARE = "DIRECT_APP_SHARE"
    }

    val isPending: Boolean
        get() = status == STATUS_SCHEDULED && scheduledTimestamp > System.currentTimeMillis()

    val isOverdue: Boolean
        get() = status == STATUS_SCHEDULED && scheduledTimestamp <= System.currentTimeMillis()
}
