package org.unstabledev.pomegranate

import javafx.application.Platform
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.util.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine

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

actual class AudioRecorder actual constructor() {
    private var captureJob: Job? = null
    private var targetFile: File? = null

    actual fun start(outputFile: KMPFile) {
        if (isRecording()) return

        targetFile = File(outputFile.getAbsolutePath())

        captureJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val format = AudioFormat(
                    48000f,  // sample rate
                    16,      // sample size in bits
                    1,       // channels (mono)
                    true,    // signed
                    false    // big endian
                )

                val info = DataLine.Info(TargetDataLine::class.java, format)

                if (!AudioSystem.isLineSupported(info)) {
                    throw IllegalStateException("Audio line not supported")
                }

                val line = AudioSystem.getLine(info) as TargetDataLine
                line.open(format)
                line.start()

                val buffer = ByteArray(4096)
                val output = ByteArrayOutputStream()

                while (isActive) {
                    val bytesRead = line.read(buffer, 0, buffer.size)
                    if (bytesRead > 0) {
                        output.write(buffer, 0, bytesRead)
                    }
                }

                line.stop()
                line.close()

                val audioData = output.toByteArray()
                saveAsWav(targetFile!!, audioData, format)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    actual fun stop() {
        captureJob?.cancel()
        captureJob = null
        targetFile = null
    }

    actual fun isRecording(): Boolean = captureJob?.isActive == true

    actual fun release() {
        stop()
    }

    private fun saveAsWav(file: File, audioData: ByteArray, format: AudioFormat) {
        val audioInputStream = AudioInputStream(
            audioData.inputStream(),
            format,
            audioData.size.toLong() / format.frameSize
        )
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file)
    }
}