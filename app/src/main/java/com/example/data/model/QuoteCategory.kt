package com.example.data.model

enum class QuoteCategory(
    val displayName: String,
    val tag: String,
    val meaning: String,
    val question: String,
    val tags: List<String>
) {
    PHILOSOPHY(
        displayName = "Philosophy & Wisdom",
        tag = "PHILOSOPHY / 2026",
        meaning = "Contemplation alone cannot create fulfillment; purposeful movement and deliberate choices turn ideas into reality.",
        question = "How often do you balance thinking with actual execution?",
        tags = listOf("#philosophy", "#stoicism", "#deepthoughts", "#wisdom", "#thinkers", "#mindsetshift", "#innergrowth", "#quoteoftheday")
    ),
    MOTIVATION(
        displayName = "Action & Motivation",
        tag = "DISCIPLINE / 2026",
        meaning = "Waiting for ideal circumstances is a trap. True momentum and confidence only appear after the first bold step is taken.",
        question = "What is one action you have been putting off that you can begin today?",
        tags = listOf("#motivation", "#discipline", "#actiontaker", "#focus", "#grit", "#dailygrind", "#mindsetiseverything", "#keepmoving")
    ),
    MINDSET(
        displayName = "Growth & Mindset",
        tag = "PERSPECTIVE / 2026",
        meaning = "Our experiences are shaped not merely by external events, but by the internal lens through which we choose to interpret them.",
        question = "Where can you shift your perspective to unlock more clarity?",
        tags = listOf("#mindset", "#personalgrowth", "#clarity", "#selfmastery", "#perspective", "#evolution", "#mentalstrength", "#intentions")
    ),
    LIFE(
        displayName = "Life & Reflection",
        tag = "REFLECTIONS / 2026",
        meaning = "Fulfillment is found in living deliberately rather than passively letting days drift by in endless anticipation.",
        question = "What made you feel truly present this week?",
        tags = listOf("#lifequotes", "#reflections", "#consciousliving", "#slowliving", "#lifelessons", "#presence", "#meaningfulmoments", "#quotestoliveby")
    ),
    CINEMA(
        displayName = "Moody & Cinema",
        tag = "CINEMA / 2026",
        meaning = "Certain truths only surface when silence falls and we examine what we carry forward in the dark.",
        question = "Which story or line has stayed with you the longest?",
        tags = listOf("#cinematic", "#moodygrams", "#visualpoetry", "#narrative", "#cinematography", "#nostalgia", "#nightthoughts", "#storytelling")
    ),
    STOICISM(
        displayName = "Stoic Resilience",
        tag = "STOIC / 2026",
        meaning = "You have power over your mind - not outside events. Realize this, and you will find unwavering inner strength.",
        question = "What outside factor are you currently trying to control that you should let go of?",
        tags = listOf("#stoic", "#marcusAurelius", "#seneca", "#amorFati", "#innerPeace", "#resilience", "#dailyStoic", "#wisdom")
    ),
    AMBITION(
        displayName = "Ambition & Craft",
        tag = "AMBITION / 2026",
        meaning = "Mastery is the quiet devotion to craft when no applause is present. Great outcomes demand sustained commitment.",
        question = "What standard of craft are you willing to uphold relentlessly?",
        tags = listOf("#ambition", "#craftsmanship", "#deepwork", "#relentless", "#excellence", "#builder", "#legacy", "#mastery")
    );

    companion object {
        fun fromName(name: String): QuoteCategory {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) }
                ?: PHILOSOPHY
        }
    }
}
