package org.unstabledev.pomegranate

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executors

actual class Camera {
    companion object {
        lateinit var context: Context
    }

    private var surfaceRequest by mutableStateOf<SurfaceRequest?>(null)
    private var isFrontCamera by mutableStateOf(false)

    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private val analysisExecutor = Executors.newSingleThreadExecutor()
    private var onFrameOnCall: ((bytes: ByteArray) -> Unit)? = null

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var lifecycleOwner: LifecycleOwner? = null

    actual fun takePhoto(): String {
        val photoFile = File(
            "${Repository.pomegranatePath}temp",
            "photo_${System.currentTimeMillis()}.jpg"
        )
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

    actual fun videoStream(action: (bytes: ByteArray) -> Unit) {
        onFrameOnCall = action
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

            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().apply {
                    setAnalyzer(analysisExecutor) { imageProxy ->
                        val bitmap = imageProxy.toBitmap()
                        ByteArrayOutputStream().use { stream ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, stream)
                            onFrameOnCall?.invoke(stream.toByteArray())
                        }
                        bitmap.recycle()
                        imageProxy.close()
                    }
                }

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

    @Composable
    actual fun StartCamera(front: Boolean) {
        val owner = LocalLifecycleOwner.current
        LaunchedEffect(front) {
            if (cameraProvider == null) {
                initCamera(owner, front)
            } else if (isFrontCamera != front) {
                isFrontCamera = front
                bindCamera()
            }
        }
    }

    @Composable
    actual fun CameraPreview(modifier: Modifier) {
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
}