package org.unstabledev.pomegranate

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

@SuppressLint("StaticFieldLeak")
object AudioPlaybackManager {
    lateinit var context: Context
    private var mediaPlayer: MediaPlayer? = null
    var currentPath: String? = null
        private set

    private var completionListener: (() -> Unit)? = null

    var mediaSession: MediaSessionCompat? = null
        private set

    private val handler = Handler(Looper.getMainLooper())

    private val positionUpdateRunnable = object : Runnable {
        override fun run() {
            if (mediaPlayer?.isPlaying == true) {
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, getCurrentPosition())
                handler.postDelayed(this, 1000)
            }
        }
    }

    fun initMediaSession() {
        if (mediaSession != null) return

        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post { initMediaSession() }
            return
        }

        val mediaButtonReceiver = ComponentName(context, AudioMediaButtonReceiver::class.java)
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            component = mediaButtonReceiver
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            mediaButtonIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val sessionIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val sessionPendingIntent = PendingIntent.getActivity(
            context, 0, sessionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSessionCompat(context, "AudioPlayback", mediaButtonReceiver, pendingIntent).apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    currentPath?.let { play(it) }
                }

                override fun onPause() {
                    pause()
                }

                override fun onStop() {
                    stopAndRelease()
                }

                override fun onSeekTo(pos: Long) {
                    seekTo(pos)
                    val state = if (isPlaying()) PlaybackStateCompat.STATE_PLAYING
                    else PlaybackStateCompat.STATE_PAUSED
                    updatePlaybackState(state, pos)
                }
            })
            setSessionActivity(sessionPendingIntent)
            isActive = true
        }
    }

    fun releaseMediaSession() {
        handler.removeCallbacks(positionUpdateRunnable)

        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post { releaseMediaSession() }
            return
        }

        mediaSession?.isActive = false
        mediaSession?.release()
        mediaSession = null
    }

    private fun updatePlaybackState(
        state: Int,
        position: Long = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN
    ) {
        handler.post {
            val stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_STOP or
                            PlaybackStateCompat.ACTION_SEEK_TO
                )
                .setState(state, position, 1f)
            mediaSession?.setPlaybackState(stateBuilder.build())
        }
    }

    private fun updateMetadata(title: String = "Pomegranate", artist: String = "Audio") {
        handler.post {
            val metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getDuration())
                .build()
            mediaSession?.setMetadata(metadata)
        }
    }

    @Synchronized
    fun prepare(path: String, onComplete: (() -> Unit)?) {
        completionListener = onComplete

        if (currentPath == path && mediaPlayer != null) {
            return
        }

        releaseInternal()
        currentPath = path

        val mp = MediaPlayer()
        try {
            mp.setDataSource(path)
            mp.setOnCompletionListener {
                handler.removeCallbacks(positionUpdateRunnable)
                completionListener?.invoke()
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, getDuration())
                AudioPlaybackService.updateState(context, isPlaying = false)
            }
            mp.setOnErrorListener { _, what, extra ->
                android.util.Log.e("AudioPlayback", "MediaPlayer error: what=$what, extra=$extra")
                false
            }
            mp.prepare()
            mediaPlayer = mp

            initMediaSession()
            handler.postDelayed({
                updateMetadata()
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, 0)
            }, 100)

        } catch (e: Exception) {
            android.util.Log.e("AudioPlayback", "Error preparing audio: ${e.message}", e)
            mp.release()
            mediaPlayer = null
            currentPath = null
        }
    }

    @Synchronized
    fun play(path: String) {
        if (currentPath != path || mediaPlayer == null) {
            prepare(path, completionListener)
        }

        if (mediaPlayer == null) {
            android.util.Log.e("AudioPlayback", "Cannot play: MediaPlayer is null")
            return
        }

        try {
            mediaPlayer?.start()
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, getCurrentPosition())
            handler.removeCallbacks(positionUpdateRunnable)
            handler.post(positionUpdateRunnable)

            AudioPlaybackService.start(context)
            AudioPlaybackService.updateState(context, isPlaying = true)
        } catch (e: Exception) {
            android.util.Log.e("AudioPlayback", "Error starting playback: ${e.message}", e)
        }
    }

    @Synchronized
    fun pause() {
        try {
            mediaPlayer?.pause()
            handler.removeCallbacks(positionUpdateRunnable)
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, getCurrentPosition())
            AudioPlaybackService.updateState(context, isPlaying = false)
        } catch (e: Exception) {
            android.util.Log.e("AudioPlayback", "Error pausing: ${e.message}", e)
        }
    }

    @Synchronized
    fun seekTo(ms: Long) {
        try {
            mediaPlayer?.seekTo(ms.toInt())
        } catch (e: Exception) {
            android.util.Log.e("AudioPlayback", "Error seeking: ${e.message}", e)
        }
    }

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (e: IllegalStateException) {
            false
        }
    }

    fun getDuration(): Long {
        return try {
            mediaPlayer?.duration?.toLong()?.coerceAtLeast(0L) ?: 0L
        } catch (e: IllegalStateException) {
            0L
        }
    }

    fun getCurrentPosition(): Long {
        return try {
            mediaPlayer?.currentPosition?.toLong()?.coerceAtLeast(0L) ?: 0L
        } catch (e: IllegalStateException) {
            0L
        }
    }

    @Synchronized
    fun togglePlayPause() {
        if (isPlaying()) pause() else currentPath?.let { play(it) }
    }

    @Synchronized
    fun stopAndRelease() {
        handler.removeCallbacks(positionUpdateRunnable)
        releaseInternal()
        updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
        AudioPlaybackService.stop(context)
    }

    private fun releaseInternal() {
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            android.util.Log.e("AudioPlayback", "Error releasing player: ${e.message}", e)
        }
        mediaPlayer = null
        currentPath = null
    }
}