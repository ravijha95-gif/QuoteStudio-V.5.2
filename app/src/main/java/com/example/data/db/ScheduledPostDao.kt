package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ScheduledPostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledPostDao {
    @Query("SELECT * FROM scheduled_posts ORDER BY scheduledTimestamp ASC")
    fun getAllScheduledPosts(): Flow<List<ScheduledPostEntity>>

    @Query("SELECT * FROM scheduled_posts WHERE status = 'SCHEDULED' ORDER BY scheduledTimestamp ASC")
    fun getActiveScheduledPosts(): Flow<List<ScheduledPostEntity>>

    @Query("SELECT * FROM scheduled_posts WHERE status = 'SCHEDULED' AND scheduledTimestamp <= :currentTime")
    suspend fun getDueScheduledPosts(currentTime: Long): List<ScheduledPostEntity>

    @Query("SELECT * FROM scheduled_posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: Long): ScheduledPostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: ScheduledPostEntity): Long

    @Update
    suspend fun updatePost(post: ScheduledPostEntity)

    @Query("UPDATE scheduled_posts SET status = :status, postedAt = :postedAt, failureReason = :failureReason WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, postedAt: Long? = null, failureReason: String? = null)

    @Query("UPDATE scheduled_posts SET scheduledTimestamp = :newTime, status = 'SCHEDULED' WHERE id = :id")
    suspend fun reschedulePost(id: Long, newTime: Long)

    @Delete
    suspend fun deletePost(post: ScheduledPostEntity)

    @Query("DELETE FROM scheduled_posts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
