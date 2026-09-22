package com.example.data.repository

import com.example.data.db.SavedPostDao
import com.example.data.model.SavedPostEntity
import kotlinx.coroutines.flow.Flow

class SavedPostRepository(private val dao: SavedPostDao) {
    val allPosts: Flow<List<SavedPostEntity>> = dao.getAllSavedPosts()

    suspend fun savePost(post: SavedPostEntity): Long = dao.insertPost(post)

    suspend fun deletePost(post: SavedPostEntity) = dao.deletePost(post)

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
