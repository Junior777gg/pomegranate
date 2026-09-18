package org.unstabledev.pomegranate.components

import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Movie
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap

actual object GifDecoder {
    actual fun decode(bytes: ByteArray): List<GifFrame> {
        return try {
            decodeWithMovie(bytes) ?: decodeWithBitmapFactory(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun decodeWithMovie(bytes: ByteArray): List<GifFrame>? {
        return try {
            val movie = Movie.decodeByteArray(bytes, 0, bytes.size) ?: return null
            val frames = mutableListOf<GifFrame>()

            val duration = movie.duration()
            if (duration <= 0) return null

            val frameCount = estimateFrameCount(movie)
            val frameDuration = if (frameCount > 1) duration / frameCount else duration

            for (i in 0 until frameCount) {
                val time = (i * duration) / frameCount
                movie.setTime(time)

                val bitmap = createBitmap(movie.width(), movie.height())
                val canvas = Canvas(bitmap)
                movie.draw(canvas, 0f, 0f)

                frames.add(
                    GifFrame(
                        bitmap = bitmap.asImageBitmap(),
                        duration = frameDuration
                    )
                )
            }

            frames
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun estimateFrameCount(movie: Movie): Int {
        val duration = movie.duration()
        return when {
            duration <= 0 -> 1
            duration < 100 -> 10
            duration < 500 -> 20
            duration < 1000 -> 30
            else -> (duration / 50).coerceAtMost(60)
        }
    }

    private fun decodeWithBitmapFactory(bytes: ByteArray): List<GifFrame> {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: return emptyList()

        return listOf(
            GifFrame(
                bitmap = bitmap.asImageBitmap(),
                duration = 0
            )
        )
    }
}