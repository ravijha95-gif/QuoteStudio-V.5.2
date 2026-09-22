package com.example.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object InstagramApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private const val GRAPH_API_BASE = "https://graph.facebook.com/v19.0"

    /**
     * Verifies the Meta Graph API credentials by requesting profile metadata.
     */
    suspend fun verifyConnection(accessToken: String, accountId: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                if (accessToken.isBlank() || accountId.isBlank()) {
                    return@withContext Result.failure(Exception("Access Token and Account ID must not be empty."))
                }

                val targetEndpoint = "$GRAPH_API_BASE/$accountId?fields=id,username,name&access_token=$accessToken"
                val request = Request.Builder()
                    .url(targetEndpoint)
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string().orEmpty()

                if (response.isSuccessful) {
                    val json = JSONObject(responseBody)
                    val username = json.optString("username", accountId)
                    Result.success("Successfully verified Instagram Account: @$username")
                } else {
                    val errorJson = JSONObject(responseBody).optJSONObject("error")
                    val message = errorJson?.optString("message") ?: "HTTP ${response.code}: $responseBody"
                    Result.failure(Exception(message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Publishes a photo to Instagram via Meta Graph API:
     * Step 1: Create an IG Media container
     * Step 2: Publish the media container
     */
    suspend fun publishPhotoPost(
        accessToken: String,
        accountId: String,
        imageUrl: String,
        caption: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (accessToken.isBlank() || accountId.isBlank()) {
                return@withContext Result.failure(Exception("Meta Graph API credentials missing."))
            }

            // Step 1: Create Media Container
            val containerUrl = "$GRAPH_API_BASE/$accountId/media"
            val formBody = FormBody.Builder()
                .add("image_url", imageUrl)
                .add("caption", caption)
                .add("access_token", accessToken)
                .build()

            val containerRequest = Request.Builder()
                .url(containerUrl)
                .post(formBody)
                .build()

            val containerResponse = client.newCall(containerRequest).execute()
            val containerBody = containerResponse.body?.string().orEmpty()

            if (!containerResponse.isSuccessful) {
                val errorMsg = try {
                    JSONObject(containerBody).optJSONObject("error")?.optString("message")
                } catch (e: Exception) {
                    null
                } ?: "Failed to create media container ($containerBody)"
                return@withContext Result.failure(Exception(errorMsg))
            }

            val containerJson = JSONObject(containerBody)
            val creationId = containerJson.getString("id")

            // Wait 2 seconds for Instagram's media processing pipeline
            kotlinx.coroutines.delay(2000)

            // Step 2: Publish Container
            val publishUrl = "$GRAPH_API_BASE/$accountId/media_publish"
            val publishBody = FormBody.Builder()
                .add("creation_id", creationId)
                .add("access_token", accessToken)
                .build()

            val publishRequest = Request.Builder()
                .url(publishUrl)
                .post(publishBody)
                .build()

            val publishResponse = client.newCall(publishRequest).execute()
            val publishResponseBody = publishResponse.body?.string().orEmpty()

            if (publishResponse.isSuccessful) {
                val publishJson = JSONObject(publishResponseBody)
                val publishedPostId = publishJson.optString("id", creationId)
                Result.success(publishedPostId)
            } else {
                val errorMsg = try {
                    JSONObject(publishResponseBody).optJSONObject("error")?.optString("message")
                } catch (e: Exception) {
                    null
                } ?: "Failed to publish media ($publishResponseBody)"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
