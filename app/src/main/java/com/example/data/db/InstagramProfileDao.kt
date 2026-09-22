package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.InstagramProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstagramProfileDao {
    @Query("SELECT * FROM instagram_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<InstagramProfileEntity?>

    @Query("SELECT * FROM instagram_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileDirect(): InstagramProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: InstagramProfileEntity)

    @Query("DELETE FROM instagram_profile WHERE id = 1")
    suspend fun disconnectProfile()

    @Query("UPDATE instagram_profile SET autoPublishEnabled = :enabled WHERE id = 1")
    suspend fun updateAutoPublish(enabled: Boolean)

    @Query("UPDATE instagram_profile SET metaAccessToken = :token, metaAccountId = :accountId WHERE id = 1")
    suspend fun updateMetaCredentials(token: String, accountId: String)
}
