package org.unstabledev.pomegranate

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlin.jvm.java

class AudioPlaybackService : Service() {
    companion object {
        private const val CHANNEL_ID = "audio_playback"
        private const val NOTIF_ID = 4242
        private const val ACTION_PLAY_PAUSE = "org.unstabledev.pomegranate.ACTION_PLAY_PAUSE"
        private const val ACTION_STOP = "org.unstabledev.pomegranate.ACTION_STOP"
        private const val ACTION_UPDATE_STATE = "org.unstabledev.pomegranate.ACTION_UPDATE_STATE"
        private const val EXTRA_IS_PLAYING = "isPlaying"

        fun start(context: Context) {
            val intent = Intent(context, AudioPlaybackService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun updateState(context: Context, isPlaying: Boolean) {
            val intent = Intent(context, AudioPlaybackService::class.java).apply {
                action = ACTION_UPDATE_STATE
                putExtra(EXTRA_IS_PLAYING, isPlaying)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AudioPlaybackService::class.java))
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIF_ID, buildNotification(isPlaying = true))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> {
                AudioPlaybackManager.togglePlayPause()
                startForeground(NOTIF_ID, buildNotification(AudioPlaybackManager.isPlaying()))
            }
            ACTION_STOP -> {
                AudioPlaybackManager.stopAndRelease()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_UPDATE_STATE -> {
                val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false)
                startForeground(NOTIF_ID, buildNotification(isPlaying))
            }
        }
        return START_NOT_STICKY
    }

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Audio playback",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    private fun pendingServiceIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, AudioPlaybackService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            this, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildNotification(isPlaying: Boolean): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val contentPending = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomegranate")
            .setContentText(if (isPlaying) "Аудио воспроизводится" else "Аудио приостоновлено")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(contentPending)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Пауза" else "Воспроизвести",
                pendingServiceIntent(ACTION_PLAY_PAUSE, 1)
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Стоп",
                pendingServiceIntent(ACTION_STOP, 2)
            )
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .build()
    }
}