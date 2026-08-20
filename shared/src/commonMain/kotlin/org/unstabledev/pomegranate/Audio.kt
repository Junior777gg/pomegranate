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

expect class AudioRecorder() {
    fun start(outputFile: KMPFile)
    fun stop()
    fun isRecording(): Boolean
    fun release()
}

expect class CallAudioRecorder() {
    fun start()
    suspend fun getFrame(): ByteArray
    fun stop()
}

expect class CallAudioPlayer(){
    fun start()
    fun playChunk(chunk: ByteArray)
    fun stop()
}