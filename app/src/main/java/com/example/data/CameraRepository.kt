package com.example.data

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.R
import com.example.camera.MediaStorageHelper
import com.example.model.CameraMode
import com.example.model.CapturedMedia
import com.example.model.PhotoFilter
import com.example.util.ImageProcessingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class CameraRepository(private val context: Context) {

    private val _mediaList = MutableStateFlow<List<CapturedMedia>>(emptyList())
    val mediaList: StateFlow<List<CapturedMedia>> = _mediaList.asStateFlow()

    init {
        loadStarterAndMediaStoreMedia()
    }

    fun loadStarterAndMediaStoreMedia() {
        val initialSamples = listOf(
            CapturedMedia(
                id = "sample_1_sunset",
                drawableResId = R.drawable.sample_sunset,
                mode = CameraMode.AI_AUTO,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 18,
                resolution = "3840 x 2160 (4K UHD)",
                iso = "ISO 100",
                shutter = "1/500s",
                aperture = "f/1.8",
                focalLength = "24mm eq.",
                aiScene = "Sunset Landscape",
                filterApplied = PhotoFilter.AI_VIBRANT,
                isFavorite = true
            ),
            CapturedMedia(
                id = "sample_2_night",
                drawableResId = R.drawable.sample_night,
                mode = CameraMode.NIGHT,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 55,
                resolution = "3840 x 2160 (4K UHD)",
                iso = "ISO 800",
                shutter = "1/15s (HDR)",
                aperture = "f/1.6",
                focalLength = "24mm eq.",
                aiScene = "Neon City Night",
                filterApplied = PhotoFilter.NIGHT_BRIGHT,
                isFavorite = false
            ),
            CapturedMedia(
                id = "sample_3_portrait",
                drawableResId = R.drawable.sample_portrait,
                mode = CameraMode.PORTRAIT,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                resolution = "3840 x 2160 (4K UHD)",
                iso = "ISO 200",
                shutter = "1/250s",
                aperture = "f/1.4 Bokeh",
                focalLength = "50mm eq.",
                aiScene = "Studio Portrait",
                filterApplied = PhotoFilter.STUDIO_BOKEH,
                isFavorite = true
            )
        )

        // Query real MediaStore items created by NASO Camera
        val mediaStoreItems = queryMediaStoreMedia()
        _mediaList.value = mediaStoreItems + initialSamples
    }

    private fun queryMediaStoreMedia(): List<CapturedMedia> {
        val results = mutableListOf<CapturedMedia>()
        val resolver = context.contentResolver

        // 1. Query Images
        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE
        )
        val imageSelection = "${MediaStore.Images.Media.DISPLAY_NAME} LIKE 'NASO_%'"

        try {
            resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageProjection,
                imageSelection,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol)
                    val date = cursor.getLong(dateCol) * 1000L
                    val size = cursor.getLong(sizeCol)
                    val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    results.add(
                        CapturedMedia(
                            id = "media_$id",
                            mediaUri = uri,
                            mode = CameraMode.AI_AUTO,
                            timestamp = date,
                            resolution = "3840 x 2160 (4K)",
                            iso = "ISO Auto",
                            shutter = "1/250s",
                            aperture = "f/1.8",
                            focalLength = "24mm eq.",
                            aiScene = if (name.contains("Enhanced")) "AI Enhanced" else "Original Capture",
                            filterApplied = PhotoFilter.ORIGINAL,
                            isVideo = false,
                            fileSizeBytes = size
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Permission or MediaStore query error handled safely
        }

        // 2. Query Videos
        val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE
        )
        val videoSelection = "${MediaStore.Video.Media.DISPLAY_NAME} LIKE 'NASO_%'"

        try {
            resolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                videoSelection,
                null,
                "${MediaStore.Video.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val date = cursor.getLong(dateCol) * 1000L
                    val durationMs = cursor.getLong(durCol)
                    val size = cursor.getLong(sizeCol)
                    val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

                    results.add(
                        CapturedMedia(
                            id = "video_$id",
                            mediaUri = uri,
                            mode = CameraMode.VIDEO,
                            timestamp = date,
                            resolution = "4K UHD (60fps)",
                            iso = "ISO 400",
                            shutter = "1/120s",
                            aperture = "f/1.8",
                            focalLength = "24mm eq.",
                            aiScene = "4K UHD Video",
                            isVideo = true,
                            videoDurationSec = (durationMs / 1000).toInt(),
                            fileSizeBytes = size
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // ignore
        }

        return results
    }

    fun addRealCapturedPhoto(
        uri: Uri,
        mode: CameraMode,
        aiScene: String,
        iso: String,
        shutter: String,
        filter: PhotoFilter = PhotoFilter.ORIGINAL
    ): CapturedMedia {
        val media = CapturedMedia(
            id = UUID.randomUUID().toString(),
            mediaUri = uri,
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

    fun addRealCapturedVideo(
        uri: Uri,
        durationSec: Int
    ): CapturedMedia {
        val media = CapturedMedia(
            id = UUID.randomUUID().toString(),
            mediaUri = uri,
            mode = CameraMode.VIDEO,
            timestamp = System.currentTimeMillis(),
            resolution = "3840 x 2160 (4K UHD 60fps)",
            iso = "ISO 400",
            shutter = "1/120s",
            aperture = "f/1.8",
            focalLength = "24mm eq.",
            aiScene = "4K UHD Video",
            isVideo = true,
            videoDurationSec = durationSec
        )
        _mediaList.value = listOf(media) + _mediaList.value
        return media
    }

    fun addSimulatedCapturedPhoto(
        bitmap: Bitmap,
        mode: CameraMode,
        aiScene: String,
        iso: String,
        shutter: String,
        filter: PhotoFilter = PhotoFilter.AI_VIBRANT
    ): CapturedMedia {
        val title = "NASO_${mode.name}_${System.currentTimeMillis()}"
        val uri = MediaStorageHelper.saveBitmapToMediaStore(context, bitmap, title, mode)
        val file = ImageProcessingUtils.saveBitmapToInternalStorage(context, bitmap, "NASO_${mode.name}")

        val media = CapturedMedia(
            id = UUID.randomUUID().toString(),
            mediaUri = uri,
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

    fun deleteMedia(id: String) {
        val item = _mediaList.value.firstOrNull { it.id == id }
        if (item?.mediaUri != null) {
            MediaStorageHelper.deleteMediaFromUri(context, item.mediaUri)
        }
        _mediaList.value = _mediaList.value.filterNot { it.id == id }
    }

    fun toggleFavorite(id: String) {
        _mediaList.value = _mediaList.value.map { item ->
            if (item.id == id) {
                item.copy(isFavorite = !item.isFavorite)
            } else {
                item
            }
        }
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
