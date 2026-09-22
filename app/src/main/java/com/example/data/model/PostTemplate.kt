package com.example.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

enum class TemplateBackgroundType {
    SOLID,
    RADIAL_DARK,
    RADIAL_GOLD,
    CINEMA_GRADIENT,
    LINEAR_CREAM,
    SPLIT_CONTRAST,
    TEAL_RADIAL
}

enum class TypographyFamily(val label: String, val fontFamily: FontFamily) {
    SERIF("Editorial Serif", FontFamily.Serif),
    SANS_SERIF("Modern Sans", FontFamily.SansSerif),
    MONOSPACE("Minimal Mono", FontFamily.Monospace),
    CURSIVE("Literary Cursive", FontFamily.Cursive)
}

enum class TemplateGroup(val label: String) {
    ALL("All"),
    MINIMAL("Minimal"),
    PHOTOGRAPHY("Photo"),
    EDITORIAL("Editorial"),
    CINEMATIC("Cinematic"),
    PHRASE("Phrase")
}

data class PostTemplate(
    val id: String,
    val name: String,
    val description: String,
    val sampleText: String,
    val group: TemplateGroup,
    val keywords: List<String>,
    val bgType: TemplateBackgroundType,
    val primaryBgColor: Color,
    val secondaryBgColor: Color,
    val textColor: Color,
    val authorColor: Color = textColor.copy(alpha = 0.72f),
    val accentLineColor: Color = textColor.copy(alpha = 0.25f),
    val defaultFontFamily: TypographyFamily = TypographyFamily.SERIF,
    val defaultFontWeight: FontWeight = FontWeight.Bold,
    val defaultTextAlign: TextAlign = TextAlign.Center,
    val defaultVerticalBottom: Boolean = false,
    val isUppercase: Boolean = false,
    val hasDividerLine: Boolean = false
) {
    companion object {
        val ALL_TEMPLATES = listOf(
            PostTemplate(
                id = "dark",
                name = "Minimal Dark",
                description = "Quiet, deep, restrained with dark navy vignette",
                sampleText = "KEEP MOVING.",
                group = TemplateGroup.MINIMAL,
                keywords = listOf("keep", "step", "hard", "never", "thought", "quiet", "strength", "must", "silence"),
                bgType = TemplateBackgroundType.RADIAL_DARK,
                primaryBgColor = Color(0xFF10151D),
                secondaryBgColor = Color(0xFF06080B),
                textColor = Color(0xFFF5F6F8),
                defaultFontFamily = TypographyFamily.SERIF,
                defaultFontWeight = FontWeight.Bold,
                defaultTextAlign = TextAlign.Center
            ),
            PostTemplate(
                id = "photo",
                name = "Photography + Quote",
                description = "Cinematic photo overlay with bottom-anchored type",
                sampleText = "THE ROAD GOES ON.",
                group = TemplateGroup.PHOTOGRAPHY,
                keywords = listOf("road", "ocean", "mountain", "journey", "life", "sun", "light", "go", "lost", "path"),
                bgType = TemplateBackgroundType.SOLID,
                primaryBgColor = Color(0xFF1E293B),
                secondaryBgColor = Color(0xFF0F172A),
                textColor = Color(0xFFF8FAFC),
                defaultFontFamily = TypographyFamily.SANS_SERIF,
                defaultFontWeight = FontWeight.ExtraBold,
                defaultTextAlign = TextAlign.Start,
                defaultVerticalBottom = true
            ),
            PostTemplate(
                id = "editorial",
                name = "Elegant Editorial",
                description = "Warm cream parchment paper with refined black serif",
                sampleText = "BECOME WHO YOU ARE.",
                group = TemplateGroup.EDITORIAL,
                keywords = listOf("wisdom", "truth", "meaning", "thought", "become", "yourself", "life", "action"),
                bgType = TemplateBackgroundType.SOLID,
                primaryBgColor = Color(0xFFE8E1D5),
                secondaryBgColor = Color(0xFFDFD7CA),
                textColor = Color(0xFF171717),
                defaultFontFamily = TypographyFamily.SERIF,
                defaultFontWeight = FontWeight.Bold,
                defaultTextAlign = TextAlign.Start,
                hasDividerLine = true
            ),
            PostTemplate(
                id = "cinema",
                name = "Cinematic Poster",
                description = "Moody, dramatic film glow with warm golden bronze",
                sampleText = "SOME STORIES STAY.",
                group = TemplateGroup.CINEMATIC,
                keywords = listOf("pain", "memory", "alone", "lost", "night", "goodbye", "heart", "dark", "stories", "film"),
                bgType = TemplateBackgroundType.CINEMA_GRADIENT,
                primaryBgColor = Color(0xFF65503C),
                secondaryBgColor = Color(0xFF070605),
                textColor = Color(0xFFF5F6F8),
                defaultFontFamily = TypographyFamily.SERIF,
                defaultFontWeight = FontWeight.SemiBold,
                defaultTextAlign = TextAlign.Start,
                defaultVerticalBottom = true
            ),
            PostTemplate(
                id = "phrase",
                name = "Bold Phrase",
                description = "High-impact poster with clean uppercase typography",
                sampleText = "DO IT ANYWAY.",
                group = TemplateGroup.PHRASE,
                keywords = listOf("action", "do", "discipline", "success", "win", "focus", "start", "courage", "must", "now"),
                bgType = TemplateBackgroundType.SOLID,
                primaryBgColor = Color(0xFFF0EDE6),
                secondaryBgColor = Color(0xFFE5E0D6),
                textColor = Color(0xFF141414),
                defaultFontFamily = TypographyFamily.SANS_SERIF,
                defaultFontWeight = FontWeight.Black,
                defaultTextAlign = TextAlign.Center,
                isUppercase = true
            ),
            PostTemplate(
                id = "magazine",
                name = "Modern Magazine",
                description = "Editorial magazine layout with subtle warm gradient",
                sampleText = "A DIFFERENT WAY TO LIVE.",
                group = TemplateGroup.EDITORIAL,
                keywords = listOf("ideas", "meaning", "life", "thought", "truth", "different", "way", "lifestyle"),
                bgType = TemplateBackgroundType.LINEAR_CREAM,
                primaryBgColor = Color(0xFFE4DFD4),
                secondaryBgColor = Color(0xFFF7F4ED),
                textColor = Color(0xFF181818),
                defaultFontFamily = TypographyFamily.SERIF,
                defaultFontWeight = FontWeight.Bold,
                defaultTextAlign = TextAlign.Start
            ),
            PostTemplate(
                id = "luxury",
                name = "Luxury Night",
                description = "Premium champagne gold glow on obsidian black",
                sampleText = "BUILD SOMETHING LASTING.",
                group = TemplateGroup.MINIMAL,
                keywords = listOf("success", "future", "build", "dream", "ambition", "lasting", "legacy", "wealth"),
                bgType = TemplateBackgroundType.RADIAL_GOLD,
                primaryBgColor = Color(0xFF55462D),
                secondaryBgColor = Color(0xFF080705),
                textColor = Color(0xFFEFE1BD),
                authorColor = Color(0xFFCDBF9D),
                accentLineColor = Color(0x66CDBF9D),
                defaultFontFamily = TypographyFamily.SERIF,
                defaultFontWeight = FontWeight.Medium,
                defaultTextAlign = TextAlign.Center
            ),
            PostTemplate(
                id = "paper",
                name = "Vintage Paper",
                description = "Literary parchment with antique tone and rule line",
                sampleText = "WORDS MATTER.",
                group = TemplateGroup.EDITORIAL,
                keywords = listOf("words", "wisdom", "memory", "thought", "story", "time", "books", "literature"),
                bgType = TemplateBackgroundType.SOLID,
                primaryBgColor = Color(0xFFDED8C9),
                secondaryBgColor = Color(0xFFD2CABA),
                textColor = Color(0xFF1A1916),
                defaultFontFamily = TypographyFamily.SERIF,
                defaultFontWeight = FontWeight.Bold,
                defaultTextAlign = TextAlign.Start,
                hasDividerLine = true
            ),
            PostTemplate(
                id = "neon",
                name = "Midnight Modern",
                description = "Contemporary deep teal radial glow with high clarity",
                sampleText = "START BEFORE READY.",
                group = TemplateGroup.CINEMATIC,
                keywords = listOf("future", "change", "start", "ready", "dream", "now", "tech", "growth"),
                bgType = TemplateBackgroundType.TEAL_RADIAL,
                primaryBgColor = Color(0xFF19354A),
                secondaryBgColor = Color(0xFF070B11),
                textColor = Color(0xFFF5F6F8),
                defaultFontFamily = TypographyFamily.SANS_SERIF,
                defaultFontWeight = FontWeight.ExtraBold,
                defaultTextAlign = TextAlign.Center
            ),
            PostTemplate(
                id = "split",
                name = "Editorial Split",
                description = "Striking two-tone vertical split composition",
                sampleText = "CHOOSE YOUR NEXT STEP.",
                group = TemplateGroup.EDITORIAL,
                keywords = listOf("choice", "choose", "action", "change", "next", "step", "life", "two"),
                bgType = TemplateBackgroundType.SPLIT_CONTRAST,
                primaryBgColor = Color(0xFF111111),
                secondaryBgColor = Color(0xFFDED9CE),
                textColor = Color(0xFFFFFFFF),
                defaultFontFamily = TypographyFamily.SERIF,
                defaultFontWeight = FontWeight.Bold,
                defaultTextAlign = TextAlign.Start
            )
        )

        fun findById(id: String): PostTemplate {
            return ALL_TEMPLATES.firstOrNull { it.id == id } ?: ALL_TEMPLATES[0]
        }
    }
}
