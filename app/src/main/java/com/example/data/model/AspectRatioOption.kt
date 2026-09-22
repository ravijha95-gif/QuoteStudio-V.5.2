package com.example.data.model

enum class AspectRatioOption(
    val label: String,
    val subLabel: String,
    val ratioWidth: Float,
    val ratioHeight: Float,
    val exportWidth: Int,
    val exportHeight: Int
) {
    PORTRAIT(
        label = "4:5",
        subLabel = "Feed Portrait (1080 × 1350)",
        ratioWidth = 4f,
        ratioHeight = 5f,
        exportWidth = 1080,
        exportHeight = 1350
    ),
    SQUARE(
        label = "1:1",
        subLabel = "Square (1080 × 1080)",
        ratioWidth = 1f,
        ratioHeight = 1f,
        exportWidth = 1080,
        exportHeight = 1080
    ),
    STORY(
        label = "9:16",
        subLabel = "Story / Reel (1080 × 1920)",
        ratioWidth = 9f,
        ratioHeight = 16f,
        exportWidth = 1080,
        exportHeight = 1920
    );

    val aspectRatio: Float
        get() = ratioWidth / ratioHeight
}
