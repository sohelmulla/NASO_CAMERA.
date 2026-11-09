package com.example.camera

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.model.CameraMode
import com.example.model.CapturedMedia
import com.example.model.PhotoFilter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object MediaStorageHelper {

    private const val FOLDER_PHOTOS = "Pictures/NASO Camera"
    private const val FOLDER_VIDEOS = "Movies/NASO Camera"

    fun createPhotoContentValues(title: String, mode: CameraMode): ContentValues {
        return ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$title.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.TITLE, title)
            put(MediaStore.Images.Media.DESCRIPTION, "Shot on NASO Camera - ${mode.title}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, FOLDER_PHOTOS)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
    }

    fun markPhotoCompleted(resolver: ContentResolver, uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            try {
                resolver.update(uri, values, null, null)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun saveBitmapToMediaStore(
        context: Context,
        bitmap: Bitmap,
        title: String,
        mode: CameraMode = CameraMode.AI_AUTO
    ): Uri? {
        val resolver = context.contentResolver
        val values = createPhotoContentValues(title, mode)
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null

        try {
            resolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 96, stream)
            }
            markPhotoCompleted(resolver, uri)
            return uri
        } catch (e: Exception) {
            try {
                resolver.delete(uri, null, null)
            } catch (ex: Exception) {
                // ignore
            }
            return null
        }
    }

    fun createVideoContentValues(title: String): ContentValues {
        return ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "$title.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.TITLE, title)
            put(MediaStore.Video.Media.DESCRIPTION, "Recorded on NASO Camera 4K Pro")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, FOLDER_VIDEOS)
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }
    }

    fun markVideoCompleted(resolver: ContentResolver, uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.IS_PENDING, 0)
            }
            try {
                resolver.update(uri, values, null, null)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun loadBitmapFromUri(context: Context, uri: Uri, targetWidth: Int = 1920): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
            val (height: Int, width: Int) = options.run { outHeight to outWidth }
            var inSampleSize = 1
            if (height > targetWidth || width > targetWidth) {
                val halfH = height / 2
                val halfW = width / 2
                while (halfH / inSampleSize >= targetWidth && halfW / inSampleSize >= targetWidth) {
                    inSampleSize *= 2
                }
            }
            options.inSampleSize = inSampleSize
            options.inJustDecodeBounds = false
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun deleteMediaFromUri(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.delete(uri, null, null) > 0
        } catch (e: Exception) {
            false
        }
    }
}
