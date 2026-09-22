package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "instagram_profile")
data class InstagramProfileEntity(
    @PrimaryKey val id: Int = 1, // Single active linked profile
    val username: String,
    val fullName: String = "",
    val bio: String = "",
    val profilePicUrl: String? = null,
    val followerCount: Int = 12500,
    val postsCount: Int = 248,
    val accountType: String = "CREATOR", // "CREATOR", "BUSINESS", "PERSONAL"
    val isLinked: Boolean = true,
    val metaAccessToken: String = "",
    val metaAccountId: String = "",
    val autoPublishEnabled: Boolean = true,
    val preferredPostFormat: String = "FEED", // "FEED", "STORY"
    val linkedTimestamp: Long = System.currentTimeMillis()
)
