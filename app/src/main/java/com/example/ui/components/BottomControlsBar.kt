package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.CameraMode
import com.example.model.CapturedMedia
import com.example.ui.theme.AccentRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.NasoGold

@Composable
fun BottomControlsBar(
    cameraMode: CameraMode,
    isCapturing: Boolean,
    isVideoRecording: Boolean,
    latestMedia: CapturedMedia?,
    mediaCount: Int,
    isFrontCamera: Boolean,
    onShutterClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onSwitchCameraClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shutterScale by animateFloatAsState(
        targetValue = if (isPressed || isCapturing) 0.88f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "shutterScale"
    )

    val flipRotation by animateFloatAsState(
        targetValue = if (isFrontCamera) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "flipRotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Smart Gallery thumbnail button
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, NasoGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .clickable { onGalleryClick() }
                .testTag("gallery_button"),
            contentAlignment = Alignment.Center
        ) {
            if (latestMedia != null) {
                if (latestMedia.localFilePath != null) {
                    AsyncImage(
                        model = latestMedia.localFilePath,
                        contentDescription = "Latest capture thumbnail",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else if (latestMedia.drawableResId != null) {
                    Image(
                        painter = painterResource(id = latestMedia.drawableResId),
                        contentDescription = "Latest capture thumbnail",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Gallery",
                    tint = NasoGold,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Media count pill
            if (mediaCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(NasoGold)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "$mediaCount",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // 2. Shutter Capture Button
        Box(
            modifier = Modifier
                .size(76.dp)
                .scale(shutterScale)
                .clip(CircleShape)
                .background(Color.Transparent)
                .border(4.dp, NasoGold, CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onShutterClick
                )
                .testTag("capture_shutter_button"),
            contentAlignment = Alignment.Center
        ) {
            if (cameraMode == CameraMode.VIDEO) {
                // Video record button (red dot or square when recording)
                if (isVideoRecording) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentRed)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(AccentRed)
                    )
                }
            } else {
                // Photo capture button (crisp white center with subtle gold halo)
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        // 3. Camera switch button
        IconButton(
            onClick = onSwitchCameraClick,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
                .testTag("switch_camera_button")
        ) {
            Icon(
                imageVector = Icons.Default.Cameraswitch,
                contentDescription = "Switch Camera",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .rotate(flipRotation)
            )
        }
    }
}
