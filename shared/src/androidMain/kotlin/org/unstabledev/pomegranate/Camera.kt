package org.unstabledev.pomegranate

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executors

actual object Camera {
    lateinit var context: Context
    var surfaceRequest = mutableStateOf<SurfaceRequest?>(null)
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private val analysisExecutor = Executors.newSingleThreadExecutor()
    var onFrameOnCall :((bytes: ByteArray) -> Unit)? = null

    actual fun takePhoto(): String{
        val photoFile = File(
            "${Repository.pomegranatePath}temp",
            "photo_${System.currentTimeMillis()}.jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(p0: ImageCapture.OutputFileResults) {}
                override fun onError(p0: ImageCaptureException) = println("Photo capture failed: $p0")
            }

        )
        return photoFile.absolutePath
    }
    actual fun videoStream(action:(bytes: ByteArray) -> Unit){
        onFrameOnCall = action
    }

    @Composable
    actual fun startCamera() {
        val lyfecycleOwner = LocalLifecycleOwner.current
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider { request ->
                    surfaceRequest.value = request
                }
            }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().apply {
                    setAnalyzer(analysisExecutor) { imageProxy ->
                        val bitmap = imageProxy.toBitmap()
                        ByteArrayOutputStream().use { byteArray ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArray)
                            onFrameOnCall?.let {(byteArray.toByteArray())}
                        }
                        bitmap.recycle()
                        imageProxy.close()
                    }
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lyfecycleOwner, cameraSelector, preview, imageCapture, imageAnalysis)
        }, ContextCompat.getMainExecutor(context))

    }

    @Composable
    actual fun CameraPreview(modifier: Modifier) {
        surfaceRequest.value?.let { request ->
            CameraXViewfinder(
                modifier = modifier,
                surfaceRequest = request
            )
        }
    }
}