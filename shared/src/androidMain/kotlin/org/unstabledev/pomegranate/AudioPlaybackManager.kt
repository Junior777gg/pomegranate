package org.unstabledev.pomegranate

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer

@SuppressLint("StaticFieldLeak")
object AudioPlaybackManager {
    lateinit var context: Context
    private var mediaPlayer: MediaPlayer? = null
    var currentPath: String? = null
        private set

    private var completionListener: (() -> Unit)? = null

    @Synchronized
    fun prepare(path: String, onComplete: (() -> Unit)?) {
        completionListener = onComplete

        if (currentPath == path && mediaPlayer != null) {
            return
        }

        releaseInternal()
        currentPath = path
        mediaPlayer = MediaPlayer().apply {
            setDataSource(path)
            setOnCompletionListener {
                completionListener?.invoke()
                AudioPlaybackService.updateState(context, isPlaying = false)
            }
            prepare()
        }
    }

    @Synchronized
    fun play(path: String) {
        if (currentPath != path || mediaPlayer == null) {
            prepare(path, completionListener)
        }
        mediaPlayer?.start()
        AudioPlaybackService.start(context)
        AudioPlaybackService.updateState(context, isPlaying = true)
    }

    @Synchronized
    fun pause() {
        mediaPlayer?.pause()
        AudioPlaybackService.updateState(context, isPlaying = false)
    }

    @Synchronized
    fun seekTo(ms: Long) {
        mediaPlayer?.seekTo(ms.toInt())
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true
    fun getDuration(): Long = mediaPlayer?.duration?.toLong()?.coerceAtLeast(0L) ?: 0L
    fun getCurrentPosition(): Long = mediaPlayer?.currentPosition?.toLong()?.coerceAtLeast(0L) ?: 0L

    @Synchronized
    fun togglePlayPause() {
        if (isPlaying()) pause() else currentPath?.let { play(it) }
    }

    @Synchronized
    fun stopAndRelease() {
        releaseInternal()
        AudioPlaybackService.stop(context)
    }

    private fun releaseInternal() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentPath = null
    }
}