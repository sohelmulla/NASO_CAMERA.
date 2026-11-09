package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.CameraRepository
import com.example.model.AspectRatioSetting
import com.example.model.CameraMode
import com.example.model.CapturedMedia
import com.example.model.FlashMode
import com.example.model.PhotoFilter
import com.example.model.ProSettings
import com.example.model.TimerSetting
import com.example.util.ImageProcessingUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CameraRepository(application.applicationContext)

    val mediaList: StateFlow<List<CapturedMedia>> = repository.mediaList

    private val _currentMode = MutableStateFlow(CameraMode.AI_AUTO)
    val currentMode: StateFlow<CameraMode> = _currentMode.asStateFlow()

    private val _aspectRatio = MutableStateFlow(AspectRatioSetting.RATIO_4_3)
    val aspectRatio: StateFlow<AspectRatioSetting> = _aspectRatio.asStateFlow()

    private val _flashMode = MutableStateFlow(FlashMode.AUTO)
    val flashMode: StateFlow<FlashMode> = _flashMode.asStateFlow()

    private val _timerSetting = MutableStateFlow(TimerSetting.OFF)
    val timerSetting: StateFlow<TimerSetting> = _timerSetting.asStateFlow()

    private val _proSettings = MutableStateFlow(ProSettings())
    val proSettings: StateFlow<ProSettings> = _proSettings.asStateFlow()

    private val _zoomLevel = MutableStateFlow(1.0f)
    val zoomLevel: StateFlow<Float> = _zoomLevel.asStateFlow()

    private val _isFrontCamera = MutableStateFlow(false)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    private val _aiStatusMessage = MutableStateFlow<String?>("✨ AI 4K Neural Engine Active")
    val aiStatusMessage: StateFlow<String?> = _aiStatusMessage.asStateFlow()

    private val _aiSceneDetected = MutableStateFlow("Golden Sunset Pro")
    val aiSceneDetected: StateFlow<String> = _aiSceneDetected.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    private val _countdownRemaining = MutableStateFlow<Int?>(null)
    val countdownRemaining: StateFlow<Int?> = _countdownRemaining.asStateFlow()

    private val _isVideoRecording = MutableStateFlow(false)
    val isVideoRecording: StateFlow<Boolean> = _isVideoRecording.asStateFlow()

    private val _videoRecordDurationSec = MutableStateFlow(0)
    val videoRecordDurationSec: StateFlow<Int> = _videoRecordDurationSec.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showProShelf = MutableStateFlow(false)
    val showProShelf: StateFlow<Boolean> = _showProShelf.asStateFlow()

    private val _showGallery = MutableStateFlow(false)
    val showGallery: StateFlow<Boolean> = _showGallery.asStateFlow()

    private val _activeInspectedMedia = MutableStateFlow<CapturedMedia?>(null)
    val activeInspectedMedia: StateFlow<CapturedMedia?> = _activeInspectedMedia.asStateFlow()

    private val _splitBeforeAfterProgress = MutableStateFlow(0.5f)
    val splitBeforeAfterProgress: StateFlow<Float> = _splitBeforeAfterProgress.asStateFlow()

    private val _cameraPermissionGranted = MutableStateFlow(false)
    val cameraPermissionGranted: StateFlow<Boolean> = _cameraPermissionGranted.asStateFlow()

    private var videoTimerJob: Job? = null
    private var countdownJob: Job? = null

    init {
        // Start simulated AI scene analysis updates
        viewModelScope.launch {
            val scenes = listOf(
                "Golden Sunset 4K",
                "Studio Portrait HDR",
                "Neon Night Scene",
                "Ultra-Macro Clarity",
                "Architecture & Landscape"
            )
            var index = 0
            while (true) {
                delay(7000)
                if (_currentMode.value == CameraMode.AI_AUTO) {
                    index = (index + 1) % scenes.size
                    _aiSceneDetected.value = scenes[index]
                    _aiStatusMessage.value = "✨ AI Scene: ${_aiSceneDetected.value}"
                    delay(3000)
                    _aiStatusMessage.value = null
                }
            }
        }
    }

    fun setMode(mode: CameraMode) {
        _currentMode.value = mode
        when (mode) {
            CameraMode.AI_AUTO -> {
                _aiStatusMessage.value = "✨ AI Auto & Neural Color Active"
                _aiSceneDetected.value = "Golden Sunset 4K"
                _showProShelf.value = false
            }
            CameraMode.PORTRAIT -> {
                _aiStatusMessage.value = "👤 Portrait Bokeh f/1.4 Active"
                _aiSceneDetected.value = "Studio Portrait"
                _showProShelf.value = false
            }
            CameraMode.NIGHT -> {
                _aiStatusMessage.value = "🌙 Night HDR Multi-Exposure Active"
                _aiSceneDetected.value = "Night Scene 4K"
                _showProShelf.value = false
            }
            CameraMode.PRO -> {
                _aiStatusMessage.value = "⚙️ Pro Manual Controls Enabled"
                _showProShelf.value = true
            }
            CameraMode.VIDEO -> {
                _aiStatusMessage.value = "🎥 4K UHD 60FPS Video Ready"
                _showProShelf.value = false
            }
        }
    }

    fun toggleFlash() {
        _flashMode.value = when (_flashMode.value) {
            FlashMode.AUTO -> FlashMode.ON
            FlashMode.ON -> FlashMode.TORCH
            FlashMode.TORCH -> FlashMode.OFF
            FlashMode.OFF -> FlashMode.AUTO
        }
    }

    fun toggleAspectRatio() {
        _aspectRatio.value = when (_aspectRatio.value) {
            AspectRatioSetting.RATIO_4_3 -> AspectRatioSetting.RATIO_16_9
            AspectRatioSetting.RATIO_16_9 -> AspectRatioSetting.RATIO_1_1
            AspectRatioSetting.RATIO_1_1 -> AspectRatioSetting.RATIO_FULL
            AspectRatioSetting.RATIO_FULL -> AspectRatioSetting.RATIO_4_3
        }
    }

    fun toggleTimer() {
        _timerSetting.value = when (_timerSetting.value) {
            TimerSetting.OFF -> TimerSetting.SEC_3
            TimerSetting.SEC_3 -> TimerSetting.SEC_10
            TimerSetting.SEC_10 -> TimerSetting.OFF
        }
    }

    fun toggleHdr() {
        _proSettings.value = _proSettings.value.copy(hdrEnabled = !_proSettings.value.hdrEnabled)
        _aiStatusMessage.value = if (_proSettings.value.hdrEnabled) "✨ HDR Pro Enabled" else "HDR Disabled"
    }

    fun setZoom(zoom: Float) {
        _zoomLevel.value = zoom
    }

    fun switchCamera() {
        _isFrontCamera.value = !_isFrontCamera.value
    }

    fun toggleProShelf() {
        _showProShelf.value = !_showProShelf.value
    }

    fun updateProSettings(settings: ProSettings) {
        _proSettings.value = settings
    }

    fun setSettingsDialogVisible(visible: Boolean) {
        _showSettingsDialog.value = visible
    }

    fun setCameraPermissionGranted(granted: Boolean) {
        _cameraPermissionGranted.value = granted
    }

    fun triggerShutter(onComplete: (CapturedMedia) -> Unit = {}) {
        if (_isCapturing.value) return

        if (_currentMode.value == CameraMode.VIDEO) {
            toggleVideoRecording()
            return
        }

        val timerSec = _timerSetting.value.seconds
        if (timerSec > 0) {
            countdownJob?.cancel()
            countdownJob = viewModelScope.launch {
                for (i in timerSec downTo 1) {
                    _countdownRemaining.value = i
                    delay(1000)
                }
                _countdownRemaining.value = null
                executePhotoCapture(onComplete)
            }
        } else {
            executePhotoCapture(onComplete)
        }
    }

    private fun executePhotoCapture(onComplete: (CapturedMedia) -> Unit) {
        viewModelScope.launch {
            _isCapturing.value = true
            _aiStatusMessage.value = "✨ AI 4K Enhancing & Denoising..."

            delay(650) // Simulate multi-frame HDR capture & neural pipeline

            val mode = _currentMode.value
            val sampleRes = when (mode) {
                CameraMode.NIGHT -> R.drawable.sample_night
                CameraMode.PORTRAIT -> R.drawable.sample_portrait
                else -> R.drawable.sample_sunset
            }

            val baseBitmap = ImageProcessingUtils.loadBitmapFromResource(
                getApplication<Application>().applicationContext,
                sampleRes
            ) ?: Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888)

            val filter = when (mode) {
                CameraMode.NIGHT -> PhotoFilter.NIGHT_BRIGHT
                CameraMode.PORTRAIT -> PhotoFilter.STUDIO_BOKEH
                else -> PhotoFilter.AI_VIBRANT
            }

            val enhancedBitmap = ImageProcessingUtils.applyFilter(baseBitmap, filter)

            val media = repository.addCapturedPhoto(
                bitmap = enhancedBitmap,
                mode = mode,
                aiScene = _aiSceneDetected.value,
                iso = if (mode == CameraMode.PRO && _proSettings.value.iso != "Auto") "ISO ${_proSettings.value.iso}" else "ISO 100",
                shutter = if (mode == CameraMode.PRO && _proSettings.value.shutter != "Auto") _proSettings.value.shutter else "1/500s",
                filter = filter
            )

            _isCapturing.value = false
            _aiStatusMessage.value = "✨ 4K AI Photo Saved!"
            delay(1800)
            _aiStatusMessage.value = null
            onComplete(media)
        }
    }

    fun toggleVideoRecording() {
        if (_isVideoRecording.value) {
            // Stop recording
            videoTimerJob?.cancel()
            videoTimerJob = null
            val duration = _videoRecordDurationSec.value
            _isVideoRecording.value = false
            _videoRecordDurationSec.value = 0
            val media = repository.addCapturedVideo(durationSec = duration.coerceAtLeast(1))
            _aiStatusMessage.value = "✨ 4K Video Saved (${duration}s)"
            viewModelScope.launch {
                delay(2000)
                _aiStatusMessage.value = null
            }
        } else {
            // Start recording
            _isVideoRecording.value = true
            _videoRecordDurationSec.value = 0
            _aiStatusMessage.value = "🔴 Recording 4K UHD 60FPS"
            videoTimerJob = viewModelScope.launch {
                while (_isVideoRecording.value) {
                    delay(1000)
                    _videoRecordDurationSec.value += 1
                }
            }
        }
    }

    fun openGallery() {
        _showGallery.value = true
    }

    fun closeGallery() {
        _showGallery.value = false
        _activeInspectedMedia.value = null
    }

    fun selectMediaForInspection(media: CapturedMedia) {
        _activeInspectedMedia.value = media
        _splitBeforeAfterProgress.value = 0.5f
    }

    fun closeInspection() {
        _activeInspectedMedia.value = null
    }

    fun setInspectionFilter(filter: PhotoFilter) {
        val current = _activeInspectedMedia.value ?: return
        repository.updateMediaFilter(current.id, filter)
        _activeInspectedMedia.value = current.copy(filterApplied = filter)
    }

    fun deleteInspectedMedia() {
        val current = _activeInspectedMedia.value ?: return
        repository.deleteMedia(current.id)
        _activeInspectedMedia.value = null
    }

    fun setSplitProgress(progress: Float) {
        _splitBeforeAfterProgress.value = progress.coerceIn(0f, 1f)
    }
}
