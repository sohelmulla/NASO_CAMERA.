package com.example.camera

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.model.CameraMode
import com.example.model.FlashMode
import com.example.model.ProSettings
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraXManager(private val context: Context) {

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    var cameraControl: CameraControl? = null
        private set

    var cameraInfo: CameraInfo? = null
        private set

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null

    var isRecordingVideo: Boolean = false
        private set

    var isRecordingPaused: Boolean = false
        private set

    var supportedVideoQualities: List<String> = emptyList()
        private set

    fun initializeCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        isFrontCamera: Boolean,
        flashMode: FlashMode,
        cameraMode: CameraMode = CameraMode.AI_AUTO,
        proSettings: ProSettings = ProSettings(),
        onInitialized: (Boolean) -> Unit
    ) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                cameraProvider = provider

                val previewBuilder = Preview.Builder()
                if (cameraMode == CameraMode.PRO) {
                    ProCameraInterop.applyProSettingsToPreview(previewBuilder, proSettings)
                }

                val preview = previewBuilder.build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val captureFlashMode = when (flashMode) {
                    FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
                    FlashMode.ON, FlashMode.TORCH -> ImageCapture.FLASH_MODE_ON
                    FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setFlashMode(captureFlashMode)
                    .build()

                val qualitySelector = QualitySelector.from(
                    Quality.UHD,
                    FallbackStrategy.higherQualityOrLowerThan(Quality.FHD)
                )

                val recorder = Recorder.Builder()
                    .setQualitySelector(qualitySelector)
                    .setExecutor(cameraExecutor)
                    .build()

                videoCapture = VideoCapture.withOutput(recorder)

                val cameraSelector = if (isFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                provider.unbindAll()

                // Bind use cases safely according to camera mode to prevent stream combination limit issues
                val camera: Camera = try {
                    if (cameraMode == CameraMode.VIDEO) {
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            videoCapture
                        )
                    } else {
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    }
                } catch (e: Exception) {
                    // Fallback to binding preview and image capture
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                }

                cameraControl = camera.cameraControl
                cameraInfo = camera.cameraInfo

                // Detect supported video qualities on this lens
                try {
                    val caps = Recorder.getVideoCapabilities(camera.cameraInfo)
                    val allQualities = listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD)
                    supportedVideoQualities = allQualities.filter { q ->
                        caps.isQualitySupported(q, androidx.camera.core.DynamicRange.SDR)
                    }.map { q ->
                        when (q) {
                            Quality.UHD -> "4K UHD (2160p)"
                            Quality.FHD -> "Full HD (1080p)"
                            Quality.HD -> "HD (720p)"
                            Quality.SD -> "SD (480p)"
                            else -> "Standard"
                        }
                    }
                } catch (e: Exception) {
                    supportedVideoQualities = listOf("Full HD (1080p)", "HD (720p)")
                }

                if (flashMode == FlashMode.TORCH && !isFrontCamera) {
                    cameraControl?.enableTorch(true)
                }

                onInitialized(true)
            } catch (e: Exception) {
                Log.e("CameraXManager", "Camera initialization error: ${e.message}", e)
                onInitialized(false)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun setZoom(ratio: Float) {
        try {
            cameraControl?.setZoomRatio(ratio.coerceIn(1.0f, 8.0f))
        } catch (e: Exception) {
            Log.e("CameraXManager", "Set zoom failed: ${e.message}")
        }
    }

    fun setExposureCompensation(index: Int) {
        try {
            cameraControl?.setExposureCompensationIndex(index)
        } catch (e: Exception) {
            Log.e("CameraXManager", "Set exposure failed: ${e.message}")
        }
    }

    fun setTorch(enabled: Boolean) {
        try {
            cameraControl?.enableTorch(enabled)
        } catch (e: Exception) {
            Log.e("CameraXManager", "Torch failed: ${e.message}")
        }
    }

    fun startTapToFocus(previewView: PreviewView, x: Float, y: Float) {
        try {
            val factory = previewView.meteringPointFactory
            val point = factory.createPoint(x, y)
            val action = FocusMeteringAction.Builder(point).build()
            cameraControl?.startFocusAndMetering(action)
        } catch (e: Exception) {
            Log.e("CameraXManager", "Tap to focus failed: ${e.message}")
        }
    }

    fun takePhoto(
        mode: CameraMode,
        onSuccess: (Uri) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val capture = imageCapture ?: run {
            onError(IllegalStateException("Camera ImageCapture not initialized"))
            return
        }

        val title = "NASO_${System.currentTimeMillis()}"
        val contentValues = MediaStorageHelper.createPhotoContentValues(title, mode)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri
                    if (savedUri != null) {
                        MediaStorageHelper.markPhotoCompleted(context.contentResolver, savedUri)
                        onSuccess(savedUri)
                    } else {
                        onError(IllegalStateException("Saved URI was null"))
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraXManager", "ImageCapture failed: ${exception.message}", exception)
                    onError(exception)
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    fun startVideoRecording(
        recordAudio: Boolean,
        onEvent: (VideoRecordEvent) -> Unit
    ) {
        val vc = videoCapture ?: return
        val title = "NASO_VID_${System.currentTimeMillis()}"
        val contentValues = MediaStorageHelper.createVideoContentValues(title)

        val outputOptions = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        var pending = vc.output.prepareRecording(context, outputOptions)

        if (recordAudio) {
            try {
                pending = pending.withAudioEnabled()
            } catch (e: SecurityException) {
                Log.w("CameraXManager", "Microphone permission denied, proceeding with silent video recording")
            }
        }

        activeRecording = pending.start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    isRecordingVideo = true
                    isRecordingPaused = false
                }
                is VideoRecordEvent.Pause -> {
                    isRecordingPaused = true
                }
                is VideoRecordEvent.Resume -> {
                    isRecordingPaused = false
                }
                is VideoRecordEvent.Finalize -> {
                    isRecordingVideo = false
                    isRecordingPaused = false
                    if (!event.hasError()) {
                        MediaStorageHelper.markVideoCompleted(context.contentResolver, event.outputResults.outputUri)
                    }
                }
            }
            onEvent(event)
        }
    }

    fun pauseVideoRecording() {
        activeRecording?.pause()
    }

    fun resumeVideoRecording() {
        activeRecording?.resume()
    }

    fun stopVideoRecording() {
        activeRecording?.stop()
        activeRecording = null
        isRecordingVideo = false
        isRecordingPaused = false
    }

    fun unbind() {
        stopVideoRecording()
        cameraProvider?.unbindAll()
    }

    fun shutdown() {
        unbind()
        cameraExecutor.shutdown()
    }
}
