package com.example.camera

import android.view.MotionEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.model.AspectRatioSetting
import com.example.model.CameraMode
import com.example.model.FlashMode
import com.example.model.ProSettings
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.NasoGold

@Composable
fun CameraPreviewComposable(
    cameraXManager: CameraXManager,
    isFrontCamera: Boolean,
    cameraMode: CameraMode,
    aspectRatioSetting: AspectRatioSetting,
    flashMode: FlashMode,
    proSettings: ProSettings,
    zoomLevel: Float,
    isPermissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraInitFailed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        val ratioModifier = when (aspectRatioSetting) {
            AspectRatioSetting.RATIO_4_3 -> Modifier.aspectRatio(3f / 4f)
            AspectRatioSetting.RATIO_16_9 -> Modifier.aspectRatio(9f / 16f)
            AspectRatioSetting.RATIO_1_1 -> Modifier.aspectRatio(1f)
            AspectRatioSetting.RATIO_FULL -> Modifier.fillMaxSize()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(ratioModifier)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (isPermissionGranted && !cameraInitFailed) {
                key(isFrontCamera, cameraMode) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }

                            cameraXManager.initializeCamera(
                                lifecycleOwner = lifecycleOwner,
                                previewView = previewView,
                                isFrontCamera = isFrontCamera,
                                flashMode = flashMode,
                                cameraMode = cameraMode,
                                proSettings = proSettings
                            ) { success ->
                                if (!success) {
                                    cameraInitFailed = true
                                } else {
                                    cameraXManager.setZoom(zoomLevel)
                                }
                            }

                            previewView.setOnTouchListener { _, event ->
                                if (event.action == MotionEvent.ACTION_UP) {
                                    cameraXManager.startTapToFocus(previewView, event.x, event.y)
                                }
                                true
                            }

                            previewView
                        },
                        update = {
                            cameraXManager.setZoom(zoomLevel)
                            if (!isFrontCamera) {
                                cameraXManager.setTorch(flashMode == FlashMode.TORCH)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                // High-resolution simulated live viewfinder fallback
                SimulatedViewfinder(
                    cameraMode = cameraMode,
                    zoomLevel = zoomLevel,
                    isFrontCamera = isFrontCamera,
                    isPermissionGranted = isPermissionGranted,
                    onRequestPermission = onRequestPermission,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun SimulatedViewfinder(
    cameraMode: CameraMode,
    zoomLevel: Float,
    isFrontCamera: Boolean,
    isPermissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "simulatedViewfinder")
    val subtleDrift by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "subtleDrift"
    )

    val sampleRes = when (cameraMode) {
        CameraMode.NIGHT -> R.drawable.sample_night
        CameraMode.PORTRAIT -> R.drawable.sample_portrait
        else -> R.drawable.sample_sunset
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val colorFilter = when (cameraMode) {
            CameraMode.NIGHT -> {
                val cm = ColorMatrix(
                    floatArrayOf(
                        1.2f, 0f, 0f, 0f, 20f,
                        0f, 1.2f, 0f, 0f, 20f,
                        0f, 0f, 1.3f, 0f, 30f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                ColorFilter.colorMatrix(cm)
            }
            CameraMode.PORTRAIT -> {
                val cm = ColorMatrix()
                cm.setToSaturation(1.15f)
                ColorFilter.colorMatrix(cm)
            }
            else -> null
        }

        Image(
            painter = painterResource(id = sampleRes),
            contentDescription = "Simulated Live Viewfinder",
            modifier = Modifier
                .fillMaxSize()
                .scale(zoomLevel.coerceIn(0.5f, 5.0f) * subtleDrift),
            contentScale = ContentScale.Crop,
            colorFilter = colorFilter
        )

        if (!isPermissionGranted) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = NasoGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.Black
                    )
                    Text(
                        text = "Enable Real Camera Lens",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}
