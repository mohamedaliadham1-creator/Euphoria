package com.example.euphoria.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
    DARK,
    LIGHT,
    AMOLED,
    FOLLOW_SYSTEM
}

enum class WallpaperOverlayMode(val displayName: String) {
    FULL("Full Screen"),
    BLURRED("Blurred Dream"),
    DARK("Dark Cinema"),
    GRADIENT("Neon Gradient"),
    NOW_PLAYING_ONLY("Now Playing Only")
}

enum class WaveformStyle(val displayName: String) {
    NEON("Neon Glow"),
    THIN("Thin Minimal"),
    SMOOTH("Smooth Wave"),
    BAR("Spectrum Bars"),
    MINIMAL("Subtle Line")
}

enum class PlayerThemeStyle(val displayName: String) {
    NEON_WAVE("Neon Wave"),
    CIRCULAR_VINYL("Circular Vinyl"),
    GLASS_PLAYER("Glass Aurora"),
    FULL_ARTWORK("Full Artwork"),
    MINIMAL_AMOLED("Minimal AMOLED"),
    FLOATING_CONTROLS("Floating Controls"),
    IMMERSIVE_ARTWORK("Immersive Blur"),
    GRADIENT_PLAYER("Gradient Sunset"),
    MINIMAL_WHITE("Studio White"),
    FUTURISTIC_PLAYER("Cyber Matrix")
}

enum class EuphoriaFontFamily(val displayName: String) {
    SYSTEM("Euphoria Default"),
    FUTURISTIC("Futuristic Sans"),
    ROUNDED("Smooth Rounded"),
    SERIF("Classic Serif"),
    MONOSPACE("Cyber Mono")
}

data class EuphoriaThemeData(
    val id: String,
    val name: String,
    val primaryBg: Color,
    val secondaryBg: Color,
    val surfaceColor: Color,
    val cardColor: Color,
    val accentColor: Color,
    val secondaryAccentColor: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val glowColor: Color,
    val cardAlpha: Float = 0.65f,
    val cornerRadiusDp: Float = 22f,
    val blurRadiusDp: Float = 16f,
    val isDark: Boolean = true,
    val isAmoled: Boolean = false,
    val wallpaperUri: String? = null,
    val wallpaperOverlayMode: WallpaperOverlayMode = WallpaperOverlayMode.GRADIENT,
    val wallpaperBrightness: Float = 0.5f,
    val wallpaperBlur: Float = 8f,
    val wallpaperAlpha: Float = 0.75f,
    val wallpaperSaturation: Float = 1.0f,
    val playerThemeStyle: PlayerThemeStyle = PlayerThemeStyle.NEON_WAVE,
    val waveformStyle: WaveformStyle = WaveformStyle.NEON,
    val waveformColor: Color = accentColor,
    val fontStyle: EuphoriaFontFamily = EuphoriaFontFamily.SYSTEM
) {
    val backgroundBrush: Brush
        get() = if (isAmoled) {
            Brush.verticalGradient(listOf(Color.Black, Color(0xFF050505)))
        } else {
            Brush.verticalGradient(
                listOf(
                    primaryBg,
                    secondaryBg,
                    primaryBg.copy(alpha = 0.95f)
                )
            )
        }
}

