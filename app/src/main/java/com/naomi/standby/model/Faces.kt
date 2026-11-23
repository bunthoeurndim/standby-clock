package com.naomi.standby.model

import androidx.compose.ui.graphics.Color

/** A face colour theme. Each theme changes the rendered colour of every face. */
enum class FaceTheme(val displayName: String, val color: Color, val idPart: String) {
    WHITE("White", Color.White, "white"),
    RED("Night Red", Color(0xFFFF2A1A), "red"),
    GREEN("Green", Color(0xFF4CAF50), "green"),
    AMBER("Amber", Color(0xFFFFB300), "amber"),
    BLUE("Blue", Color(0xFF42A5F5), "blue"),
    PINK("Pink", Color(0xFFEC407A), "pink")
}

/** Two layout variants per face: Large fills the standby area, Compact leaves room for the date line. */
enum class FaceLayout(val displayName: String, val idPart: String, val sizeFactor: Float) {
    LARGE("Large", "large", 1.0f),
    COMPACT("Compact", "compact", 0.72f)
}

/** The 12 distinct base face renderers. */
enum class FaceBase(val id: String, val displayName: String, val description: String) {
    BIG_DIGITAL("big_digital", "Big Digital", "Huge digits with date underneath"),
    FLIP("flip", "Flip Clock", "Split-flap style digit cards"),
    ANALOG("analog", "Analog", "Classic round clock with hands"),
    SEGMENTED_LCD("lcd", "Segmented LCD", "Seven-segment display"),
    NEON("neon", "Neon", "Glowing neon digits on black"),
    WORD_CLOCK("word", "Word Clock", "The time spelled out in words"),
    ROMAN("roman", "Roman Numerals", "Analog dial with Roman numerals"),
    MATRIX("matrix", "Matrix", "Falling character rain with the time"),
    PIXEL_ART("pixel", "Pixel Art", "Blocky 5x7 pixel digits"),
    SOLAR("solar", "Solar", "Sun arc showing the time of day"),
    WORLD_CLOCK("world", "World Clock", "Three time zones at once"),
    DEMENTIA("dementia", "Day Clock", "Full date and time of day in large text")
}

/**
 * One entry in the face picker. Every entry renders differently:
 * base renderer x theme colour x layout size.
 */
data class FaceEntry(
    val id: String,
    val base: FaceBase,
    val theme: FaceTheme,
    val layout: FaceLayout
) {
    val name: String
        get() = "${base.displayName} — ${theme.displayName} ${layout.displayName}"
}

/** The full face catalogue. 12 bases x 6 themes x 2 layouts = 144 faces. */
object FaceRegistry {
    val allFaces: List<FaceEntry> = buildList {
        for (base in FaceBase.entries) {
            for (theme in FaceTheme.entries) {
                for (layout in FaceLayout.entries) {
                    val id = "${base.id}_${theme.idPart}_${layout.idPart}"
                    add(FaceEntry(id, base, theme, layout))
                }
            }
        }
    }

    val count: Int get() = allFaces.size

    fun byId(id: String): FaceEntry =
        allFaces.firstOrNull { it.id == id } ?: allFaces.first()

    fun facesForBase(base: FaceBase): List<FaceEntry> =
        allFaces.filter { it.base == base }
}
