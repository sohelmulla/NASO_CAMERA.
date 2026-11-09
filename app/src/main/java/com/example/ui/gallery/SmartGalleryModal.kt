package com.example.ui.gallery

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
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
import com.example.model.PhotoFilter
import com.example.ui.theme.AccentRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceTranslucent
import com.example.ui.theme.NasoGold
import com.example.ui.theme.TextSecondary

@Composable
fun SmartGalleryModal(
    mediaList: List<CapturedMedia>,
    inspectedMedia: CapturedMedia?,
    splitProgress: Float,
    onSplitProgressChanged: (Float) -> Unit,
    onMediaSelected: (CapturedMedia) -> Unit,
    onCloseInspection: () -> Unit,
    onFilterChanged: (PhotoFilter) -> Unit,
    onDeleteMedia: () -> Unit,
    onCloseGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BackHandler {
        if (inspectedMedia != null) {
            onCloseInspection()
        } else {
            onCloseGallery()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("smart_gallery_modal")
    ) {
        if (inspectedMedia == null) {
            // Grid Overview
            GalleryGridView(
                mediaList = mediaList,
                onMediaClick = onMediaSelected,
                onClose = onCloseGallery,
                onDownloadClick = { media ->
                    Toast.makeText(context, "✨ 4K AI Photo Saved to Gallery!", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            // Inspection & AI Studio Mode
            InspectionDetailView(
                media = inspectedMedia,
                splitProgress = splitProgress,
                onSplitProgressChanged = onSplitProgressChanged,
                onFilterSelected = onFilterChanged,
                onBack = onCloseInspection,
                onDelete = onDeleteMedia,
                onShare = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "NASO 4K Pro Shot")
                        putExtra(Intent.EXTRA_TEXT, "Shot on NASO Camera AI 4K Pro • ${inspectedMedia.aiScene} • ${inspectedMedia.resolution}")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share NASO 4K Photo"))
                },
                onDownload = {
                    Toast.makeText(context, "✨ Downloaded in full 4K UHD resolution!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun GalleryGridView(
    mediaList: List<CapturedMedia>,
    onMediaClick: (CapturedMedia) -> Unit,
    onClose: () -> Unit,
    onDownloadClick: (CapturedMedia) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Smart Gallery",
                    color = NasoGold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${mediaList.size} captures",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("gallery_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Gallery",
                    tint = Color.White
                )
            }
        }

        if (mediaList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No captures yet.\nTap the shutter to take stunning 4K photos!",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(mediaList, key = { it.id }) { item ->
                    GalleryGridCard(
                        media = item,
                        onClick = { onMediaClick(item) },
                        onDownload = { onDownloadClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryGridCard(
    media: CapturedMedia,
    onClick: () -> Unit,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("gallery_card_${media.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                if (media.localFilePath != null) {
                    AsyncImage(
                        model = media.localFilePath,
                        contentDescription = "Captured photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (media.drawableResId != null) {
                    Image(
                        painter = painterResource(id = media.drawableResId),
                        contentDescription = "Captured photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Video play icon badge
                if (media.isVideo) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play video",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // 4K Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (media.isVideo) "4K 60FPS" else "4K AI",
                        color = NasoGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Mode / Scene Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = media.aiScene,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Card footer with Download link matching the prototype
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = media.filterApplied.displayName,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onDownload() }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = NasoGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "डाऊनलोड",
                        color = NasoGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun InspectionDetailView(
    media: CapturedMedia,
    splitProgress: Float,
    onSplitProgressChanged: (Float) -> Unit,
    onFilterSelected: (PhotoFilter) -> Unit,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .testTag("inspection_detail_view")
    ) {
        // Top Inspection Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to Gallery",
                    tint = Color.White
                )
            }

            Text(
                text = "AI 4K Inspector",
                color = NasoGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Row {
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = AccentRed
                    )
                }
            }
        }

        // Before / After Comparison Viewport
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val newProgress = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                        onSplitProgressChanged(newProgress)
                    }
                }
                .testTag("before_after_viewport"),
            contentAlignment = Alignment.Center
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val splitPx = widthPx * splitProgress

            // 1. Raw / Unenhanced version (Full width background)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        // Left side gets rendered as "Before"
                        clipRect(left = 0f, top = 0f, right = splitPx, bottom = size.height) {
                            this@drawWithContent.drawContent()
                        }
                    }
            ) {
                MediaImageLayer(
                    media = media,
                    filter = PhotoFilter.ORIGINAL,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. AI Enhanced version (Right side)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        clipRect(left = splitPx, top = 0f, right = size.width, bottom = size.height) {
                            this@drawWithContent.drawContent()
                        }
                    }
            ) {
                MediaImageLayer(
                    media = media,
                    filter = media.filterApplied,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 3. Vertical Divider Line with Draggable Handle
            Box(
                modifier = Modifier
                    .offset { IntOffset(splitPx.toInt() - 2, 0) }
                    .width(4.dp)
                    .height(this@BoxWithConstraints.maxHeight)
                    .background(NasoGold)
            )

            // Split Handle Circle
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

            // Labels: BEFORE and AFTER
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "RAW SENSOR",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "AI 4K ENHANCED",
                    color = NasoGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Hint for Before/After Slider
        Text(
            text = "◀ Drag slider to compare Raw Sensor vs AI 4K Pro ▶",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 8.dp)
        )

        // AI Enhancement Presets
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = NasoGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI NEURAL ENHANCEMENTS",
                    color = NasoGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            PhotoFilter.values().forEach { filter ->
                val isSelected = media.filterApplied == filter
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onFilterSelected(filter) }
                        .testTag("filter_${filter.name.lowercase()}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) DarkSurfaceElevated else DarkSurface
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NasoGold) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = filter.displayName,
                                color = if (isSelected) NasoGold else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = filter.description,
                                color = TextSecondary,
                                fontSize = 11.sp
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
        }

        // Shot EXIF & Neural Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "SHOT METADATA & NEURAL EXIF",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ExifItem(label = "Resolution", value = media.resolution)
                    ExifItem(label = "Aperture", value = media.aperture)
                    ExifItem(label = "Shutter", value = media.shutter)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ExifItem(label = "ISO", value = media.iso)
                    ExifItem(label = "Focal Length", value = media.focalLength)
                    ExifItem(label = "Scene", value = media.aiScene)
                }
            }
        }

        // Save / Download Full Resolution Button
        Button(
            onClick = onDownload,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(50.dp)
                .testTag("download_photo_button"),
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
                text = "Save 4K AI Photo to Gallery (डाऊनलोड)",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MediaImageLayer(
    media: CapturedMedia,
    filter: PhotoFilter,
    modifier: Modifier = Modifier
) {
    val colorFilter = remember(filter) {
        when (filter) {
            PhotoFilter.ORIGINAL -> null
            PhotoFilter.AI_VIBRANT -> {
                val cm = ColorMatrix()
                cm.setToSaturation(1.30f)
                ColorFilter.colorMatrix(cm)
            }
            PhotoFilter.NIGHT_BRIGHT -> {
                val cm = ColorMatrix(
                    floatArrayOf(
                        1.2f, 0f, 0f, 0f, 30f,
                        0f, 1.2f, 0f, 0f, 30f,
                        0f, 0f, 1.3f, 0f, 35f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                ColorFilter.colorMatrix(cm)
            }
            PhotoFilter.CINEMATIC_GOLD -> {
                val cm = ColorMatrix(
                    floatArrayOf(
                        1.25f, 0f, 0f, 0f, 18f,
                        0f, 1.12f, 0f, 0f, 10f,
                        0f, 0f, 0.90f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                ColorFilter.colorMatrix(cm)
            }
            PhotoFilter.STUDIO_BOKEH -> {
                val cm = ColorMatrix()
                cm.setToSaturation(1.12f)
                ColorFilter.colorMatrix(cm)
            }
            PhotoFilter.NOIR_DRAMA -> {
                val cm = ColorMatrix()
                cm.setToSaturation(0f)
                ColorFilter.colorMatrix(cm)
            }
        }
    }

    if (media.localFilePath != null) {
        AsyncImage(
            model = media.localFilePath,
            contentDescription = "Media preview",
            modifier = modifier,
            contentScale = ContentScale.Crop,
            colorFilter = colorFilter
        )
    } else if (media.drawableResId != null) {
        Image(
            painter = painterResource(id = media.drawableResId),
            contentDescription = "Media preview",
            modifier = modifier,
            contentScale = ContentScale.Crop,
            colorFilter = colorFilter
        )
    }
}

@Composable
private fun ExifItem(label: String, value: String) {
    Column {
        Text(text = label, color = TextSecondary, fontSize = 10.sp)
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
