package org.unstabledev.pomegranate

expect class AudioPlayer() {
    fun setDataSource(path: String)
    fun prepare()
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun release()
    fun isPlaying(): Boolean
    fun isActive(): Boolean
    fun getDuration(): Long
    fun getCurrentPosition(): Long
    fun setOnCompletionListener(listener: () -> Unit)
}