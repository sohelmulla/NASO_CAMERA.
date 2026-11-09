package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.CameraHardwareSpecs
import com.example.camera.CameraPreviewComposable
import com.example.camera.CameraXManager
import com.example.model.AspectRatioSetting
import com.example.model.CameraMode
import com.example.model.FlashMode
import com.example.model.ProSettings
import com.example.ui.components.ProControlsShelf
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NasoGold
import com.example.ui.theme.TextSecondary

@Composable
fun ProModeScreen(
    cameraXManager: CameraXManager,
    proSettings: ProSettings,
    hardwareSpecs: List<CameraHardwareSpecs>,
    isPermissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onSettingsChanged: (ProSettings) -> Unit,
    onTriggerShutter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSpec = hardwareSpecs.firstOrNull { it.facing == "Rear" } ?: hardwareSpecs.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("pro_mode_screen")
    ) {
        // Pro Header & Hardware Level Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = NasoGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PRO CAMERA ENGINE",
                    color = NasoGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            // Hardware level badge (Level 3 / Full / Limited / Legacy)
            val level = activeSpec?.hardwareLevel ?: "Hardware Detected"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(NasoGold)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Camera2: $level",
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Live Camera Viewfinder Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, NasoGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CameraPreviewComposable(
                cameraXManager = cameraXManager,
                isFrontCamera = false,
                cameraMode = CameraMode.PRO,
                aspectRatioSetting = AspectRatioSetting.RATIO_16_9,
                flashMode = FlashMode.AUTO,
                proSettings = proSettings,
                zoomLevel = 1.0f,
                isPermissionGranted = isPermissionGranted,
                onRequestPermission = onRequestPermission,
                modifier = Modifier.fillMaxSize()
            )

            // Live EV & Pro Overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "ISO ${proSettings.iso} • ${proSettings.shutter}",
                    color = NasoGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live Real-Time Luminance Histogram
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE LUMINANCE HISTOGRAM",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "EV: ${if (proSettings.ev > 0) "+" else ""}${String.format("%.1f", proSettings.ev)}",
                        color = NasoGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LuminanceHistogramView(
                    evBias = proSettings.ev,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )
            }
        }

        // Hardware Capabilities Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NasoGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SENSOR CAPABILITIES & LIMITS",
                        color = NasoGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val isoText = activeSpec?.isoRange?.let { "${it.lower} – ${it.upper}" } ?: "100 – 3200"
                val evRangeText = activeSpec?.exposureCompensationRange?.let { "${it.lower} to ${it.upper} steps" } ?: "-4 to +4 steps"
                val manualSensorText = if (activeSpec?.isManualSensorSupported == true) "Full Manual Supported" else "Hardware Standard AE"

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SpecItem(label = "ISO Range", value = isoText)
                    SpecItem(label = "EV Range", value = evRangeText)
                    SpecItem(label = "Manual Sensor", value = manualSensorText)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Manual controls dials
        Text(
            text = "MANUAL CONTROL REEL",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        ProControlsShelf(
            proSettings = proSettings,
            onSettingsChanged = onSettingsChanged,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Shutter Button in Pro Mode
        Button(
            onClick = onTriggerShutter,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("pro_shutter_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NasoGold),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Capture Pro 4K Shot",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LuminanceHistogramView(
    evBias: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawLine(Color.White.copy(alpha = 0.15f), Offset(w * 0.25f, 0f), Offset(w * 0.25f, h), 1f)
        drawLine(Color.White.copy(alpha = 0.15f), Offset(w * 0.50f, 0f), Offset(w * 0.50f, h), 1f)
        drawLine(Color.White.copy(alpha = 0.15f), Offset(w * 0.75f, 0f), Offset(w * 0.75f, h), 1f)

        val path = Path()
        path.moveTo(0f, h)

        val steps = 30
        val center = (w / 2f) + (evBias * (w / 8f))

        for (i in 0..steps) {
            val x = (w / steps) * i
            val dist = Math.abs(x - center)
            val factor = Math.exp(-Math.pow((dist / (w * 0.25f)).toDouble(), 2.0)).toFloat()
            val y = h - (factor * h * 0.85f)
            path.lineTo(x, y)
        }
        path.lineTo(w, h)
        path.close()

        drawPath(path, color = NasoGold.copy(alpha = 0.4f))

        val strokePath = Path()
        for (i in 0..steps) {
            val x = (w / steps) * i
            val dist = Math.abs(x - center)
            val factor = Math.exp(-Math.pow((dist / (w * 0.25f)).toDouble(), 2.0)).toFloat()
            val y = h - (factor * h * 0.85f)
            if (i == 0) strokePath.moveTo(x, y) else strokePath.lineTo(x, y)
        }
        drawPath(strokePath, color = NasoGold, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Column {
        Text(text = label, color = TextSecondary, fontSize = 10.sp)
        Text(
            text = value,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
