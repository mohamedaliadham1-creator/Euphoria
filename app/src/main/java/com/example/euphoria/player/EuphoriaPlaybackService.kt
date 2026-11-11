package com.example.euphoria.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.euphoria.MainActivity
import com.example.euphoria.data.model.Song

class EuphoriaPlaybackService : Service() {

    private var mediaSession: MediaSession? = null

    companion object {
        const val CHANNEL_ID = "euphoria_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.example.euphoria.service.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.euphoria.service.ACTION_PAUSE"
        const val ACTION_NEXT = "com.example.euphoria.service.ACTION_NEXT"
        const val ACTION_PREV = "com.example.euphoria.service.ACTION_PREV"
        const val ACTION_STOP = "com.example.euphoria.service.ACTION_STOP"

        fun startService(context: Context) {
            val intent = Intent(context, EuphoriaPlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initMediaSession()
    }

    private fun initMediaSession() {
        mediaSession = MediaSession(this, "EuphoriaSession").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    EuphoriaPlayerManager.getInstance(applicationContext).play()
                }

                override fun onPause() {
                    EuphoriaPlayerManager.getInstance(applicationContext).pause()
                }

                override fun onSkipToNext() {
                    EuphoriaPlayerManager.getInstance(applicationContext).playNext()
                }

                override fun onSkipToPrevious() {
                    EuphoriaPlayerManager.getInstance(applicationContext).playPrevious()
                }

                override fun onStop() {
                    EuphoriaPlayerManager.getInstance(applicationContext).pause()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            })
            isActive = true
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "EUPHORIA Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground playback notification with controls"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val player = EuphoriaPlayerManager.getInstance(applicationContext)
        when (intent?.action) {
            ACTION_PLAY -> player.play()
            ACTION_PAUSE -> player.pause()
            ACTION_NEXT -> player.playNext()
            ACTION_PREV -> player.playPrevious()
            ACTION_STOP -> {
                player.pause()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val song = player.currentSong.value
        val isPlaying = player.isPlaying.value
        val notification = buildNotification(song, isPlaying)
        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    fun updateNotification(song: Song?, isPlaying: Boolean) {
        val notification = buildNotification(song, isPlaying)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)

        // Update MediaSession playback state
        val state = if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
        mediaSession?.setPlaybackState(
            PlaybackState.Builder()
                .setActions(
                    PlaybackState.ACTION_PLAY or
                            PlaybackState.ACTION_PAUSE or
                            PlaybackState.ACTION_SKIP_TO_NEXT or
                            PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackState.ACTION_STOP
                )
                .setState(state, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .build()
        )
    }

    private fun buildNotification(song: Song?, isPlaying: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        fun createServiceActionPendingIntent(action: String, code: Int): PendingIntent {
            val intent = Intent(this, EuphoriaPlaybackService::class.java).apply { this.action = action }
            return PendingIntent.getService(
                this,
                code,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_pause,
                "Pause",
                createServiceActionPendingIntent(ACTION_PAUSE, 201)
            ).build()
        } else {
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_play,
                "Play",
                createServiceActionPendingIntent(ACTION_PLAY, 202)
            ).build()
        }

        val prevAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_media_previous,
            "Previous",
            createServiceActionPendingIntent(ACTION_PREV, 203)
        ).build()

        val nextAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_media_next,
            "Next",
            createServiceActionPendingIntent(ACTION_NEXT, 204)
        ).build()

        val stopAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_delete,
            "Stop",
            createServiceActionPendingIntent(ACTION_STOP, 205)
        ).build()

        val title = song?.title ?: "EUPHORIA Player"
        val subtitle = "${song?.artist ?: "Made by Adham Mohamed Ali"} • ${song?.album ?: "Offline"}"

        val iconBitmap = try {
            BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_foreground)
        } catch (e: Exception) {
            null
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setLargeIcon(iconBitmap)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setContentIntent(openPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .addAction(stopAction)

        return builder.build()
    }

    override fun onDestroy() {
        mediaSession?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
