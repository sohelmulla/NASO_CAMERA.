package com.example.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CapturedMedia
import com.example.studio.StudioEnhancementType
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NasoGold
import com.example.ui.theme.TextSecondary

@Composable
fun AIStudioScreen(
    mediaList: List<CapturedMedia>,
    selectedMedia: CapturedMedia?,
    activeEnhancement: StudioEnhancementType,
    processedBitmap: Bitmap?,
    splitProgress: Float,
    onMediaSelected: (CapturedMedia) -> Unit,
    onEnhancementChanged: (StudioEnhancementType) -> Unit,
    onSplitProgressChanged: (Float) -> Unit,
    onSaveEnhanced: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photoOnlyList = mediaList.filter { !it.isVideo }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("ai_studio_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = NasoGold,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI COMPUTATIONAL STUDIO",
                    color = NasoGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            // Privacy & Offline badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkSurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = NasoGold,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "100% Offline",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Photo Picker Strip
        Text(
            text = "SELECT PHOTO TO ENHANCE",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            photoOnlyList.forEach { item ->
                val isSelected = item.id == selectedMedia?.id
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) NasoGold else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onMediaSelected(item) }
                ) {
                    if (item.mediaUri != null) {
                        AsyncImage(
                            model = item.mediaUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (item.localFilePath != null) {
                        AsyncImage(
                            model = item.localFilePath,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (item.drawableResId != null) {
                        Image(
                            painter = painterResource(id = item.drawableResId),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Before/After Split Viewer
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val newProgress = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                        onSplitProgressChanged(newProgress)
                    }
                }
                .testTag("studio_split_viewer"),
            contentAlignment = Alignment.Center
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val splitPx = widthPx * splitProgress

            // 1. Raw / Original Version (Left side)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        clipRect(left = 0f, top = 0f, right = splitPx, bottom = size.height) {
                            this@drawWithContent.drawContent()
                        }
                    }
            ) {
                if (selectedMedia?.mediaUri != null) {
                    AsyncImage(
                        model = selectedMedia.mediaUri,
                        contentDescription = "Original Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (selectedMedia?.localFilePath != null) {
                    AsyncImage(
                        model = selectedMedia.localFilePath,
                        contentDescription = "Original Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (selectedMedia?.drawableResId != null) {
                    Image(
                        painter = painterResource(id = selectedMedia.drawableResId),
                        contentDescription = "Original Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // 2. Enhanced Version (Right side)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        clipRect(left = splitPx, top = 0f, right = size.width, bottom = size.height) {
                            this@drawWithContent.drawContent()
                        }
                    }
            ) {
                if (processedBitmap != null) {
                    Image(
                        bitmap = processedBitmap.asImageBitmap(),
                        contentDescription = "Enhanced Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (selectedMedia?.drawableResId != null) {
                    Image(
                        painter = painterResource(id = selectedMedia.drawableResId),
                        contentDescription = "Enhanced Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // 3. Draggable Split Divider Line & Handle
            Box(
                modifier = Modifier
                    .offset { IntOffset(splitPx.toInt() - 2, 0) }
                    .width(4.dp)
                    .height(this@BoxWithConstraints.maxHeight)
                    .background(NasoGold)
            )

            Box(
                modifier = Modifier
                    .offset { IntOffset(splitPx.toInt() - 16, (constraints.maxHeight / 2) - 16) }
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(NasoGold)
                    .border(2.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⇄",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Label Badges
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "ORIGINAL SENSOR",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "ENHANCED OUTPUT",
                    color = NasoGold,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = "◀ Drag divider to compare before / after ▶",
            color = TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Enhancement Algorithms List
        Text(
            text = "ENHANCEMENT ALGORITHMS",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        StudioEnhancementType.values().forEach { type ->
            val isSelected = type == activeEnhancement
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onEnhancementChanged(type) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) DarkSurfaceElevated else DarkSurface
                ),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NasoGold) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = type.title,
                                color = if (isSelected) NasoGold else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = type.category,
                                    color = TextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Text(
                            text = type.technicalDescription,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(NasoGold)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save Enhanced Photo Button (preserves original)
        Button(
            onClick = onSaveEnhanced,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_enhanced_photo_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NasoGold),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Save Enhanced Photo (Preserves Original)",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