object BuiltInThemes {
    val MidnightPurple = EuphoriaThemeData(
        id = "midnight_purple",
        name = "Midnight Purple",
        primaryBg = Color(0xFF130826),
        secondaryBg = Color(0xFF240D4A),
        surfaceColor = Color(0xFF1E0D38),
        cardColor = Color(0xFF2A124E),
        accentColor = Color(0xFFC743FF),
        secondaryAccentColor = Color(0xFF8B2CF5),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFD6C8EC),
        textMuted = Color(0xFF8E7AA6),
        glowColor = Color(0x99C743FF),
        cardAlpha = 0.65f,
        cornerRadiusDp = 24f,
        isDark = true,
        playerThemeStyle = PlayerThemeStyle.CIRCULAR_VINYL,
        waveformStyle = WaveformStyle.NEON
    )

    val AmoledBlack = EuphoriaThemeData(
        id = "amoled_black",
        name = "AMOLED Black",
        primaryBg = Color(0xFF000000),
        secondaryBg = Color(0xFF050505),
        surfaceColor = Color(0xFF0D0D0D),
        cardColor = Color(0xFF151515),
        accentColor = Color(0xFFFF2A55),
        secondaryAccentColor = Color(0xFFFF5277),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFE0E0E0),
        textMuted = Color(0xFF757575),
        glowColor = Color(0x88FF2A55),
        cardAlpha = 0.85f,
        cornerRadiusDp = 20f,
        isDark = true,
        isAmoled = true,
        playerThemeStyle = PlayerThemeStyle.MINIMAL_AMOLED,
        waveformStyle = WaveformStyle.BAR
    )

    val CyberpunkNeon = EuphoriaThemeData(
        id = "cyberpunk_neon",
        name = "Cyberpunk Neon",
        primaryBg = Color(0xFF080B16),
        secondaryBg = Color(0xFF130A24),
        surfaceColor = Color(0xFF101728),
        cardColor = Color(0xFF172036),
        accentColor = Color(0xFF00F0FF),
        secondaryAccentColor = Color(0xFFFF0077),
        textPrimary = Color(0xFFF0F6FC),
        textSecondary = Color(0xFF70E0FF),
        textMuted = Color(0xFF4A6882),
        glowColor = Color(0x9900F0FF),
        cardAlpha = 0.7f,
        cornerRadiusDp = 18f,
        playerThemeStyle = PlayerThemeStyle.FUTURISTIC_PLAYER,
        waveformStyle = WaveformStyle.NEON
    )

    val OceanBlue = EuphoriaThemeData(
        id = "ocean_blue",
        name = "Ocean Blue",
        primaryBg = Color(0xFF051329),
        secondaryBg = Color(0xFF0B2545),
        surfaceColor = Color(0xFF0E2F56),
        cardColor = Color(0xFF133E6D),
        accentColor = Color(0xFF00B4D8),
        secondaryAccentColor = Color(0xFF90E0EF),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFCAF0F8),
        textMuted = Color(0xFF5B8CA3),
        glowColor = Color(0x8800B4D8),
        cardAlpha = 0.65f,
        cornerRadiusDp = 22f,
        playerThemeStyle = PlayerThemeStyle.GLASS_PLAYER,
        waveformStyle = WaveformStyle.SMOOTH
    )

    val EmeraldDreams = EuphoriaThemeData(
        id = "emerald_dreams",
        name = "Emerald Dreams",
        primaryBg = Color(0xFF041A14),
        secondaryBg = Color(0xFF092B21),
        surfaceColor = Color(0xFF0E382B),
        cardColor = Color(0xFF144D3B),
        accentColor = Color(0xFF10B981),
        secondaryAccentColor = Color(0xFF34D399),
        textPrimary = Color(0xFFF0FDF4),
        textSecondary = Color(0xFFA7F3D0),
        textMuted = Color(0xFF4F7A6A),
        glowColor = Color(0x8810B981),
        cardAlpha = 0.7f,
        cornerRadiusDp = 22f,
        playerThemeStyle = PlayerThemeStyle.NEON_WAVE,
        waveformStyle = WaveformStyle.SMOOTH
    )

    val Sunset = EuphoriaThemeData(
        id = "sunset",
        name = "Sunset",
        primaryBg = Color(0xFF1E091B),
        secondaryBg = Color(0xFF3B102A),
        surfaceColor = Color(0xFF4A1532),
        cardColor = Color(0xFF5C1A3E),
        accentColor = Color(0xFFFF5A36),
        secondaryAccentColor = Color(0xFFFF9E00),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFFFD8BE),
        textMuted = Color(0xFFA6647B),
        glowColor = Color(0x99FF5A36),
        cardAlpha = 0.65f,
        cornerRadiusDp = 24f,
        playerThemeStyle = PlayerThemeStyle.GRADIENT_PLAYER,
        waveformStyle = WaveformStyle.BAR
    )

    val Crimson = EuphoriaThemeData(
        id = "crimson",
        name = "Crimson",
        primaryBg = Color(0xFF1A0508),
        secondaryBg = Color(0xFF2E090F),
        surfaceColor = Color(0xFF3D0C15),
        cardColor = Color(0xFF52101C),
        accentColor = Color(0xFFE50914),
        secondaryAccentColor = Color(0xFFFF3B47),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFFFD0D5),
        textMuted = Color(0xFF9E5C64),
        glowColor = Color(0x88E50914),
        cardAlpha = 0.7f,
        cornerRadiusDp = 20f,
        playerThemeStyle = PlayerThemeStyle.CIRCULAR_VINYL,
        waveformStyle = WaveformStyle.BAR
    )

    val Arctic = EuphoriaThemeData(
        id = "arctic",
        name = "Arctic",
        primaryBg = Color(0xFF081420),
        secondaryBg = Color(0xFF0E2235),
        surfaceColor = Color(0xFF142E46),
        cardColor = Color(0xFF1B3D5C),
        accentColor = Color(0xFF70D6FF),
        secondaryAccentColor = Color(0xFFB8E8FF),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFE0F4FF),
        textMuted = Color(0xFF6B8FA8),
        glowColor = Color(0x8870D6FF),
        cardAlpha = 0.65f,
        cornerRadiusDp = 22f,
        playerThemeStyle = PlayerThemeStyle.GLASS_PLAYER,
        waveformStyle = WaveformStyle.THIN
    )

    val Lavender = EuphoriaThemeData(
        id = "lavender",
        name = "Lavender",
        primaryBg = Color(0xFF141026),
        secondaryBg = Color(0xFF221A40),
        surfaceColor = Color(0xFF2E2454),
        cardColor = Color(0xFF3B2F6B),
        accentColor = Color(0xFFB388FF),
        secondaryAccentColor = Color(0xFFD1C4E9),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFEDE7F6),
        textMuted = Color(0xFF8B80A8),
        glowColor = Color(0x88B388FF),
        cardAlpha = 0.65f,
        cornerRadiusDp = 24f,
        playerThemeStyle = PlayerThemeStyle.FLOATING_CONTROLS,
        waveformStyle = WaveformStyle.SMOOTH
    )

    val MinimalWhite = EuphoriaThemeData(
        id = "minimal_white",
        name = "Minimal White",
        primaryBg = Color(0xFFF7F8FA),
        secondaryBg = Color(0xFFEDF0F5),
        surfaceColor = Color(0xFFFFFFFF),
        cardColor = Color(0xFFFFFFFF),
        accentColor = Color(0xFF6366F1),
        secondaryAccentColor = Color(0xFF818CF8),
        textPrimary = Color(0xFF1E293B),
        textSecondary = Color(0xFF475569),
        textMuted = Color(0xFF94A3B8),
        glowColor = Color(0x446366F1),
        cardAlpha = 0.95f,
        cornerRadiusDp = 20f,
        isDark = false,
        playerThemeStyle = PlayerThemeStyle.MINIMAL_WHITE,
        waveformStyle = WaveformStyle.SMOOTH
    )

    val GlassAurora = EuphoriaThemeData(
        id = "glass_aurora",
        name = "Glass Aurora",
        primaryBg = Color(0xFF09141B),
        secondaryBg = Color(0xFF13222B),
        surfaceColor = Color(0xFF1B323E),
        cardColor = Color(0xFF213F4E),
        accentColor = Color(0xFF2DD4BF),
        secondaryAccentColor = Color(0xFF38BDF8),
        textPrimary = Color(0xFFF0FDFA),
        textSecondary = Color(0xFFCCFBF1),
        textMuted = Color(0xFF5E8B99),
        glowColor = Color(0x992DD4BF),
        cardAlpha = 0.55f,
        cornerRadiusDp = 26f,
        blurRadiusDp = 24f,
        playerThemeStyle = PlayerThemeStyle.GLASS_PLAYER,
        waveformStyle = WaveformStyle.NEON
    )

    val DeepSpace = EuphoriaThemeData(
        id = "deep_space",
        name = "Deep Space",
        primaryBg = Color(0xFF060714),
        secondaryBg = Color(0xFF0C1028),
        surfaceColor = Color(0xFF12193C),
        cardColor = Color(0xFF192250),
        accentColor = Color(0xFF818CF8),
        secondaryAccentColor = Color(0xFFC084FC),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFDDD6FE),
        textMuted = Color(0xFF63668E),
        glowColor = Color(0x88818CF8),
        cardAlpha = 0.7f,
        cornerRadiusDp = 24f,
        playerThemeStyle = PlayerThemeStyle.FUTURISTIC_PLAYER,
        waveformStyle = WaveformStyle.NEON
    )

    val Vintage = EuphoriaThemeData(
        id = "vintage",
        name = "Vintage",
        primaryBg = Color(0xFF1A1310),
        secondaryBg = Color(0xFF2C201A),
        surfaceColor = Color(0xFF3B2C24),
        cardColor = Color(0xFF4A372E),
        accentColor = Color(0xFFD4A373),
        secondaryAccentColor = Color(0xFFFAEDCD),
        textPrimary = Color(0xFFFEFAE0),
        textSecondary = Color(0xFFE9D8A6),
        textMuted = Color(0xFF9E8675),
        glowColor = Color(0x66D4A373),
        cardAlpha = 0.75f,
        cornerRadiusDp = 18f,
        playerThemeStyle = PlayerThemeStyle.CIRCULAR_VINYL,
        waveformStyle = WaveformStyle.MINIMAL
    )

    val Monochrome = EuphoriaThemeData(
        id = "monochrome",
        name = "Monochrome",
        primaryBg = Color(0xFF121212),
        secondaryBg = Color(0xFF1E1E1E),
        surfaceColor = Color(0xFF242424),
        cardColor = Color(0xFF2C2C2C),
        accentColor = Color(0xFFE0E0E0),
        secondaryAccentColor = Color(0xFFFFFFFF),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFCCCCCC),
        textMuted = Color(0xFF777777),
        glowColor = Color(0x44FFFFFF),
        cardAlpha = 0.8f,
        cornerRadiusDp = 20f,
        playerThemeStyle = PlayerThemeStyle.MINIMAL_AMOLED,
        waveformStyle = WaveformStyle.BAR
    )

    val MidnightTeal = EuphoriaThemeData(
        id = "midnight_teal",
        name = "Midnight Teal",
        primaryBg = Color(0xFF041618),
        secondaryBg = Color(0xFF072428),
        surfaceColor = Color(0xFF0B3338),
        cardColor = Color(0xFF10444B),
        accentColor = Color(0xFF14B8A6),
        secondaryAccentColor = Color(0xFF2DD4BF),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFCCFBF1),
        textMuted = Color(0xFF4A757B),
        glowColor = Color(0x8814B8A6),
        cardAlpha = 0.65f,
        cornerRadiusDp = 22f,
        playerThemeStyle = PlayerThemeStyle.NEON_WAVE,
        waveformStyle = WaveformStyle.SMOOTH
    )

    val all: List<EuphoriaThemeData> = listOf(
        MidnightPurple,
        AmoledBlack,
        CyberpunkNeon,
        OceanBlue,
        EmeraldDreams,
        Sunset,
        Crimson,
        Arctic,
        Lavender,
        MinimalWhite,
        GlassAurora,
        DeepSpace,
        Vintage,
        Monochrome,
        MidnightTeal
    )

    fun getById(id: String): EuphoriaThemeData {
        return all.firstOrNull { it.id == id } ?: MidnightPurple
    }
}
