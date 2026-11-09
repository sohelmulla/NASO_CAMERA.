package com.example.data

import android.content.Context
import android.graphics.Bitmap
import com.example.R
import com.example.model.CameraMode
import com.example.model.CapturedMedia
import com.example.model.PhotoFilter
import com.example.util.ImageProcessingUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class CameraRepository(private val context: Context) {

    private val _mediaList = MutableStateFlow<List<CapturedMedia>>(emptyList())
    val mediaList: StateFlow<List<CapturedMedia>> = _mediaList.asStateFlow()

    init {
        loadInitialMedia()
    }

    private fun loadInitialMedia() {
        val initial = listOf(
            CapturedMedia(
                id = "sample_1_sunset",
                drawableResId = R.drawable.sample_sunset,
                mode = CameraMode.AI_AUTO,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 18,
                resolution = "3840 x 2160 (4K)",
                iso = "ISO 100",
                shutter = "1/500s",
                aperture = "f/1.8",
                focalLength = "24mm eq.",
                aiScene = "Sunset Landscape",
                filterApplied = PhotoFilter.AI_VIBRANT
            ),
            CapturedMedia(
                id = "sample_2_night",
                drawableResId = R.drawable.sample_night,
                mode = CameraMode.NIGHT,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 55,
                resolution = "3840 x 2160 (4K)",
                iso = "ISO 800",
                shutter = "1/15s (HDR)",
                aperture = "f/1.6",
                focalLength = "24mm eq.",
                aiScene = "Neon City Night",
                filterApplied = PhotoFilter.NIGHT_BRIGHT
            ),
            CapturedMedia(
                id = "sample_3_portrait",
                drawableResId = R.drawable.sample_portrait,
                mode = CameraMode.PORTRAIT,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                resolution = "3840 x 2160 (4K)",
                iso = "ISO 200",
                shutter = "1/250s",
                aperture = "f/1.4 Bokeh",
                focalLength = "50mm eq.",
                aiScene = "Studio Portrait",
                filterApplied = PhotoFilter.STUDIO_BOKEH
            )
        )
        _mediaList.value = initial
    }

    fun addCapturedPhoto(
        bitmap: Bitmap,
        mode: CameraMode,
        aiScene: String,
        iso: String,
        shutter: String,
        filter: PhotoFilter = PhotoFilter.AI_VIBRANT
    ): CapturedMedia {
        val file = ImageProcessingUtils.saveBitmapToInternalStorage(context, bitmap, "NASO_${mode.name}")
        val media = CapturedMedia(
            id = UUID.randomUUID().toString(),
            localFilePath = file.absolutePath,
            mode = mode,
            timestamp = System.currentTimeMillis(),
            resolution = "3840 x 2160 (4K)",
            iso = iso,
            shutter = shutter,
            aperture = if (mode == CameraMode.PORTRAIT) "f/1.4" else "f/1.8",
            focalLength = "24mm eq.",
            aiScene = aiScene,
            filterApplied = filter
        )
        _mediaList.value = listOf(media) + _mediaList.value
        return media
    }

    fun addCapturedVideo(
        durationSec: Int,
        aiScene: String = "4K Cinema 60FPS"
    ): CapturedMedia {
        val media = CapturedMedia(
            id = UUID.randomUUID().toString(),
            drawableResId = R.drawable.sample_night,
            mode = CameraMode.VIDEO,
            timestamp = System.currentTimeMillis(),
            resolution = "3840 x 2160 (4K 60fps)",
            iso = "ISO 400",
            shutter = "1/120s",
            aperture = "f/1.8",
            focalLength = "24mm eq.",
            aiScene = aiScene,
            isVideo = true,
            videoDurationSec = durationSec
        )
        _mediaList.value = listOf(media) + _mediaList.value
        return media
    }

    fun deleteMedia(id: String) {
        _mediaList.value = _mediaList.value.filterNot { it.id == id }
    }

    fun updateMediaFilter(id: String, newFilter: PhotoFilter) {
        _mediaList.value = _mediaList.value.map { item ->
            if (item.id == id) {
                item.copy(filterApplied = newFilter)
            } else {
                item
            }
        }
    }
}
