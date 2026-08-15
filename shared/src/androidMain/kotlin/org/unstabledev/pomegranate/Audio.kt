package org.unstabledev.pomegranate

import android.media.MediaRecorder
import android.os.Build

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
    private var recording = false

    actual fun start(outputFile: KMPFile) {
        if (recording) return

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context!!)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.OGG)
            setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
            setAudioSamplingRate(48000)
            setAudioEncodingBitRate(64000)
            setOutputFile(outputFile.getAbsolutePath())

            try {
                prepare()
                start()
                recording = true
            } catch (e: Exception) {
                e.printStackTrace()
                release()
                throw e
            }
        }
    }

    actual fun stop() {
        if (!recording) return

        try {
            recorder?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        recording = false
    }

    actual fun isRecording(): Boolean = recording

    actual fun release() {
        try {
            recorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        recorder = null
        recording = false
    }
}