package com.example.data.repository

import com.example.data.db.ScheduledPostDao
import com.example.data.model.ScheduledPostEntity
import kotlinx.coroutines.flow.Flow

class ScheduledPostRepository(private val dao: ScheduledPostDao) {

    val allScheduledPosts: Flow<List<ScheduledPostEntity>> = dao.getAllScheduledPosts()
    val activeScheduledPosts: Flow<List<ScheduledPostEntity>> = dao.getActiveScheduledPosts()

    suspend fun schedulePost(post: ScheduledPostEntity): Long {
        return dao.insertPost(post)
    }

    suspend fun getPostById(id: Long): ScheduledPostEntity? {
        return dao.getPostById(id)
    }

    suspend fun reschedulePost(id: Long, newTimestamp: Long) {
        dao.reschedulePost(id, newTimestamp)
    }

    suspend fun markPublished(id: Long, postedAt: Long = System.currentTimeMillis()) {
        dao.updateStatus(id, ScheduledPostEntity.STATUS_PUBLISHED, postedAt = postedAt, failureReason = null)
    }

    suspend fun markFailed(id: Long, reason: String) {
        dao.updateStatus(id, ScheduledPostEntity.STATUS_FAILED, postedAt = null, failureReason = reason)
    }

    suspend fun deletePost(id: Long) {
        dao.deleteById(id)
    }
}
