package com.example.euphoria.data.repository

import android.content.Context
import android.net.Uri
import com.example.euphoria.data.local.AppDatabase
import com.example.euphoria.data.local.entity.FavoriteSongEntity
import com.example.euphoria.data.local.entity.PlaybackHistoryEntity
import com.example.euphoria.data.local.entity.PlaylistEntity
import com.example.euphoria.data.local.entity.PlaylistSongEntity
import com.example.euphoria.data.local.entity.SongMetadataOverrideEntity
import com.example.euphoria.data.local.entity.UserCustomThemeEntity
import com.example.euphoria.data.model.Album
import com.example.euphoria.data.model.Artist
import com.example.euphoria.data.model.Song
import com.example.euphoria.data.provider.DemoMusicProvider
import com.example.euphoria.data.provider.MediaScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val playlistDao = database.playlistDao()
    private val metadataDao = database.metadataOverrideDao()
    private val themeDao = database.themeDao()
    private val favoritesDao = database.favoritesDao()
    private val historyDao = database.historyDao()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val _rawSongs = MutableStateFlow<List<Song>>(emptyList())
    val rawSongs: StateFlow<List<Song>> = _rawSongs.asStateFlow()

    val metadataOverrides: Flow<List<SongMetadataOverrideEntity>> = metadataDao.getAllOverrides()
    val favoriteSongIds: Flow<List<Long>> = favoritesDao.getFavoriteSongIds()
    val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
    val customThemes: Flow<List<UserCustomThemeEntity>> = themeDao.getAllThemes()
    val recentHistorySongIds: Flow<List<Long>> = historyDao.getRecentlyPlayedSongIds()
    val mostPlayedSongIds: Flow<List<Long>> = historyDao.getMostPlayedSongIds()

    // Fully merged reactive songs flow with metadata overrides applied
    val songs: StateFlow<List<Song>> = combine(_rawSongs, metadataOverrides) { rawList, overrides ->
        val overrideMap = overrides.associateBy { it.songId }
        rawList.map { song ->
            val override = overrideMap[song.id]
            if (override != null) {
                song.copy(
                    title = override.title ?: song.title,
                    artist = override.artist ?: song.artist,
                    album = override.album ?: song.album,
                    albumArtist = override.albumArtist ?: song.albumArtist,
                    genre = override.genre ?: song.genre,
                    year = override.year ?: song.year,
                    trackNumber = override.trackNumber ?: song.trackNumber,
                    discNumber = override.discNumber ?: song.discNumber,
                    description = override.description ?: song.description,
                    albumArtUri = if (override.customArtworkUri != null) Uri.parse(override.customArtworkUri) else song.albumArtUri
                )
            } else {
                song
            }
        }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    val albums: StateFlow<List<Album>> = songs.combine(songs) { list, _ ->
        list.groupBy { it.album }.map { (albumName, songsInAlbum) ->
            val first = songsInAlbum.first()
            Album(
                id = first.id,
                name = albumName,
                artist = first.artist,
                songCount = songsInAlbum.size,
                artworkUri = songsInAlbum.firstOrNull { it.albumArtUri != null }?.albumArtUri,
                year = first.year
            )
        }.sortedBy { it.name }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    val artists: StateFlow<List<Artist>> = songs.combine(songs) { list, _ ->
        list.groupBy { it.artist }.map { (artistName, songsByArtist) ->
            val albumsCount = songsByArtist.map { it.album }.distinct().size
            Artist(
                name = artistName,
                songCount = songsByArtist.size,
                albumCount = albumsCount,
                artworkUri = songsByArtist.firstOrNull { it.albumArtUri != null }?.albumArtUri
            )
        }.sortedBy { it.name }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    init {
        rescanLibrary()
    }

    fun rescanLibrary() {
        repositoryScope.launch {
            val deviceSongs = MediaScanner.scanDeviceSongs(context)
            val demoSongs = DemoMusicProvider.getOrGenerateDemoSongs(context)
            val combined = if (deviceSongs.isEmpty()) {
                demoSongs
            } else {
                // If device songs exist, include both so users always have rich content
                deviceSongs + demoSongs
            }
            _rawSongs.value = combined
        }
    }

    suspend fun saveMetadataOverride(override: SongMetadataOverrideEntity) = withContext(Dispatchers.IO) {
        metadataDao.insertOrUpdateOverride(override)
    }

    suspend fun restoreOriginalMetadata(songId: Long) = withContext(Dispatchers.IO) {
        metadataDao.deleteOverride(songId)
    }

    suspend fun toggleFavorite(songId: Long, isFav: Boolean) = withContext(Dispatchers.IO) {
        if (isFav) {
            favoritesDao.removeFavorite(songId)
        } else {
            favoritesDao.addFavorite(FavoriteSongEntity(songId))
        }
    }

    fun isSongFavorite(songId: Long): Flow<Boolean> = favoritesDao.isFavorite(songId)

    suspend fun recordSongPlay(songId: Long) = withContext(Dispatchers.IO) {
        historyDao.recordPlay(PlaybackHistoryEntity(songId = songId))
    }

    // Playlists
    suspend fun createPlaylist(name: String, description: String = "", coverUri: String? = null): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(PlaylistEntity(name = name, description = description, customCoverUri = coverUri))
    }

    suspend fun updatePlaylist(playlist: PlaylistEntity) = withContext(Dispatchers.IO) {
        playlistDao.updatePlaylist(playlist)
    }

    suspend fun deletePlaylist(id: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(id)
    }

    fun getPlaylistSongIds(playlistId: Long): Flow<List<Long>> = playlistDao.getSongIdsForPlaylist(playlistId)

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long, sortOrder: Int) = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylistSong(PlaylistSongEntity(playlistId = playlistId, songId = songId, sortOrder = sortOrder))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    suspend fun updatePlaylistOrder(playlistId: Long, songIds: List<Long>) = withContext(Dispatchers.IO) {
        playlistDao.updatePlaylistSongOrder(playlistId, songIds)
    }

    // Custom themes
    suspend fun saveCustomTheme(theme: UserCustomThemeEntity): Long = withContext(Dispatchers.IO) {
        themeDao.insertTheme(theme)
    }

    suspend fun deleteCustomTheme(id: Long) = withContext(Dispatchers.IO) {
        themeDao.deleteTheme(id)
    }
}
