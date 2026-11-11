package com.example.euphoria.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.euphoria.MainActivity
import com.example.R
import com.example.euphoria.data.model.Song
import com.example.euphoria.player.EuphoriaPlayerManager

object WidgetUpdateHelper {
    const val ACTION_PLAY_PAUSE = "com.example.euphoria.widget.ACTION_PLAY_PAUSE"
    const val ACTION_NEXT = "com.example.euphoria.widget.ACTION_NEXT"
    const val ACTION_PREV = "com.example.euphoria.widget.ACTION_PREV"
    const val ACTION_FAV = "com.example.euphoria.widget.ACTION_FAV"
    const val ACTION_SHUFFLE = "com.example.euphoria.widget.ACTION_SHUFFLE"
    const val ACTION_REPEAT = "com.example.euphoria.widget.ACTION_REPEAT"

    fun updateAllWidgets(context: Context, song: Song?, isPlaying: Boolean, progressPercent: Int = 0) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        // 1. Compact Widget
        val compactComponent = ComponentName(context, EuphoriaCompactWidgetProvider::class.java)
        val compactIds = appWidgetManager.getAppWidgetIds(compactComponent)
        for (id in compactIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_compact_layout)
            bindBaseViews(context, views, song, isPlaying)
            setupPendingIntents(context, views, EuphoriaCompactWidgetProvider::class.java)
            appWidgetManager.updateAppWidget(id, views)
        }

        // 2. Premium Widget
        val premiumComponent = ComponentName(context, EuphoriaPremiumWidgetProvider::class.java)
        val premiumIds = appWidgetManager.getAppWidgetIds(premiumComponent)
        for (id in premiumIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_premium_layout)
            bindBaseViews(context, views, song, isPlaying)
            views.setProgressBar(R.id.widget_progress_bar, 100, progressPercent, false)
            setupPendingIntents(context, views, EuphoriaPremiumWidgetProvider::class.java)
            appWidgetManager.updateAppWidget(id, views)
        }

        // 3. Minimal Widget
        val minimalComponent = ComponentName(context, EuphoriaMinimalWidgetProvider::class.java)
        val minimalIds = appWidgetManager.getAppWidgetIds(minimalComponent)
        for (id in minimalIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_minimal_layout)
            bindBaseViews(context, views, song, isPlaying)
            setupPendingIntents(context, views, EuphoriaMinimalWidgetProvider::class.java)
            appWidgetManager.updateAppWidget(id, views)
        }

        // 4. Artwork Widget
        val artworkComponent = ComponentName(context, EuphoriaArtworkWidgetProvider::class.java)
        val artworkIds = appWidgetManager.getAppWidgetIds(artworkComponent)
        for (id in artworkIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_artwork_layout)
            bindBaseViews(context, views, song, isPlaying)
            setupPendingIntents(context, views, EuphoriaArtworkWidgetProvider::class.java)
            appWidgetManager.updateAppWidget(id, views)
        }

        // 5. Extended Widget
        val extendedComponent = ComponentName(context, EuphoriaExtendedWidgetProvider::class.java)
        val extendedIds = appWidgetManager.getAppWidgetIds(extendedComponent)
        for (id in extendedIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_extended_layout)
            bindBaseViews(context, views, song, isPlaying)
            views.setProgressBar(R.id.widget_progress_bar, 100, progressPercent, false)
            setupPendingIntents(context, views, EuphoriaExtendedWidgetProvider::class.java)
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    private fun bindBaseViews(context: Context, views: RemoteViews, song: Song?, isPlaying: Boolean) {
        val title = song?.title ?: "EUPHORIA Player"
        val artist = song?.artist ?: "Made by Adham Mohamed Ali"
        views.setTextViewText(R.id.widget_track_title, title)
        views.setTextViewText(R.id.widget_artist_name, artist)

        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        views.setImageViewResource(R.id.widget_btn_play_pause, playPauseIcon)

        // Launch app on root container click
        val openAppIntent = Intent(context, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_album_art, openPendingIntent)
    }

    private fun setupPendingIntents(context: Context, views: RemoteViews, providerClass: Class<*>) {
        fun createActionPendingIntent(action: String, requestCode: Int): PendingIntent {
            val intent = Intent(context, providerClass).apply { this.action = action }
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        views.setOnClickPendingIntent(R.id.widget_btn_play_pause, createActionPendingIntent(ACTION_PLAY_PAUSE, 101))
        views.setOnClickPendingIntent(R.id.widget_btn_next, createActionPendingIntent(ACTION_NEXT, 102))
        views.setOnClickPendingIntent(R.id.widget_btn_prev, createActionPendingIntent(ACTION_PREV, 103))
        views.setOnClickPendingIntent(R.id.widget_btn_fav, createActionPendingIntent(ACTION_FAV, 104))
        views.setOnClickPendingIntent(R.id.widget_btn_shuffle, createActionPendingIntent(ACTION_SHUFFLE, 105))
        views.setOnClickPendingIntent(R.id.widget_btn_repeat, createActionPendingIntent(ACTION_REPEAT, 106))
    }

    fun handleBroadcastAction(context: Context, action: String?) {
        val player = EuphoriaPlayerManager.getInstance(context)
        when (action) {
            ACTION_PLAY_PAUSE -> player.togglePlayPause()
            ACTION_NEXT -> player.playNext()
            ACTION_PREV -> player.playPrevious()
            ACTION_FAV -> player.toggleCurrentSongFavorite()
            ACTION_SHUFFLE -> player.toggleShuffle()
            ACTION_REPEAT -> player.cycleRepeatMode()
        }
    }
}

class EuphoriaCompactWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val player = EuphoriaPlayerManager.getInstance(context)
        WidgetUpdateHelper.updateAllWidgets(context, player.currentSong.value, player.isPlaying.value)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        WidgetUpdateHelper.handleBroadcastAction(context, intent.action)
    }
}

class EuphoriaPremiumWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val player = EuphoriaPlayerManager.getInstance(context)
        WidgetUpdateHelper.updateAllWidgets(context, player.currentSong.value, player.isPlaying.value)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        WidgetUpdateHelper.handleBroadcastAction(context, intent.action)
    }
}

class EuphoriaMinimalWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val player = EuphoriaPlayerManager.getInstance(context)
        WidgetUpdateHelper.updateAllWidgets(context, player.currentSong.value, player.isPlaying.value)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        WidgetUpdateHelper.handleBroadcastAction(context, intent.action)
    }
}

class EuphoriaArtworkWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val player = EuphoriaPlayerManager.getInstance(context)
        WidgetUpdateHelper.updateAllWidgets(context, player.currentSong.value, player.isPlaying.value)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        WidgetUpdateHelper.handleBroadcastAction(context, intent.action)
    }
}

class EuphoriaExtendedWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val player = EuphoriaPlayerManager.getInstance(context)
        WidgetUpdateHelper.updateAllWidgets(context, player.currentSong.value, player.isPlaying.value)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        WidgetUpdateHelper.handleBroadcastAction(context, intent.action)
    }
}
