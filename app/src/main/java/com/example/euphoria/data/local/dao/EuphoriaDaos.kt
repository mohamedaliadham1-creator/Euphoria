package com.example.euphoria.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.euphoria.data.local.entity.FavoriteSongEntity
import com.example.euphoria.data.local.entity.PlaybackHistoryEntity
import com.example.euphoria.data.local.entity.PlaylistEntity
import com.example.euphoria.data.local.entity.PlaylistSongEntity
import com.example.euphoria.data.local.entity.SongMetadataOverrideEntity
import com.example.euphoria.data.local.entity.UserCustomThemeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY isPinned DESC, createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("SELECT songId FROM playlist_songs WHERE playlistId = :playlistId ORDER BY sortOrder ASC")
    fun getSongIdsForPlaylist(playlistId: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSong(item: PlaylistSongEntity)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearPlaylistSongs(playlistId: Long)

    @Transaction
    suspend fun updatePlaylistSongOrder(playlistId: Long, songIds: List<Long>) {
        clearPlaylistSongs(playlistId)
        songIds.forEachIndexed { index, songId ->
            insertPlaylistSong(PlaylistSongEntity(playlistId = playlistId, songId = songId, sortOrder = index))
        }
    }
}

@Dao
interface MetadataOverrideDao {
    @Query("SELECT * FROM metadata_overrides")
    fun getAllOverrides(): Flow<List<SongMetadataOverrideEntity>>

    @Query("SELECT * FROM metadata_overrides WHERE songId = :songId")
    suspend fun getOverrideForSong(songId: Long): SongMetadataOverrideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOverride(override: SongMetadataOverrideEntity)

    @Query("DELETE FROM metadata_overrides WHERE songId = :songId")
    suspend fun deleteOverride(songId: Long)

    @Query("DELETE FROM metadata_overrides")
    suspend fun clearAllOverrides()
}

@Dao
interface ThemeDao {
    @Query("SELECT * FROM user_custom_themes ORDER BY createdAt DESC")
    fun getAllThemes(): Flow<List<UserCustomThemeEntity>>

    @Query("SELECT * FROM user_custom_themes WHERE id = :id")
    suspend fun getThemeById(id: Long): UserCustomThemeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTheme(theme: UserCustomThemeEntity): Long

    @Update
    suspend fun updateTheme(theme: UserCustomThemeEntity)

    @Query("DELETE FROM user_custom_themes WHERE id = :id")
    suspend fun deleteTheme(id: Long)
}

@Dao
interface FavoritesDao {
    @Query("SELECT songId FROM favorite_songs ORDER BY addedAt DESC")
    fun getFavoriteSongIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE songId = :songId)")
    fun isFavorite(songId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteSongEntity)

    @Query("DELETE FROM favorite_songs WHERE songId = :songId")
    suspend fun removeFavorite(songId: Long)
}

@Dao
interface HistoryDao {
    @Query("SELECT songId FROM playback_history ORDER BY playedAt DESC LIMIT 100")
    fun getRecentlyPlayedSongIds(): Flow<List<Long>>

    @Query("SELECT songId FROM playback_history GROUP BY songId ORDER BY COUNT(id) DESC LIMIT 50")
    fun getMostPlayedSongIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordPlay(entry: PlaybackHistoryEntity)

    @Query("DELETE FROM playback_history")
    suspend fun clearHistory()
}
