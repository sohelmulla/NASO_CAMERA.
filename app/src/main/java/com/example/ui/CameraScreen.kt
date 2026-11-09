package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.camera.CameraPreviewComposable
import com.example.model.CameraMode
import com.example.model.NavDestination
import com.example.ui.components.AppNavigationBar
import com.example.ui.components.BottomControlsBar
import com.example.ui.components.ModeSelector
import com.example.ui.components.ProControlsShelf
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TopBarControls
import com.example.ui.components.ViewfinderOverlay
import com.example.ui.screens.AIStudioScreen
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.PermissionScreen
import com.example.ui.screens.ProModeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DarkBackground
import com.example.viewmodel.CameraViewModel
import kotlinx.coroutines.delay

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val cameraPermissionGranted by viewModel.cameraPermissionGranted.collectAsStateWithLifecycle()
    val cameraPermanentlyDenied by viewModel.cameraPermanentlyDenied.collectAsStateWithLifecycle()

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
    val mediaList by viewModel.mediaList.collectAsStateWithLifecycle()
    val activeInspectedMedia by viewModel.activeInspectedMedia.collectAsStateWithLifecycle()
    val hardwareSpecs by viewModel.hardwareSpecs.collectAsStateWithLifecycle()

    // Studio state
    val studioSelectedMedia by viewModel.studioSelectedMedia.collectAsStateWithLifecycle()
    val studioEnhancement by viewModel.studioEnhancement.collectAsStateWithLifecycle()
    val studioSplitProgress by viewModel.studioSplitProgress.collectAsStateWithLifecycle()
    val studioProcessedBitmap by viewModel.studioProcessedBitmap.collectAsStateWithLifecycle()

    var showShutterFlash by remember { mutableStateOf(false) }
    var userBypassedPermission by remember { mutableStateOf(false) }

    // Multi-permission launcher
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true

        viewModel.setCameraPermission(cameraGranted)
        viewModel.setAudioPermission(audioGranted)

        if (cameraGranted) {
            Toast.makeText(context, "Camera hardware enabled", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val camCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        val micCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        viewModel.setCameraPermission(camCheck == PackageManager.PERMISSION_GRANTED)
        viewModel.setAudioPermission(micCheck == PackageManager.PERMISSION_GRANTED)
    }

    // Back handling
    BackHandler(enabled = currentTab != NavDestination.CAMERA) {
        viewModel.setNavDestination(NavDestination.CAMERA)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AppNavigationBar(
                currentDestination = currentTab,
                onNavigate = { viewModel.setNavDestination(it) }
            )
        },
        modifier = modifier.testTag("naso_camera_root")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            when {
                // If camera permission not granted and not bypassed, show onboarding permission screen
                !cameraPermissionGranted && !userBypassedPermission && currentTab == NavDestination.CAMERA -> {
                    PermissionScreen(
                        isPermanentlyDenied = cameraPermanentlyDenied,
                        onRequestPermissions = {
                            permissionsLauncher.launch(
                                arrayOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.RECORD_AUDIO
                                )
                            )
                        },
                        onContinueWithoutCamera = {
                            userBypassedPermission = true
                            viewModel.setNavDestination(NavDestination.AI_STUDIO)
                        }
                    )
                }

                currentTab == NavDestination.CAMERA -> {
                    // Main Camera Viewfinder & Controls
                    Column(modifier = Modifier.fillMaxSize()) {
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

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CameraPreviewComposable(
                                cameraXManager = viewModel.cameraXManager,
                                isFrontCamera = isFrontCamera,
                                cameraMode = currentMode,
                                aspectRatioSetting = aspectRatio,
                                flashMode = flashMode,
                                proSettings = proSettings,
                                zoomLevel = zoomLevel,
                                isPermissionGranted = cameraPermissionGranted,
                                onRequestPermission = {
                                    permissionsLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.CAMERA,
                                            Manifest.permission.RECORD_AUDIO
                                        )
                                    )
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
                                onFocusTap = { offset ->
                                    // Tap to focus
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            if (showShutterFlash) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White)
                                )
                            }
                        }

                        // Mode Selector
                        ModeSelector(
                            selectedMode = currentMode,
                            onModeSelected = { viewModel.setMode(it) }
                        )

                        // Shutter & Bottom Bar
                        BottomControlsBar(
                            cameraMode = currentMode,
                            isCapturing = isCapturing,
                            isVideoRecording = isVideoRecording,
                            latestMedia = mediaList.firstOrNull(),
                            mediaCount = mediaList.size,
                            isFrontCamera = isFrontCamera,
                            onShutterClick = {
                                showShutterFlash = true
                                viewModel.triggerShutter()
                            },
                            onGalleryClick = { viewModel.setNavDestination(NavDestination.GALLERY) },
                            onSwitchCameraClick = {
                                viewModel.toggleCameraFacing()
                            }
                        )
                    }
                }

                currentTab == NavDestination.PRO_MODE -> {
                    ProModeScreen(
                        cameraXManager = viewModel.cameraXManager,
                        proSettings = proSettings,
                        hardwareSpecs = hardwareSpecs,
                        isPermissionGranted = cameraPermissionGranted,
                        onRequestPermission = {
                            permissionsLauncher.launch(
                                arrayOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.RECORD_AUDIO
                                )
                            )
                        },
                        onSettingsChanged = { viewModel.updateProSettings(it) },
                        onTriggerShutter = {
                            showShutterFlash = true
                            viewModel.triggerShutter()
                        }
                    )
                }

                currentTab == NavDestination.AI_STUDIO -> {
                    AIStudioScreen(
                        mediaList = mediaList,
                        selectedMedia = studioSelectedMedia,
                        activeEnhancement = studioEnhancement,
                        processedBitmap = studioProcessedBitmap,
                        splitProgress = studioSplitProgress,
                        onMediaSelected = { viewModel.selectStudioMedia(it) },
                        onEnhancementChanged = { viewModel.setStudioEnhancement(it) },
                        onSplitProgressChanged = { viewModel.setStudioSplitProgress(it) },
                        onSaveEnhanced = {
                            viewModel.saveStudioEnhancedPhoto { uri ->
                                Toast.makeText(
                                    context,
                                    if (uri != null) "✨ Enhanced Photo saved to Pictures/NASO Camera" else "Error saving photo",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }

                currentTab == NavDestination.GALLERY -> {
                    GalleryScreen(
                        mediaList = mediaList,
                        inspectedMedia = activeInspectedMedia,
                        onMediaSelected = { viewModel.selectMediaForInspection(it) },
                        onCloseInspection = { viewModel.closeInspection() },
                        onDeleteMedia = { viewModel.deleteMedia(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onOpenInStudio = { media ->
                            viewModel.selectStudioMedia(media)
                            viewModel.setNavDestination(NavDestination.AI_STUDIO)
                        }
                    )
                }

                currentTab == NavDestination.SETTINGS -> {
                    SettingsScreen(
                        proSettings = proSettings,
                        hardwareSpecs = hardwareSpecs,
                        onSettingsChanged = { viewModel.updateProSettings(it) }
                    )
                }
            }

            // Quick shutter flash effect reset
            LaunchedEffect(showShutterFlash) {
                if (showShutterFlash) {
                    delay(80)
                    showShutterFlash = false
                }
            }

            // Settings Dialog
            if (showSettingsDialog) {
                SettingsDialog(
                    proSettings = proSettings,
                    onSettingsChanged = { viewModel.updateProSettings(it) },
                    onDismiss = { viewModel.setSettingsDialogVisible(false) }
                )
            }
        }
    }
}
