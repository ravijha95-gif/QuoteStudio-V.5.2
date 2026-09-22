package com.example.data.generator

import com.example.data.model.QuoteCategory

data class InspirationalQuote(
    val quote: String,
    val author: String,
    val category: QuoteCategory,
    val brand: String = "@noble.thoughts"
)

object QuoteInspirations {
    val SAMPLE_QUOTES = listOf(
        InspirationalQuote(
            quote = "We must be doing something to be happy — action is no less necessary to us than thought.",
            author = "William Hazlitt",
            category = QuoteCategory.PHILOSOPHY,
            brand = "@noble.thoughts"
        ),
        InspirationalQuote(
            quote = "You have power over your mind - not outside events. Realize this, and you will find strength.",
            author = "Marcus Aurelius",
            category = QuoteCategory.STOICISM,
            brand = "@stoic.daily"
        ),
        InspirationalQuote(
            quote = "Do it anyway. The fear never goes away, but courage grows with every step forward.",
            author = "Unknown",
            category = QuoteCategory.MOTIVATION,
            brand = "@unrelenting.mind"
        ),
        InspirationalQuote(
            quote = "We suffer more often in imagination than in reality.",
            author = "Seneca",
            category = QuoteCategory.PHILOSOPHY,
            brand = "@stoic.daily"
        ),
        InspirationalQuote(
            quote = "He who has a why to live can bear almost any how.",
            author = "Friedrich Nietzsche",
            category = QuoteCategory.MINDSET,
            brand = "@deep.thinkers"
        ),
        InspirationalQuote(
            quote = "In the depths of winter, I finally learned that within me there lay an invincible summer.",
            author = "Albert Camus",
            category = QuoteCategory.CINEMA,
            brand = "@cinema.poetry"
        ),
        InspirationalQuote(
            quote = "Simplicity is the ultimate sophistication.",
            author = "Leonardo da Vinci",
            category = QuoteCategory.AMBITION,
            brand = "@mastery.craft"
        ),
        InspirationalQuote(
            quote = "Tell me, what is it you plan to do with your one wild and precious life?",
            author = "Mary Oliver",
            category = QuoteCategory.LIFE,
            brand = "@living.deliberately"
        ),
        InspirationalQuote(
            quote = "The quieter you become, the more you are able to hear.",
            author = "Rumi",
            category = QuoteCategory.LIFE,
            brand = "@noble.thoughts"
        ),
        InspirationalQuote(
            quote = "Start before you are ready. Clarity comes from engagement, not armchair thought.",
            author = "Marie Forleo",
            category = QuoteCategory.MOTIVATION,
            brand = "@unrelenting.mind"
        )
    )

    fun getRandom(): InspirationalQuote = SAMPLE_QUOTES.random()
}
