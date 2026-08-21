package org.unstabledev.pomegranate

import javafx.application.Platform
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.util.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.TargetDataLine

private val jfxStarted = AtomicBoolean(false)

fun shutdownJavaFx() {
    if (jfxStarted.getAndSet(false)) {
        try {
            Platform.exit()
        } catch (_: Exception) {
        }
    }
}

private fun ensureJfxToolkit() {
    if (jfxStarted.compareAndSet(false, true)) {
        val latch = CountDownLatch(1)
        try {
            Platform.startup { latch.countDown() }
        } catch (_: IllegalStateException) {
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
    } catch (_: IllegalStateException) {
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

        try {
            if (!latch.await(5, java.util.concurrent.TimeUnit.SECONDS)) {
                initError = RuntimeException("Timeout waiting for JavaFX MediaPlayer to prepare")
            }
        } catch (e: InterruptedException) {
            initError = e
        }

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
        } catch (_: Exception) {
            false
        }

    actual fun isActive(): Boolean = true

    actual fun getDuration(): Long =
        try {
            mediaPlayer?.totalDuration?.toMillis()?.toLong()?.coerceAtLeast(0L) ?: 0L
        } catch (_: Exception) {
            0L
        }

    actual fun getCurrentPosition(): Long =
        try {
            mediaPlayer?.currentTime?.toMillis()?.toLong()?.coerceAtLeast(0L) ?: 0L
        } catch (_: Exception) {
            0L
        }

    actual fun setOnCompletionListener(listener: () -> Unit) {
        completionListener = listener
    }
}

actual class AudioRecorder actual constructor() {
    private var targetDataLine: TargetDataLine? = null
    private var recordingThread: Thread? = null
    private var isRunning = false

    actual fun start(outputFile: KMPFile) {
        try {
            val format = AudioFormat(16000f, 16, 1, true, false)
            val info = DataLine.Info(TargetDataLine::class.java, format)

            if (!AudioSystem.isLineSupported(info)) {
                println("AudioRecorder: Audio line not supported on this system")
                return
            }

            val line = AudioSystem.getLine(info) as TargetDataLine
            line.open(format)
            line.start()
            targetDataLine = line
            isRunning = true

            recordingThread = Thread {
                try {
                    val ais = AudioInputStream(line)
                    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile)
                } catch (e: Exception) {
                    if (isRunning) e.printStackTrace()
                }
            }
            recordingThread?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun stop() {
        isRunning = false
        try {
            targetDataLine?.stop()
            targetDataLine?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            recordingThread?.join(1000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun isRecording(): Boolean = isRunning && targetDataLine?.isOpen == true

    actual fun release() {
        stop()
    }
}

actual class CallAudioRecorder actual constructor() {
    private var targetDataLine: TargetDataLine? = null
    val lastFrame = Channel<ByteArray>()
    actual fun start() {
        try {
            val format = AudioFormat(16000f, 16, 1, true, false)
            val info = DataLine.Info(TargetDataLine::class.java, format)

            if (!AudioSystem.isLineSupported(info)) {
                println("AudioRecorder: Audio line not supported on this system")
                return
            }

            val line = AudioSystem.getLine(info) as TargetDataLine
            line.open(format)
            line.start()
            targetDataLine = line
            CoroutineScope(Dispatchers.IO).launch {
                while (isActive) {
                    val buffer = ByteArray(640)
                    line.read(buffer, 0 , buffer.size)
                    lastFrame.send(buffer)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    actual suspend fun getFrame(): ByteArray {
        return lastFrame.receive()
    }

    actual fun stop() {
        targetDataLine?.stop()
    }

}

actual class CallAudioPlayer actual constructor() {
    val audioFormat = AudioFormat(16000f, 16, 1, true, false)
    lateinit var line: SourceDataLine
    actual fun start() {
        line = AudioSystem.getSourceDataLine(audioFormat)
        line.open()
    }

    actual fun playChunk(chunk: ByteArray) {
        line.write(chunk, 0, chunk.size)
    }

    actual fun stop() {
        line.close()
    }

}