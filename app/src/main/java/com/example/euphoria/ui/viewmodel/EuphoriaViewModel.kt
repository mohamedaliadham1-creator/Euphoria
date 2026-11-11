package com.example.euphoria.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.euphoria.data.local.entity.PlaylistEntity
import com.example.euphoria.data.local.entity.SongMetadataOverrideEntity
import com.example.euphoria.data.local.entity.UserCustomThemeEntity
import com.example.euphoria.data.model.Album
import com.example.euphoria.data.model.Artist
import com.example.euphoria.data.model.Song
import com.example.euphoria.player.AudioFocusMode
import com.example.euphoria.player.EuphoriaPlayerManager
import com.example.euphoria.player.FadeSettings
import com.example.euphoria.player.RepeatMode
import com.example.euphoria.player.SleepTimerState
import com.example.euphoria.theme.BuiltInThemes
import com.example.euphoria.theme.EuphoriaFontFamily
import com.example.euphoria.theme.EuphoriaThemeData
import com.example.euphoria.theme.PlayerThemeStyle
import com.example.euphoria.theme.ThemeMode
import com.example.euphoria.theme.WallpaperOverlayMode
import com.example.euphoria.theme.WallpaperPaletteExtractor
import com.example.euphoria.theme.WaveformStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EuphoriaViewModel(application: Application) : AndroidViewModel(application) {

    private val playerManager = EuphoriaPlayerManager.getInstance(application)
    val repository = playerManager.repository

    // Theme state
    private val _currentTheme = MutableStateFlow(BuiltInThemes.MidnightPurple)
    val currentTheme: StateFlow<EuphoriaThemeData> = _currentTheme.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Library & persistence
    val songs: StateFlow<List<Song>> = repository.songs
    val albums: StateFlow<List<Album>> = repository.albums
    val artists: StateFlow<List<Artist>> = repository.artists
    val playlists: StateFlow<List<PlaylistEntity>> = repository.allPlaylists.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val favoriteSongIds: StateFlow<List<Long>> = repository.favoriteSongIds.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val customThemes: StateFlow<List<UserCustomThemeEntity>> = repository.customThemes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val recentHistorySongIds: StateFlow<List<Long>> = repository.recentHistorySongIds.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Player state delegated from PlayerManager
    val currentSong: StateFlow<Song?> = playerManager.currentSong
    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying
    val currentPositionMs: StateFlow<Long> = playerManager.currentPositionMs
    val durationMs: StateFlow<Long> = playerManager.durationMs
    val queue: StateFlow<List<Song>> = playerManager.queue
    val currentQueueIndex: StateFlow<Int> = playerManager.currentQueueIndex
    val isShuffle: StateFlow<Boolean> = playerManager.isShuffle
    val repeatMode: StateFlow<RepeatMode> = playerManager.repeatMode
    val playbackSpeed: StateFlow<Float> = playerManager.playbackSpeed
    val volume: StateFlow<Float> = playerManager.volume
    val audioFocusMode: StateFlow<AudioFocusMode> = playerManager.audioFocusMode
    val fadeSettings: StateFlow<FadeSettings> = playerManager.fadeSettings
    val sleepTimerState: StateFlow<SleepTimerState> = playerManager.sleepTimerState
    val waveformAmplitudes: StateFlow<List<Float>> = playerManager.waveformAmplitudes

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Theme controls
    fun applyTheme(theme: EuphoriaThemeData) {
        _currentTheme.value = theme
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        when (mode) {
            ThemeMode.AMOLED -> {
                _currentTheme.value = _currentTheme.value.copy(
                    isAmoled = true,
                    isDark = true,
                    primaryBg = Color.Black,
                    secondaryBg = Color(0xFF060606)
                )
            }
            ThemeMode.LIGHT -> {
                _currentTheme.value = BuiltInThemes.MinimalWhite
            }
            ThemeMode.DARK -> {
                if (_currentTheme.value.isAmoled || !_currentTheme.value.isDark) {
                    _currentTheme.value = BuiltInThemes.MidnightPurple
                }
            }
            ThemeMode.FOLLOW_SYSTEM -> {
                _currentTheme.value = BuiltInThemes.MidnightPurple
            }
        }
    }

    fun applyWallpaperTheme(
        uri: Uri,
        overlayMode: WallpaperOverlayMode = WallpaperOverlayMode.GRADIENT,
        brightness: Float = 0.5f,
        blur: Float = 8f,
        alpha: Float = 0.75f,
        saturation: Float = 1.0f
    ) {
        viewModelScope.launch {
            val palette = WallpaperPaletteExtractor.extractFromUri(getApplication(), uri)
            val updated = _currentTheme.value.copy(
                id = "custom_wallpaper_${System.currentTimeMillis()}",
                name = "My Wallpaper Theme",
                primaryBg = palette.primaryBg,
                secondaryBg = palette.secondaryBg,
                surfaceColor = palette.surface,
                cardColor = palette.card,
                accentColor = palette.accent,
                secondaryAccentColor = palette.secondaryAccent,
                glowColor = palette.glow,
                textPrimary = palette.textPrimary,
                textSecondary = palette.textSecondary,
                wallpaperUri = uri.toString(),
                wallpaperOverlayMode = overlayMode,
                wallpaperBrightness = brightness,
                wallpaperBlur = blur,
                wallpaperAlpha = alpha,
                wallpaperSaturation = saturation
            )
            _currentTheme.value = updated
        }
    }

    fun updatePlayerThemeStyle(style: PlayerThemeStyle) {
        _currentTheme.value = _currentTheme.value.copy(playerThemeStyle = style)
    }

    fun updateWaveformStyle(style: WaveformStyle) {
        _currentTheme.value = _currentTheme.value.copy(waveformStyle = style)
    }

    fun updateFontFamily(font: EuphoriaFontFamily) {
        _currentTheme.value = _currentTheme.value.copy(fontStyle = font)
    }

    fun saveCurrentThemeAsCustom(name: String) {
        viewModelScope.launch {
            val theme = _currentTheme.value
            val entity = UserCustomThemeEntity(
                name = name,
                primaryBgHex = "#%08X".format(theme.primaryBg.value.toLong()),
                secondaryBgHex = "#%08X".format(theme.secondaryBg.value.toLong()),
                accentHex = "#%08X".format(theme.accentColor.value.toLong()),
                secondaryAccentHex = "#%08X".format(theme.secondaryAccentColor.value.toLong()),
                cardBgHex = "#%08X".format(theme.cardColor.value.toLong()),
                textPrimaryHex = "#%08X".format(theme.textPrimary.value.toLong()),
                textSecondaryHex = "#%08X".format(theme.textSecondary.value.toLong()),
                cardAlpha = theme.cardAlpha,
                blurRadiusDp = theme.blurRadiusDp,
                cornerRadiusDp = theme.cornerRadiusDp,
                wallpaperUri = theme.wallpaperUri,
                wallpaperOverlayMode = theme.wallpaperOverlayMode.name,
                wallpaperBrightness = theme.wallpaperBrightness,
                wallpaperBlur = theme.wallpaperBlur,
                wallpaperAlpha = theme.wallpaperAlpha,
                wallpaperSaturation = theme.wallpaperSaturation,
                playerLayout = theme.playerThemeStyle.name,
                waveformStyle = theme.waveformStyle.name,
                isBuiltIn = false
            )
            repository.saveCustomTheme(entity)
        }
    }

    fun deleteCustomTheme(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomTheme(id)
        }
    }

    // Playback delegation
    fun playSong(song: Song, queueList: List<Song>? = null) = playerManager.playSong(song, queueList)
    fun togglePlayPause() = playerManager.togglePlayPause()
    fun playNext() = playerManager.playNext()
    fun playPrevious() = playerManager.playPrevious()
    fun seekTo(positionMs: Long) = playerManager.seekTo(positionMs)
    fun setPlaybackSpeed(speed: Float) = playerManager.setPlaybackSpeed(speed)
    fun setVolume(vol: Float) = playerManager.setVolume(vol)
    fun toggleShuffle() = playerManager.toggleShuffle()
    fun cycleRepeatMode() = playerManager.cycleRepeatMode()
    fun setFadeSettings(settings: FadeSettings) = playerManager.setFadeSettings(settings)
    fun setAudioFocusMode(mode: AudioFocusMode) = playerManager.setAudioFocusMode(mode)
    fun startSleepTimer(minutes: Int, fadeOut: Boolean = true, pauseWhenPaused: Boolean = false) =
        playerManager.startSleepTimer(minutes, fadeOut, pauseWhenPaused)
    fun cancelSleepTimer() = playerManager.cancelSleepTimer()

    // Queue
    fun addToQueue(song: Song) = playerManager.addToQueue(song)
    fun playNextInQueue(song: Song) = playerManager.playNextInQueue(song)
    fun removeFromQueue(index: Int) = playerManager.removeFromQueue(index)
    fun reorderQueue(from: Int, to: Int) = playerManager.reorderQueue(from, to)
    fun clearQueue() = playerManager.clearQueue()
    fun shuffleQueue() = playerManager.shuffleQueue()

    // Favorites
    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            val isFav = favoriteSongIds.value.contains(songId)
            repository.toggleFavorite(songId, isFav)
        }
    }

    // Playlists
    fun createPlaylist(name: String, description: String = "", coverUri: String? = null) {
        viewModelScope.launch {
            repository.createPlaylist(name, description, coverUri)
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(id)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId, 0)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun saveQueueAsPlaylist(playlistName: String) {
        viewModelScope.launch {
            val currentQ = queue.value
            if (currentQ.isNotEmpty()) {
                val pid = repository.createPlaylist(playlistName, "Generated from playback queue")
                currentQ.forEachIndexed { index, song ->
                    repository.addSongToPlaylist(pid, song.id, index)
                }
            }
        }
    }

    // Metadata Editing
    fun saveMetadataOverride(
        songId: Long,
        title: String,
        artist: String,
        album: String,
        genre: String,
        year: Int,
        trackNumber: Int,
        description: String,
        customArtworkUri: String?
    ) {
        viewModelScope.launch {
            val override = SongMetadataOverrideEntity(
                songId = songId,
                title = title,
                artist = artist,
                album = album,
                genre = genre,
                year = year,
                trackNumber = trackNumber,
                description = description,
                customArtworkUri = customArtworkUri
            )
            repository.saveMetadataOverride(override)
        }
    }

    fun restoreOriginalMetadata(songId: Long) {
        viewModelScope.launch {
            repository.restoreOriginalMetadata(songId)
        }
    }

    fun rescanLibrary() {
        repository.rescanLibrary()
    }
}
