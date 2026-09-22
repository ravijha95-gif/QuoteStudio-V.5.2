package com.example.data.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.MainActivity
import com.example.R
import com.example.data.api.InstagramApiService
import com.example.data.db.QuoteStudioDatabase
import com.example.data.model.ScheduledPostEntity
import com.example.data.scheduler.PostScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PostScheduleReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "instagram_scheduler_channel"
        const val CHANNEL_NAME = "Instagram Scheduled Posts"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val postId = intent.getLongExtra(PostScheduler.EXTRA_POST_ID, -1L)
        if (postId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = QuoteStudioDatabase.getInstance(context)
                val postDao = db.scheduledPostDao()
                val profileDao = db.instagramProfileDao()

                val post = postDao.getPostById(postId) ?: return@launch
                val profile = profileDao.getProfileDirect()

                ensureNotificationChannel(context)

                // Check if user requested direct Meta Graph API auto-publish
                val canAutoPublishViaApi = post.postMode == ScheduledPostEntity.MODE_AUTO_POST &&
                        profile != null &&
                        profile.metaAccessToken.isNotBlank() &&
                        profile.metaAccountId.isNotBlank()

                if (canAutoPublishViaApi) {
                    // Attempt Meta Graph API Auto-Publish
                    val result = InstagramApiService.publishPhotoPost(
                        accessToken = profile!!.metaAccessToken,
                        accountId = profile.metaAccountId,
                        imageUrl = post.customPhotoUri ?: "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1080",
                        caption = post.caption
                    )

                    if (result.isSuccess) {
                        postDao.updateStatus(postId, ScheduledPostEntity.STATUS_PUBLISHED, postedAt = System.currentTimeMillis())
                        showPublishedNotification(context, post, profile.username)
                    } else {
                        val errorReason = result.exceptionOrNull()?.message ?: "Unknown API Error"
                        postDao.updateStatus(postId, ScheduledPostEntity.STATUS_FAILED, failureReason = errorReason)
                        showReminderNotification(context, post, profile.username, fallbackReason = errorReason)
                    }
                } else {
                    // Direct Instagram App Share / Reminder
                    showReminderNotification(context, post, profile?.username ?: "Instagram")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders and auto-publishing updates for scheduled Instagram posts"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showPublishedNotification(context: Context, post: ScheduledPostEntity, username: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent = PendingIntent.getActivity(context, post.id.toInt(), openAppIntent, flags)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_send)
            .setContentTitle("✨ Published to Instagram (@$username)")
            .setContentText("Quote by ${post.author} is now live on your profile!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("\"${post.quote}\"\n— ${post.author}\n\nSuccessfully auto-published to your Instagram feed.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(post.id.toInt(), notification)
    }

    private fun showReminderNotification(
        context: Context,
        post: ScheduledPostEntity,
        username: String,
        fallbackReason: String? = null
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent to open app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent = PendingIntent.getActivity(context, post.id.toInt(), openAppIntent, flags)

        // Load preview bitmap if available
        val previewBitmap = post.imageLocalPath?.let { path ->
            val file = File(path)
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        }

        val title = if (fallbackReason != null) {
            "📸 Publish to @$username (Manual Step Needed)"
        } else {
            "📸 Time to Post to Instagram (@$username)!"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle(title)
            .setContentText("\"${post.quote.take(50)}...\" by ${post.author}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (previewBitmap != null) {
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(previewBitmap)
                    .setSummaryText(post.quote)
            )
        } else {
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("\"${post.quote}\"\n— ${post.author}\n\n${post.caption.take(120)}...")
            )
        }

        notificationManager.notify(post.id.toInt(), builder.build())
    }
}
