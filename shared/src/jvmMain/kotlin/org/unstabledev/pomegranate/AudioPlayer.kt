package org.unstabledev.pomegranate

import javafx.application.Platform
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.util.Duration
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

private val jfxStarted = AtomicBoolean(false)

private fun ensureJfxToolkit() {
    if (jfxStarted.compareAndSet(false, true)) {
        val latch = CountDownLatch(1)
        try {
            Platform.startup { latch.countDown() }
        } catch (e: IllegalStateException) {
            latch.countDown()
        }
        latch.await()
        Platform.setImplicitExit(false)
    }
}

actual class AudioPlayer actual constructor() {
    private var mediaPlayer: MediaPlayer? = null
    private var completionListener: (() -> Unit)? = null
    private var pendingPath: String? = null

    actual fun setDataSource(path: String) {
        pendingPath = path
    }

    actual fun prepare() {
        val path = pendingPath ?: return
        ensureJfxToolkit()

        val latch = CountDownLatch(1)
        var initError: Throwable? = null

        Platform.runLater {
            try {
                disposeInternal()
                val file = File(path)
                val media = Media(file.toURI().toString())
                val player = MediaPlayer(media)
                player.setOnEndOfMedia { completionListener?.invoke() }
                player.setOnError {
                    initError = player.error
                    latch.countDown()
                }
                player.setOnReady { latch.countDown() }
                mediaPlayer = player
            } catch (e: Exception) {
                initError = e
                latch.countDown()
            }
        }

        latch.await()
        initError?.let { throw RuntimeException(it) }
    }

    actual fun play() {
        Platform.runLater { mediaPlayer?.play() }
    }

    actual fun pause() {
        Platform.runLater { mediaPlayer?.pause() }
    }

    actual fun seekTo(positionMs: Long) {
        Platform.runLater {
            mediaPlayer?.seek(Duration.millis(positionMs.toDouble()))
        }
    }

    actual fun release() {
        Platform.runLater { disposeInternal() }
    }

    private fun disposeInternal() {
        mediaPlayer?.stop()
        mediaPlayer?.dispose()
        mediaPlayer = null
    }

    actual fun isPlaying(): Boolean =
        mediaPlayer?.status == MediaPlayer.Status.PLAYING

    actual fun isActive(): Boolean = true

    actual fun getDuration(): Long =
        mediaPlayer?.totalDuration?.toMillis()?.toLong()?.coerceAtLeast(0L) ?: 0L

    actual fun getCurrentPosition(): Long =
        mediaPlayer?.currentTime?.toMillis()?.toLong()?.coerceAtLeast(0L) ?: 0L

    actual fun setOnCompletionListener(listener: () -> Unit) {
        completionListener = listener
    }
}