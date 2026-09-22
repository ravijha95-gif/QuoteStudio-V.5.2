package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_posts")
data class SavedPostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quote: String,
    val author: String,
    val category: String,
    val brandHandle: String,
    val templateId: String,
    val aspectRatio: String,
    val caption: String,
    val textSize: Float,
    val textAlign: String,
    val showTagline: Boolean,
    val brandPosition: String,
    val customPhotoUri: String? = null,
    val overlayOpacity: Float = 0.55f,
    val timestamp: Long = System.currentTimeMillis()
)
