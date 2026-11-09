package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.camera.CameraPreviewComposable
import com.example.model.CameraMode
import com.example.ui.components.BottomControlsBar
import com.example.ui.components.ModeSelector
import com.example.ui.components.ProControlsShelf
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TopBarControls
import com.example.ui.components.ViewfinderOverlay
import com.example.ui.gallery.SmartGalleryModal
import com.example.ui.theme.DarkBackground
import com.example.viewmodel.CameraViewModel
import kotlinx.coroutines.delay

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val aspectRatio by viewModel.aspectRatio.collectAsStateWithLifecycle()
    val flashMode by viewModel.flashMode.collectAsStateWithLifecycle()
    val timerSetting by viewModel.timerSetting.collectAsStateWithLifecycle()
    val proSettings by viewModel.proSettings.collectAsStateWithLifecycle()
    val zoomLevel by viewModel.zoomLevel.collectAsStateWithLifecycle()
    val isFrontCamera by viewModel.isFrontCamera.collectAsStateWithLifecycle()
    val aiStatusMessage by viewModel.aiStatusMessage.collectAsStateWithLifecycle()
    val aiSceneDetected by viewModel.aiSceneDetected.collectAsStateWithLifecycle()
    val isCapturing by viewModel.isCapturing.collectAsStateWithLifecycle()
    val countdownRemaining by viewModel.countdownRemaining.collectAsStateWithLifecycle()
    val isVideoRecording by viewModel.isVideoRecording.collectAsStateWithLifecycle()
    val videoRecordDurationSec by viewModel.videoRecordDurationSec.collectAsStateWithLifecycle()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsStateWithLifecycle()
    val showProShelf by viewModel.showProShelf.collectAsStateWithLifecycle()
    val showGallery by viewModel.showGallery.collectAsStateWithLifecycle()
    val mediaList by viewModel.mediaList.collectAsStateWithLifecycle()
    val activeInspectedMedia by viewModel.activeInspectedMedia.collectAsStateWithLifecycle()
    val splitBeforeAfterProgress by viewModel.splitBeforeAfterProgress.collectAsStateWithLifecycle()
    val isPermissionGranted by viewModel.cameraPermissionGranted.collectAsStateWithLifecycle()

    var showShutterFlash by remember { mutableStateOf(false) }

    // Camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setCameraPermissionGranted(granted)
        if (granted) {
            Toast.makeText(context, "Camera permission enabled", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val check = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (check == PackageManager.PERMISSION_GRANTED) {
            viewModel.setCameraPermissionGranted(true)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.testTag("camera_screen_root")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top controls bar
                TopBarControls(
                    flashMode = flashMode,
                    aspectRatio = aspectRatio,
                    timerSetting = timerSetting,
                    hdrEnabled = proSettings.hdrEnabled,
                    onFlashClick = { viewModel.toggleFlash() },
                    onAspectRatioClick = { viewModel.toggleAspectRatio() },
                    onTimerClick = { viewModel.toggleTimer() },
                    onHdrClick = { viewModel.toggleHdr() },
                    onSettingsClick = { viewModel.setSettingsDialogVisible(true) }
                )

                // Viewfinder middle section
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CameraPreviewComposable(
                        isFrontCamera = isFrontCamera,
                        cameraMode = currentMode,
                        aspectRatioSetting = aspectRatio,
                        flashMode = flashMode,
                        zoomLevel = zoomLevel,
                        isPermissionGranted = isPermissionGranted,
                        onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    ViewfinderOverlay(
                        aspectRatioSetting = aspectRatio,
                        proSettings = proSettings,
                        zoomLevel = zoomLevel,
                        onZoomSelected = { viewModel.setZoom(it) },
                        aiStatusMessage = aiStatusMessage,
                        aiSceneDetected = aiSceneDetected,
                        countdownRemaining = countdownRemaining,
                        isVideoRecording = isVideoRecording,
                        videoDurationSec = videoRecordDurationSec,
                        onFocusTap = { /* tap to focus */ },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Simulated shutter flash
                    if (showShutterFlash) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                        )
                    }
                }

                // Pro Controls Shelf (if in PRO mode or enabled)
                if (currentMode == CameraMode.PRO || showProShelf) {
                    ProControlsShelf(
                        proSettings = proSettings,
                        onSettingsChanged = { viewModel.updateProSettings(it) }
                    )
                }

                // Camera Mode Selector
                ModeSelector(
                    selectedMode = currentMode,
                    onModeSelected = { viewModel.setMode(it) }
                )

                // Bottom Shutter & Gallery Controls
                BottomControlsBar(
                    cameraMode = currentMode,
                    isCapturing = isCapturing,
                    isVideoRecording = isVideoRecording,
                    latestMedia = mediaList.firstOrNull(),
                    mediaCount = mediaList.size,
                    isFrontCamera = isFrontCamera,
                    onShutterClick = {
                        showShutterFlash = true
                        viewModel.triggerShutter {
                            // capture finished
                        }
                    },
                    onGalleryClick = { viewModel.openGallery() },
                    onSwitchCameraClick = { viewModel.switchCamera() }
                )
            }

            // Reset shutter flash quickly
            LaunchedEffect(showShutterFlash) {
                if (showShutterFlash) {
                    delay(80)
                    showShutterFlash = false
                }
            }

            // Pro Camera Settings Modal
            if (showSettingsDialog) {
                SettingsDialog(
                    proSettings = proSettings,
                    onSettingsChanged = { viewModel.updateProSettings(it) },
                    onDismiss = { viewModel.setSettingsDialogVisible(false) }
                )
            }

            // Smart Gallery Modal
            AnimatedVisibility(
                visible = showGallery,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                SmartGalleryModal(
                    mediaList = mediaList,
                    inspectedMedia = activeInspectedMedia,
                    splitProgress = splitBeforeAfterProgress,
                    onSplitProgressChanged = { viewModel.setSplitProgress(it) },
                    onMediaSelected = { viewModel.selectMediaForInspection(it) },
                    onCloseInspection = { viewModel.closeInspection() },
                    onFilterChanged = { viewModel.setInspectionFilter(it) },
                    onDeleteMedia = { viewModel.deleteInspectedMedia() },
                    onCloseGallery = { viewModel.closeGallery() }
                )
            }
        }
    }
}
