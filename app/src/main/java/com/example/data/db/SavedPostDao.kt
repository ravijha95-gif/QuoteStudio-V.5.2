package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SavedPostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPostDao {
    @Query("SELECT * FROM saved_posts ORDER BY timestamp DESC")
    fun getAllSavedPosts(): Flow<List<SavedPostEntity>>

    @Query("SELECT * FROM saved_posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: Long): SavedPostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: SavedPostEntity): Long

    @Delete
    suspend fun deletePost(post: SavedPostEntity)

    @Query("DELETE FROM saved_posts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
