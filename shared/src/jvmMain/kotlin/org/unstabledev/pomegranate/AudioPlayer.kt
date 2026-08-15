package org.unstabledev.pomegranate

import javafx.application.Platform
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.util.Duration
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

private val jfxStarted = AtomicBoolean(false)

fun shutdownJavaFx() {
    if (jfxStarted.getAndSet(false)) {
        try {
            Platform.exit()
        } catch (_: Exception) {}
    }
}

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

private fun safeRunLater(action: () -> Unit) {
    try {
        if (jfxStarted.get()) {
            Platform.runLater(action)
        }
    } catch (_: IllegalStateException) { }
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

        try {
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
        } catch (e: IllegalStateException) {
            initError = e
            latch.countDown()
        }

        latch.await()
        initError?.let { throw RuntimeException(it) }
    }

    actual fun play() {
        safeRunLater { mediaPlayer?.play() }
    }

    actual fun pause() {
        safeRunLater { mediaPlayer?.pause() }
    }

    actual fun seekTo(positionMs: Long) {
        safeRunLater {
            mediaPlayer?.seek(Duration.millis(positionMs.toDouble()))
        }
    }

    actual fun release() {
        safeRunLater { disposeInternal() }
    }

    private fun disposeInternal() {
        mediaPlayer?.stop()
        mediaPlayer?.dispose()
        mediaPlayer = null
    }

    actual fun isPlaying(): Boolean =
        try {
            mediaPlayer?.status == MediaPlayer.Status.PLAYING
        } catch (_: Exception) { false }

    actual fun isActive(): Boolean = true

    actual fun getDuration(): Long =
        try {
            mediaPlayer?.totalDuration?.toMillis()?.toLong()?.coerceAtLeast(0L) ?: 0L
        } catch (_: Exception) { 0L }

    actual fun getCurrentPosition(): Long =
        try {
            mediaPlayer?.currentTime?.toMillis()?.toLong()?.coerceAtLeast(0L) ?: 0L
        } catch (_: Exception) { 0L }

    actual fun setOnCompletionListener(listener: () -> Unit) {
        completionListener = listener
    }
}