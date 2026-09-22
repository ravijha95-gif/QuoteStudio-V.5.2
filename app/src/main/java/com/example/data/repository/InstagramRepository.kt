package com.example.data.repository

import com.example.data.api.InstagramApiService
import com.example.data.db.InstagramProfileDao
import com.example.data.model.InstagramProfileEntity
import kotlinx.coroutines.flow.Flow

class InstagramRepository(private val dao: InstagramProfileDao) {

    val profile: Flow<InstagramProfileEntity?> = dao.getProfile()

    suspend fun getProfileDirect(): InstagramProfileEntity? = dao.getProfileDirect()

    suspend fun linkProfile(
        username: String,
        fullName: String,
        bio: String,
        accountType: String,
        metaAccessToken: String = "",
        metaAccountId: String = "",
        autoPublishEnabled: Boolean = true
    ) {
        val cleanUsername = username.removePrefix("@").trim()
        val entity = InstagramProfileEntity(
            id = 1,
            username = cleanUsername,
            fullName = fullName.ifBlank { cleanUsername.replaceFirstChar { it.uppercase() } },
            bio = bio,
            followerCount = (12000..25000).random(),
            postsCount = (80..320).random(),
            accountType = accountType,
            isLinked = true,
            metaAccessToken = metaAccessToken.trim(),
            metaAccountId = metaAccountId.trim(),
            autoPublishEnabled = autoPublishEnabled,
            linkedTimestamp = System.currentTimeMillis()
        )
        dao.saveProfile(entity)
    }

    suspend fun disconnectProfile() {
        dao.disconnectProfile()
    }

    suspend fun toggleAutoPublish(enabled: Boolean) {
        dao.updateAutoPublish(enabled)
    }

    suspend fun updateMetaCredentials(token: String, accountId: String) {
        dao.updateMetaCredentials(token.trim(), accountId.trim())
    }

    suspend fun verifyMetaConnection(token: String, accountId: String): Result<String> {
        return InstagramApiService.verifyConnection(token, accountId)
    }
}
