package com.example.euphoria.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val description: String = "",
    val customCoverUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"]
)
data class PlaylistSongEntity(
    val playlistId: Long,
    val songId: Long,
    val sortOrder: Int,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "metadata_overrides")
data class SongMetadataOverrideEntity(
    @PrimaryKey
    val songId: Long,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val albumArtist: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val description: String? = null,
    val customArtworkUri: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_custom_themes")
data class UserCustomThemeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val primaryBgHex: String,
    val secondaryBgHex: String,
    val accentHex: String,
    val secondaryAccentHex: String,
    val cardBgHex: String,
    val textPrimaryHex: String,
    val textSecondaryHex: String,
    val cardAlpha: Float = 0.65f,
    val blurRadiusDp: Float = 16f,
    val cornerRadiusDp: Float = 20f,
    val wallpaperUri: String? = null,
    val wallpaperOverlayMode: String = "GRADIENT", // FULL, BLURRED, DARK, GRADIENT, NOW_PLAYING_ONLY
    val wallpaperBrightness: Float = 0.5f,
    val wallpaperBlur: Float = 8f,
    val wallpaperAlpha: Float = 0.8f,
    val wallpaperSaturation: Float = 1.0f,
    val playerLayout: String = "CIRCULAR_VINYL", // CIRCULAR_VINYL, GLASS_PLAYER, FULL_ARTWORK, NEON_WAVE, MINIMAL_AMOLED
    val waveformStyle: String = "NEON", // THIN, NEON, SMOOTH, BAR, MINIMAL
    val waveformColorHex: String = accentHex,
    val isBuiltIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey
    val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val songId: Long,
    val playedAt: Long = System.currentTimeMillis()
)
