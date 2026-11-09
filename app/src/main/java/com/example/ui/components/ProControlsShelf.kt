package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProSettings
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceTranslucent
import com.example.ui.theme.NasoGold
import com.example.ui.theme.TextSecondary

private enum class ProTab(val label: String) {
    ISO("ISO"),
    EV("EV"),
    WB("WB"),
    SHUTTER("S"),
    FOCUS("AF")
}

@Composable
fun ProControlsShelf(
    proSettings: ProSettings,
    onSettingsChanged: (ProSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ProTab.ISO) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurfaceTranslucent)
            .padding(vertical = 8.dp)
            .testTag("pro_controls_shelf")
    ) {
        // Tab selectors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                val currentValueText = when (tab) {
                    ProTab.ISO -> proSettings.iso
                    ProTab.EV -> "${if (proSettings.ev > 0) "+" else ""}${String.format("%.1f", proSettings.ev)}"
                    ProTab.WB -> proSettings.wb
                    ProTab.SHUTTER -> proSettings.shutter
                    ProTab.FOCUS -> proSettings.focus
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { selectedTab = tab }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tab.label,
                        color = if (isSelected) NasoGold else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentValueText,
                        color = if (isSelected) Color.White else TextSecondary.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Shelf options scroll row for the selected tab
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (selectedTab) {
                ProTab.ISO -> {
                    val isoOptions = listOf("Auto", "100", "200", "400", "800", "1600", "3200")
                    isoOptions.forEach { opt ->
                        ProOptionChip(
                            label = opt,
                            isSelected = proSettings.iso == opt,
                            onClick = { onSettingsChanged(proSettings.copy(iso = opt)) }
                        )
                    }
                }
                ProTab.EV -> {
                    val evOptions = listOf(-2.0f, -1.5f, -1.0f, -0.5f, 0.0f, 0.5f, 1.0f, 1.5f, 2.0f)
                    evOptions.forEach { opt ->
                        val label = "${if (opt > 0) "+" else ""}${String.format("%.1f", opt)}"
                        ProOptionChip(
                            label = label,
                            isSelected = Math.abs(proSettings.ev - opt) < 0.01f,
                            onClick = { onSettingsChanged(proSettings.copy(ev = opt)) }
                        )
                    }
                }
                ProTab.WB -> {
                    val wbOptions = listOf("Auto", "Daylight", "Cloudy", "Tungsten", "Fluorescent")
                    wbOptions.forEach { opt ->
                        ProOptionChip(
                            label = opt,
                            isSelected = proSettings.wb == opt,
                            onClick = { onSettingsChanged(proSettings.copy(wb = opt)) }
                        )
                    }
                }
                ProTab.SHUTTER -> {
                    val shutterOptions = listOf("Auto", "1/1000s", "1/500s", "1/250s", "1/60s", "1/15s", "1s")
                    shutterOptions.forEach { opt ->
                        ProOptionChip(
                            label = opt,
                            isSelected = proSettings.shutter == opt,
                            onClick = { onSettingsChanged(proSettings.copy(shutter = opt)) }
                        )
                    }
                }
                ProTab.FOCUS -> {
                    val focusOptions = listOf("Auto", "Macro", "Infinity")
                    focusOptions.forEach { opt ->
                        ProOptionChip(
                            label = opt,
                            isSelected = proSettings.focus == opt,
                            onClick = { onSettingsChanged(proSettings.copy(focus = opt)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) NasoGold else DarkSurfaceElevated)
            .border(
                width = 1.dp,
                color = if (isSelected) NasoGold else Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else Color.White,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
