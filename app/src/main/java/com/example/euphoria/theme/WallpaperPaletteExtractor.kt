package com.example.euphoria.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ExtractedThemePalette(
    val primaryBg: Color,
    val secondaryBg: Color,
    val surface: Color,
    val card: Color,
    val accent: Color,
    val secondaryAccent: Color,
    val glow: Color,
    val textPrimary: Color,
    val textSecondary: Color
)

object WallpaperPaletteExtractor {

    suspend fun extractFromUri(context: Context, uri: Uri): ExtractedThemePalette = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 4 // Downsample for fast analysis
                }
                val bitmap = BitmapFactory.decodeStream(stream, null, options)
                if (bitmap != null) {
                    return@withContext extractFromBitmap(bitmap)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Fallback default purple neon palette
        ExtractedThemePalette(
            primaryBg = Color(0xFF130826),
            secondaryBg = Color(0xFF240D4A),
            surface = Color(0xFF1E0D38),
            card = Color(0xFF2A124E),
            accent = Color(0xFFC743FF),
            secondaryAccent = Color(0xFF8B2CF5),
            glow = Color(0x99C743FF),
            textPrimary = Color.White,
            textSecondary = Color(0xFFD6C8EC)
        )
    }

    fun extractFromBitmap(bitmap: Bitmap): ExtractedThemePalette {
        val width = bitmap.width
        val height = bitmap.height
        val step = 10
        var totalR = 0L
        var totalG = 0L
        var totalB = 0L
        var count = 0L

        var maxVibrance = -1f
        var vibrantColor = android.graphics.Color.MAGENTA

        for (x in 0 until width step step) {
            for (y in 0 until height step step) {
                val pixel = bitmap.getPixel(x, y)
                val r = android.graphics.Color.red(pixel)
                val g = android.graphics.Color.green(pixel)
                val b = android.graphics.Color.blue(pixel)

                totalR += r
                totalG += g
                totalB += b
                count++

                // Saturation/vibrancy calculation
                val hsv = FloatArray(3)
                android.graphics.Color.colorToHSV(pixel, hsv)
                val sat = hsv[1]
                val value = hsv[2]
                if (sat > 0.4f && value > 0.3f && value < 0.95f) {
                    val score = sat * value
                    if (score > maxVibrance) {
                        maxVibrance = score
                        vibrantColor = pixel
                    }
                }
            }
        }

        val avgR = if (count > 0) (totalR / count).toInt() else 20
        val avgG = if (count > 0) (totalG / count).toInt() else 10
        val avgB = if (count > 0) (totalB / count).toInt() else 35

        // Create dark ambient base from average, toned down for high readability
        val darkBgR = (avgR * 0.25).toInt().coerceIn(10, 35)
        val darkBgG = (avgG * 0.25).toInt().coerceIn(8, 30)
        val darkBgB = (avgB * 0.25).toInt().coerceIn(15, 45)

        val secBgR = (avgR * 0.45).toInt().coerceIn(18, 55)
        val secBgG = (avgG * 0.45).toInt().coerceIn(14, 50)
        val secBgB = (avgB * 0.45).toInt().coerceIn(24, 75)

        val cardR = (avgR * 0.6).toInt().coerceIn(25, 75)
        val cardG = (avgG * 0.6).toInt().coerceIn(20, 70)
        val cardB = (avgB * 0.6).toInt().coerceIn(35, 95)

        val vibR = android.graphics.Color.red(vibrantColor)
        val vibG = android.graphics.Color.green(vibrantColor)
        val vibB = android.graphics.Color.blue(vibrantColor)

        val accent = Color(vibR, vibG, vibB)
        val secAccent = Color(
            (vibR * 0.8f + 50).toInt().coerceIn(0, 255),
            (vibG * 0.8f + 50).toInt().coerceIn(0, 255),
            (vibB * 0.8f + 50).toInt().coerceIn(0, 255)
        )

        return ExtractedThemePalette(
            primaryBg = Color(darkBgR, darkBgG, darkBgB),
            secondaryBg = Color(secBgR, secBgG, secBgB),
            surface = Color(secBgR + 10, secBgG + 10, secBgB + 15),
            card = Color(cardR, cardG, cardB),
            accent = accent,
            secondaryAccent = secAccent,
            glow = accent.copy(alpha = 0.5f),
            textPrimary = Color.White,
            textSecondary = Color(0xFFE2E8F0)
        )
    }
}
