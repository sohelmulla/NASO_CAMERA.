package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Timer10
import androidx.compose.material.icons.filled.Timer3
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioSetting
import com.example.model.FlashMode
import com.example.model.TimerSetting
import com.example.ui.theme.DarkSurfaceTranslucent
import com.example.ui.theme.NasoGold

@Composable
fun TopBarControls(
    flashMode: FlashMode,
    aspectRatio: AspectRatioSetting,
    timerSetting: TimerSetting,
    hdrEnabled: Boolean,
    onFlashClick: () -> Unit,
    onAspectRatioClick: () -> Unit,
    onTimerClick: () -> Unit,
    onHdrClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurfaceTranslucent)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo & Brand
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Text(
                text = "NASO",
                color = NasoGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            Box(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(NasoGold)
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "AI 4K",
                    color = Color.Black,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Quick Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Flash toggle
            IconButton(
                onClick = onFlashClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("flash_toggle_button")
            ) {
                val icon = when (flashMode) {
                    FlashMode.AUTO -> Icons.Default.FlashAuto
                    FlashMode.ON -> Icons.Default.FlashOn
                    FlashMode.TORCH -> Icons.Default.Highlight
                    FlashMode.OFF -> Icons.Default.FlashOff
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Flash mode: ${flashMode.label}",
                    tint = if (flashMode != FlashMode.OFF) NasoGold else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // HDR toggle
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onHdrClick() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("hdr_toggle_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "HDR",
                    color = if (hdrEnabled) NasoGold else Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = if (hdrEnabled) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Aspect Ratio toggle
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onAspectRatioClick() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("aspect_ratio_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = aspectRatio.label,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Timer toggle
            IconButton(
                onClick = onTimerClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("timer_toggle_button")
            ) {
                val icon = when (timerSetting) {
                    TimerSetting.OFF -> Icons.Default.Timer
                    TimerSetting.SEC_3 -> Icons.Default.Timer3
                    TimerSetting.SEC_10 -> Icons.Default.Timer10
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Timer: ${timerSetting.label}",
                    tint = if (timerSetting != TimerSetting.OFF) NasoGold else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Settings gear button
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Camera Settings",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
