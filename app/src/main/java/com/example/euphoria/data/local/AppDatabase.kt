package com.example.euphoria.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.euphoria.data.local.dao.FavoritesDao
import com.example.euphoria.data.local.dao.HistoryDao
import com.example.euphoria.data.local.dao.MetadataOverrideDao
import com.example.euphoria.data.local.dao.PlaylistDao
import com.example.euphoria.data.local.dao.ThemeDao
import com.example.euphoria.data.local.entity.FavoriteSongEntity
import com.example.euphoria.data.local.entity.PlaybackHistoryEntity
import com.example.euphoria.data.local.entity.PlaylistEntity
import com.example.euphoria.data.local.entity.PlaylistSongEntity
import com.example.euphoria.data.local.entity.SongMetadataOverrideEntity
import com.example.euphoria.data.local.entity.UserCustomThemeEntity

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        SongMetadataOverrideEntity::class,
        UserCustomThemeEntity::class,
        FavoriteSongEntity::class,
        PlaybackHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun metadataOverrideDao(): MetadataOverrideDao
    abstract fun themeDao(): ThemeDao
    abstract fun favoritesDao(): FavoritesDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "euphoria_music.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
