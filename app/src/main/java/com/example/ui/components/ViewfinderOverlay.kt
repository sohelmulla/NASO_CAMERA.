package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioSetting
import com.example.model.GridType
import com.example.model.ProSettings
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.DarkSurfaceTranslucent
import com.example.ui.theme.LevelWarning
import com.example.ui.theme.NasoGold
import kotlinx.coroutines.delay

@Composable
fun ViewfinderOverlay(
    aspectRatioSetting: AspectRatioSetting,
    proSettings: ProSettings,
    zoomLevel: Float,
    onZoomSelected: (Float) -> Unit,
    aiStatusMessage: String?,
    aiSceneDetected: String,
    countdownRemaining: Int?,
    isVideoRecording: Boolean,
    videoDurationSec: Int,
    onFocusTap: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    var focusPoint by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(focusPoint) {
        if (focusPoint != null) {
            delay(1500)
            focusPoint = null
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    focusPoint = offset
                    onFocusTap(offset)
                }
            }
    ) {
        // Tap-to-focus reticle
        focusPoint?.let { pos ->
            Box(
                modifier = Modifier
                    .offset { IntOffset(pos.x.toInt() - 36, pos.y.toInt() - 36) }
                    .size(72.dp)
                    .border(1.5.dp, NasoGold, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(NasoGold)
                )
            }
        }

        // Composition Grid overlay
        if (proSettings.gridType != GridType.NONE) {
            GridCanvas(
                gridType = proSettings.gridType,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Horizon Level Indicator
        if (proSettings.showLevel) {
            HorizonLevelIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 40.dp)
            )
        }

        // Top Status pill: AI Scene detection or AI Enhance message
        val displayMessage = aiStatusMessage ?: (if (proSettings.aiSceneDetectionEnabled) "✨ AI Scene: $aiSceneDetected" else null)
        if (displayMessage != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(1.dp, NasoGold.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("ai_status_pill")
            ) {
                Text(
                    text = displayMessage,
                    color = NasoGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Video Recording Status (Timer + Red blinking dot + 4K badge)
        if (isVideoRecording) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
                    .border(1.dp, AccentRed.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AccentRed)
                )
                val minutes = videoDurationSec / 60
                val seconds = videoDurationSec % 60
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "4K 60FPS",
                    color = NasoGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Countdown Timer Overlay
        AnimatedVisibility(
            visible = countdownRemaining != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            countdownRemaining?.let { count ->
                Text(
                    text = "$count",
                    color = NasoGold,
                    fontSize = 84.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Zoom Selection Controls (0.5x, 1x, 2x, 5x)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val zoomOptions = listOf(0.5f, 1.0f, 2.0f, 5.0f)
            zoomOptions.forEach { z ->
                val isSelected = Math.abs(zoomLevel - z) < 0.1f
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) NasoGold else Color.Transparent)
                        .clickable { onZoomSelected(z) }
                        .testTag("zoom_btn_${z.toInt()}x"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (z == 0.5f) ".5" else "${z.toInt()}",
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun GridCanvas(
    gridType: GridType,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (gridType) {
            GridType.NONE -> {}
            GridType.RULE_OF_THIRDS -> {
                val lineColor = Color.White.copy(alpha = 0.22f)
                val strokeWidth = 1.dp.toPx()

                // Vertical lines at 1/3 and 2/3
                drawLine(lineColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth)
                drawLine(lineColor, Offset(2 * w / 3f, 0f), Offset(2 * w / 3f, h), strokeWidth)

                // Horizontal lines at 1/3 and 2/3
                drawLine(lineColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth)
                drawLine(lineColor, Offset(0f, 2 * h / 3f), Offset(w, 2 * h / 3f), strokeWidth)
            }
            GridType.GOLDEN_RATIO -> {
                val lineColor = NasoGold.copy(alpha = 0.25f)
                val strokeWidth = 1.dp.toPx()
                val phi = 0.618f

                drawLine(lineColor, Offset(w * (1 - phi), 0f), Offset(w * (1 - phi), h), strokeWidth)
                drawLine(lineColor, Offset(w * phi, 0f), Offset(w * phi, h), strokeWidth)
                drawLine(lineColor, Offset(0f, h * (1 - phi)), Offset(w, h * (1 - phi)), strokeWidth)
                drawLine(lineColor, Offset(0f, h * phi), Offset(w, h * phi), strokeWidth)
            }
            GridType.CROSSHAIR -> {
                val lineColor = NasoGold.copy(alpha = 0.35f)
                val strokeWidth = 1.5.dp.toPx()
                val length = 24.dp.toPx()

                val cx = w / 2f
                val cy = h / 2f
                drawLine(lineColor, Offset(cx - length, cy), Offset(cx + length, cy), strokeWidth)
                drawLine(lineColor, Offset(cx, cy - length), Offset(cx, cy + length), strokeWidth)
            }
        }
    }
}

@Composable
private fun HorizonLevelIndicator(
    modifier: Modifier = Modifier
) {
    // Horizon level line with pitch indication
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Canvas(modifier = Modifier.size(width = 48.dp, height = 4.dp)) {
            drawLine(
                color = AccentGreen,
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 2.dp.toPx()
            )
        }
        Text(
            text = "0.0°",
            color = AccentGreen,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
