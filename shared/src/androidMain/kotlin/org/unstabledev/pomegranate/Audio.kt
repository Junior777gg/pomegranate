package org.unstabledev.pomegranate

import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.fleeksoft.io.ByteBuffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

actual class AudioPlayer actual constructor() {
    private var myPath: String? = null
    private var completionListener: (() -> Unit)? = null

    actual fun setDataSource(path: String) {
        myPath = path
    }

    actual fun prepare() {
        myPath?.let { AudioPlaybackManager.prepare(it, completionListener) }
    }

    actual fun play() {
        myPath?.let { AudioPlaybackManager.play(it) }
    }

    actual fun pause() {
        if (isActive()) AudioPlaybackManager.pause()
    }

    actual fun seekTo(positionMs: Long) {
        if (isActive()) AudioPlaybackManager.seekTo(positionMs)
    }

    actual fun release() {}

    actual fun isPlaying(): Boolean =
        isActive() && AudioPlaybackManager.isPlaying()

    actual fun isActive(): Boolean =
        myPath != null && AudioPlaybackManager.currentPath == myPath

    actual fun getDuration(): Long =
        if (isActive()) AudioPlaybackManager.getDuration() else 0L

    actual fun getCurrentPosition(): Long =
        if (isActive()) AudioPlaybackManager.getCurrentPosition() else 0L

    actual fun setOnCompletionListener(listener: () -> Unit) {
        completionListener = listener
    }
}

actual class AudioRecorder actual constructor() {
    private var recorder: MediaRecorder? = null

    actual fun start(outputFile: KMPFile) {
        if (ContextCompat.checkSelfPermission(AudioPlaybackManager.context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("RECORD_AUDIO permission not granted")
        }

        stop()

        try {
            val mr = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.OGG)
                setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
                setOutputFile(outputFile)
                prepare()
                start()
            }
            recorder = mr
        } catch (e: Exception) {
            android.util.Log.e("AudioRecorder", "Failed to start recording", e)
            stop()
            throw e
        }
    }

    actual fun stop() {
        try {
            recorder?.stop()
        } catch (_: Exception) { }
        try {
            recorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        recorder = null
    }

    actual fun isRecording(): Boolean = recorder != null

    actual fun release() {
        try {
            recorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        recorder = null
    }
}

actual class CallAudioRecorder actual constructor(){
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private var audioRecord: AudioRecord? = null
    lateinit var scope: Job
    val lastChunk = Channel<ByteArray>()
    actual fun start() {
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val chunkSize = 640
        if (ContextCompat.checkSelfPermission(AudioPlaybackManager.context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("RECORD_AUDIO permission not granted")
        }
        audioRecord = AudioRecord(
            // AudioSource.VOICE_COMMUNICATION включает системное эхоподавление и шумоподавление!
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            sampleRate,
            channelConfig,
            audioFormat,
            maxOf(minBufferSize, chunkSize * 4)
        )
        audioRecord?.startRecording()
        scope = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val buffer = ByteBuffer.allocate(chunkSize)
                audioRecord?.read(buffer,chunkSize)
                lastChunk.send(buffer.array())
            }
        }

    }

    actual suspend fun getFrame(): ByteArray {
        return lastChunk.receive()
    }

    actual fun stop() {
        scope.cancel()
        audioRecord?.stop()
    }
}
actual class CallAudioPlayer actual constructor() {
    private val sampleRate = 16000
    private var audioTrack: AudioTrack? = null
    actual fun start() {
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
    }

    actual fun playChunk(chunk: ByteArray) {
        audioTrack?.write(chunk, 0, chunk.size)
    }

    actual fun stop() {
        audioTrack?.stop()
    }

}

