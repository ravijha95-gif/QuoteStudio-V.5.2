package com.example.data.generator

import com.example.data.model.PostTemplate
import com.example.data.model.QuoteCategory

data class AnalysisResult(
    val suggestedCategory: QuoteCategory,
    val topTemplateId: String,
    val recommendedTemplates: List<TemplateSuggestion>,
    val defaultCaption: String,
    val punchyCaption: String,
    val storyCaption: String,
    val hashtags: List<String>
)

data class TemplateSuggestion(
    val template: PostTemplate,
    val score: Int,
    val reason: String
)

object SemanticAnalyzer {

    fun analyze(quote: String, author: String, brand: String, categoryOverride: QuoteCategory? = null): AnalysisResult {
        val qLower = quote.lowercase()
        val words = qLower.split(Regex("\\s+")).filter { it.isNotBlank() }
        val wordCount = words.size

        // 1. Determine or verify Category
        val category: QuoteCategory = categoryOverride ?: run {
            when {
                containsAny(qLower, listOf("action", "do", "start", "discipline", "win", "focus", "courage", "hard", "grind")) -> QuoteCategory.MOTIVATION
                containsAny(qLower, listOf("control", "power", "mind", "outside", "endure", "bear", "fate", "stoic")) -> QuoteCategory.STOICISM
                containsAny(qLower, listOf("thought", "truth", "wisdom", "think", "knowledge", "reason", "philosophy")) -> QuoteCategory.PHILOSOPHY
                containsAny(qLower, listOf("grow", "perspective", "lens", "clarity", "evolution", "learn", "habit")) -> QuoteCategory.MINDSET
                containsAny(qLower, listOf("craft", "ambition", "mastery", "build", "lasting", "excellence", "standards")) -> QuoteCategory.AMBITION
                containsAny(qLower, listOf("night", "alone", "dark", "pain", "memory", "silence", "stories", "heart", "winter")) -> QuoteCategory.CINEMA
                else -> QuoteCategory.LIFE
            }
        }

        // 2. Score templates based on keywords and length
        val scores = mutableMapOf<String, Int>()
        PostTemplate.ALL_TEMPLATES.forEach { scores[it.id] = 0 }

        val keywordMap = mapOf(
            "dark" to listOf("keep", "step", "hard", "never", "thought", "quiet", "strength", "must", "silence", "deep"),
            "photo" to listOf("road", "ocean", "mountain", "journey", "life", "sun", "light", "go", "lost", "path", "horizon"),
            "editorial" to listOf("wisdom", "truth", "meaning", "thought", "become", "yourself", "life", "action", "literature"),
            "cinema" to listOf("pain", "memory", "alone", "lost", "night", "goodbye", "heart", "dark", "stories", "winter", "deep"),
            "phrase" to listOf("action", "do", "discipline", "success", "win", "focus", "start", "courage", "must", "now", "stop"),
            "magazine" to listOf("ideas", "meaning", "life", "thought", "truth", "different", "way", "lifestyle", "perspective"),
            "luxury" to listOf("success", "future", "build", "dream", "ambition", "lasting", "legacy", "wealth", "gold", "craft"),
            "paper" to listOf("words", "wisdom", "memory", "thought", "story", "time", "books", "literature", "letters"),
            "neon" to listOf("future", "change", "start", "ready", "dream", "now", "tech", "growth", "bold"),
            "split" to listOf("choice", "choose", "action", "change", "next", "step", "life", "two", "contrast", "either")
        )

        keywordMap.forEach { (id, keys) ->
            keys.forEach { key ->
                if (qLower.contains(key)) {
                    scores[id] = (scores[id] ?: 0) + 2
                }
            }
        }

        // Length-based bias
        if (wordCount < 9) {
            scores["phrase"] = (scores["phrase"] ?: 0) + 4
            scores["dark"] = (scores["dark"] ?: 0) + 2
            scores["neon"] = (scores["neon"] ?: 0) + 2
        } else if (wordCount > 18) {
            scores["editorial"] = (scores["editorial"] ?: 0) + 4
            scores["magazine"] = (scores["magazine"] ?: 0) + 3
            scores["cinema"] = (scores["cinema"] ?: 0) + 3
            scores["paper"] = (scores["paper"] ?: 0) + 3
        }

        // Category biases
        when (category) {
            QuoteCategory.MOTIVATION -> {
                scores["phrase"] = (scores["phrase"] ?: 0) + 3
                scores["dark"] = (scores["dark"] ?: 0) + 2
            }
            QuoteCategory.CINEMA -> {
                scores["cinema"] = (scores["cinema"] ?: 0) + 5
                scores["photo"] = (scores["photo"] ?: 0) + 2
            }
            QuoteCategory.PHILOSOPHY, QuoteCategory.STOICISM -> {
                scores["editorial"] = (scores["editorial"] ?: 0) + 4
                scores["paper"] = (scores["paper"] ?: 0) + 3
                scores["dark"] = (scores["dark"] ?: 0) + 2
            }
            QuoteCategory.AMBITION -> {
                scores["luxury"] = (scores["luxury"] ?: 0) + 5
                scores["split"] = (scores["split"] ?: 0) + 3
            }
            QuoteCategory.MINDSET, QuoteCategory.LIFE -> {
                scores["magazine"] = (scores["magazine"] ?: 0) + 3
                scores["editorial"] = (scores["editorial"] ?: 0) + 2
            }
        }

        val rankedTemplates = PostTemplate.ALL_TEMPLATES.map { template ->
            val score = scores[template.id] ?: 0
            val reason = when {
                score > 5 -> "High semantic resonance with your text's cadence and themes"
                score in 2..5 -> "Balanced typography and contrast suited for this length"
                else -> "Clean aesthetic layout"
            }
            TemplateSuggestion(template, score, reason)
        }.sortedByDescending { it.score }

        val topTemplateId = rankedTemplates.firstOrNull()?.template?.id ?: "dark"

        // 3. Generate Captions
        val authorSuffix = if (author.isNotBlank()) "— $author" else ""
        val brandNotice = if (brand.isNotBlank()) {
            "Follow $brand for daily timeless perspectives and insightful reminders."
        } else {
            "Save this post for whenever you need the reminder."
        }

        val tagsStr = category.tags.joinToString(" ")

        // Style 1: Deep & Reflective
        val defaultCaption = buildString {
            append("\"$quote\" $authorSuffix\n\n")
            append("• What this means:\n")
            append("${category.meaning}\n\n")
            append("• Question to reflect on:\n")
            append("${category.question}\n\n")
            append("$brandNotice\n.\n.\n")
            append(tagsStr)
        }

        // Style 2: Punchy & Direct Hook
        val punchyCaption = buildString {
            append("Read this twice:\n\n")
            append("\"$quote\" $authorSuffix\n\n")
            append("Most people wait for readiness. Real progress starts before clarity arrives.\n\n")
            append("Double-tap if you needed to hear this today.\n")
            if (brand.isNotBlank()) append("📍 Follow $brand\n")
            append(".\n.\n")
            append(tagsStr)
        }

        // Style 3: Storytelling & Perspective
        val storyCaption = buildString {
            append("A quiet truth worth remembering:\n\n")
            append("\"$quote\"\n\n")
            append("${category.meaning} The hardest part isn't knowing what to do—it's having the calm conviction to execute it consistently.\n\n")
            append("Share this with someone who needs this perspective.\n")
            if (brand.isNotBlank()) append("✨ $brand\n")
            append(".\n.\n")
            append(tagsStr)
        }

        return AnalysisResult(
            suggestedCategory = category,
            topTemplateId = topTemplateId,
            recommendedTemplates = rankedTemplates.take(4),
            defaultCaption = defaultCaption,
            punchyCaption = punchyCaption,
            storyCaption = storyCaption,
            hashtags = category.tags
        )
    }

    private fun containsAny(text: String, keywords: List<String>): Boolean {
        return keywords.any { text.contains(it) }
    }
}
