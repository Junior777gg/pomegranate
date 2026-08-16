package org.unstabledev.pomegranate

import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

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