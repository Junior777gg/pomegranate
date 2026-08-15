package org.unstabledev.pomegranate

import android.media.MediaPlayer

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

    //Realised already by media playback
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