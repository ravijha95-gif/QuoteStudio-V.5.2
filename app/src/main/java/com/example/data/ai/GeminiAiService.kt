package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.generator.AnalysisResult
import com.example.data.generator.SemanticAnalyzer
import com.example.data.model.PostTemplate
import com.example.data.model.QuoteCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiAiService {
    private const val TAG = "GeminiAiService"
    private const val MODEL_NAME = "gemini-3.5-flash"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun isConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun analyzeWithAi(
        quote: String,
        author: String,
        brand: String,
        currentCategory: QuoteCategory
    ): Result<AnalysisResult> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isConfigured()) {
            // Fallback to local semantic analyzer
            val local = SemanticAnalyzer.analyze(quote, author, brand, currentCategory)
            return@withContext Result.success(local)
        }

        try {
            val systemPrompt = """
                You are an expert Instagram visual content strategist and literary editor.
                Analyze the provided quote for an aesthetic Instagram post.
                Return ONLY a valid JSON object matching this schema:
                {
                  "category": "Philosophy" | "Motivation" | "Mindset" | "Life" | "Cinema" | "Stoicism" | "Ambition",
                  "deepMeaning": "2-3 sentences uncovering the true philosophical subtext and practical relevance.",
                  "reflectionQuestion": "One deep, engaging question that encourages comments and saves.",
                  "recommendedTemplate": "dark" | "photo" | "editorial" | "cinema" | "phrase" | "magazine" | "luxury" | "paper" | "neon" | "split",
                  "viralCaption": "High-converting Instagram caption with a powerful opening hook, spacing, and call to action.",
                  "storyCaption": "Reflective micro-essay caption exploring personal growth.",
                  "hashtags": ["#tag1", "#tag2", ...]
                }
            """.trimIndent()

            val userContent = """
                Quote: "$quote"
                Author: ${author.ifBlank { "Unknown" }}
                Brand Handle: ${brand.ifBlank { "@quote.studio" }}
            """.trimIndent()

            val payload = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", "$systemPrompt\n\n$userContent") })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val genConfig = JSONObject().apply {
                    put("temperature", 0.7)
                    put("responseMimeType", "application/json")
                }
                put("generationConfig", genConfig)
            }

            val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                Log.w(TAG, "Gemini API error ${response.code}: $responseBody. Falling back to local analyzer.")
                return@withContext Result.success(SemanticAnalyzer.analyze(quote, author, brand, currentCategory))
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (text.isNullOrBlank()) {
                return@withContext Result.success(SemanticAnalyzer.analyze(quote, author, brand, currentCategory))
            }

            val parsed = JSONObject(text.trim())
            val categoryStr = parsed.optString("category", currentCategory.name)
            val detectedCategory = QuoteCategory.fromName(categoryStr)
            val deepMeaning = parsed.optString("deepMeaning", detectedCategory.meaning)
            val reflectionQuestion = parsed.optString("reflectionQuestion", detectedCategory.question)
            val recTemplate = parsed.optString("recommendedTemplate", "dark")
            val viralCaption = parsed.optString("viralCaption")
            val storyCaption = parsed.optString("storyCaption")

            val jsonTags = parsed.optJSONArray("hashtags")
            val tags = mutableListOf<String>()
            if (jsonTags != null) {
                for (i in 0 until jsonTags.length()) {
                    val t = jsonTags.optString(i)
                    if (t.isNotBlank()) tags.add(if (t.startsWith("#")) t else "#$t")
                }
            }
            if (tags.isEmpty()) tags.addAll(detectedCategory.tags)

            val authorSuffix = if (author.isNotBlank()) "— $author" else ""
            val defaultCaption = buildString {
                append("\"$quote\" $authorSuffix\n\n")
                append("• What this means:\n")
                append("$deepMeaning\n\n")
                append("• Question to reflect on:\n")
                append("$reflectionQuestion\n\n")
                if (brand.isNotBlank()) append("Follow $brand for daily perspectives.\n.\n.\n")
                append(tags.joinToString(" "))
            }

            val finalViralCaption = if (viralCaption.isNotBlank()) {
                "$viralCaption\n.\n.\n${tags.joinToString(" ")}"
            } else {
                defaultCaption
            }

            val finalStoryCaption = if (storyCaption.isNotBlank()) {
                "$storyCaption\n.\n.\n${tags.joinToString(" ")}"
            } else {
                defaultCaption
            }

            val ranked = PostTemplate.ALL_TEMPLATES.map {
                val score = if (it.id == recTemplate) 10 else 3
                val reason = if (it.id == recTemplate) "AI Selected: Optimal visual mood for this text" else "Alternative template"
                com.example.data.generator.TemplateSuggestion(it, score, reason)
            }.sortedByDescending { it.score }

            Result.success(
                AnalysisResult(
                    suggestedCategory = detectedCategory,
                    topTemplateId = recTemplate,
                    recommendedTemplates = ranked.take(4),
                    defaultCaption = defaultCaption,
                    punchyCaption = finalViralCaption,
                    storyCaption = finalStoryCaption,
                    hashtags = tags
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in Gemini API analysis", e)
            Result.success(SemanticAnalyzer.analyze(quote, author, brand, currentCategory))
        }
    }
}
