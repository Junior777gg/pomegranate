package org.unstabledev.pomegranate

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.RequiresPermission
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.panpf.sketch.util.rotate
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executors
import kotlin.coroutines.resume

actual class Camera {
    companion object {
        lateinit var context: Context
    }

    private lateinit var owner: LifecycleOwner

    private var surfaceRequest by mutableStateOf<SurfaceRequest?>(null)
    private var isFrontCamera by mutableStateOf(false)

    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private val analysisExecutor = Executors.newSingleThreadExecutor()

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var lifecycleOwner: LifecycleOwner? = null

    // Последний кадр из анализатора (для быстрого стрима)
    @Volatile
    private var latestJpeg: ByteArray? = null

    actual fun takePhoto(): String {
        val photoFile = File(
            "${Repository.pomegranatePath}temp",
            "photo_${System.currentTimeMillis()}.jpeg"
        ).apply { createNewFile() }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {}
                override fun onError(exception: ImageCaptureException) {
                    println("Photo capture failed: ${exception.message}")
                }
            }
        )
        return photoFile.absolutePath
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private fun initCamera(owner: LifecycleOwner, front: Boolean) {
        lifecycleOwner = owner
        isFrontCamera = front

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            cameraProvider = provider

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider { request -> surfaceRequest = request }
            }
            previewUseCase = preview

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            // Анализатор непрерывно держит свежий кадр в latestJpeg
            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build().apply {
                    setAnalyzer(analysisExecutor) { imageProxy ->
                        try {
                            val bitmap = imageProxy.toBitmap().rotate(-90)
                            val stream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, stream)
                            latestJpeg = stream.toByteArray()
                            bitmap.recycle()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            imageProxy.close()
                        }
                    }
                }

            // ВАЖНО: биндим камеру, иначе ничего не работает
            bindCamera()
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCamera() {
        val provider = cameraProvider ?: return
        val preview = previewUseCase ?: return
        val owner = lifecycleOwner ?: return

        val cameraSelector = if (isFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        provider.unbindAll()
        provider.bindToLifecycle(owner, cameraSelector, preview, imageCapture, imageAnalysis)
    }

    actual fun startCamera(front: Boolean) {
        if (cameraProvider == null) {
            initCamera(owner, front)
        } else if (isFrontCamera != front) {
            isFrontCamera = front
            bindCamera()
        }
    }

    @Composable
    actual fun CameraPreview(modifier: Modifier) {
        owner = LocalLifecycleOwner.current
        surfaceRequest?.let { request ->
            CameraXViewfinder(modifier = modifier, surfaceRequest = request)
        }
    }

    actual fun switchView() {
        switchView(!isFrontCamera)
    }

    actual fun switchView(front: Boolean) {
        if (isFrontCamera != front) {
            isFrontCamera = front
            bindCamera()
        }
    }

    // ВАРИАНТ 1 (рекомендуемый для стрима): берём последний кадр из анализатора.
    // Быстро, не тормозит камеру, идеально для видеозвонка.
    actual suspend fun getFrame(): ByteArray {
        // ждём пока появится хотя бы один кадр
        var waited = 0
        while (latestJpeg == null && waited < 5000) {
            kotlinx.coroutines.delay(50)
            waited += 50
        }
        return latestJpeg ?: ByteArray(0)
    }

}