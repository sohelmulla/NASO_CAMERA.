package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.example.model.PhotoFilter
import java.io.File
import java.io.FileOutputStream

object ImageProcessingUtils {

    fun loadBitmapFromResource(context: Context, resId: Int, targetWidth: Int = 1080): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeResource(context.resources, resId, options)
            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetWidth)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeResource(context.resources, resId, options)
        } catch (e: Exception) {
            null
        }
    }

    fun loadBitmapFromFile(path: String, targetWidth: Int = 1080): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)
            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetWidth)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeFile(path, options)
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Applies real hardware color matrix filters to simulate AI Enhancements:
     * - AI_VIBRANT: Increases contrast, saturation, and dynamic range
     * - NIGHT_BRIGHT: Boosts shadows, brightens without blowing highlights
     * - CINEMATIC_GOLD: Adds golden warmth, crushed blacks, rich tone
     * - NOIR_DRAMA: High dynamic black and white
     * - STUDIO_BOKEH: Subtle vignette and warm clarity
     */
    fun applyFilter(src: Bitmap, filter: PhotoFilter): Bitmap {
        if (filter == PhotoFilter.ORIGINAL) return src

        val output = Bitmap.createBitmap(src.width, src.height, src.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cm = ColorMatrix()

        when (filter) {
            PhotoFilter.ORIGINAL -> {
                // Unchanged
            }
            PhotoFilter.AI_VIBRANT -> {
                // Contrast 1.15, Saturation 1.25, Brightness 1.05
                val contrast = 1.18f
                val t = (1.0f - contrast) / 2.0f * 255.0f
                val contrastMatrix = floatArrayOf(
                    contrast, 0f, 0f, 0f, t + 10f,
                    0f, contrast, 0f, 0f, t + 10f,
                    0f, 0f, contrast, 0f, t + 10f,
                    0f, 0f, 0f, 1f, 0f
                )
                val satMatrix = ColorMatrix()
                satMatrix.setSaturation(1.30f)
                val cmContrast = ColorMatrix(contrastMatrix)
                cmContrast.postConcat(satMatrix)
                cm.set(cmContrast)
            }
            PhotoFilter.NIGHT_BRIGHT -> {
                // Night mode brightening: +35% brightness, dynamic contrast
                val brightness = 35f
                val contrast = 1.12f
                val t = (1.0f - contrast) / 2.0f * 255.0f
                val matrix = floatArrayOf(
                    contrast, 0f, 0f, 0f, t + brightness,
                    0f, contrast, 0f, 0f, t + brightness,
                    0f, 0f, contrast, 0f, t + brightness * 1.1f,
                    0f, 0f, 0f, 1f, 0f
                )
                val satMatrix = ColorMatrix()
                satMatrix.setSaturation(1.15f)
                val cmNight = ColorMatrix(matrix)
                cmNight.postConcat(satMatrix)
                cm.set(cmNight)
            }
            PhotoFilter.CINEMATIC_GOLD -> {
                // Rich golden highlights & deep teals
                val satMatrix = ColorMatrix()
                satMatrix.setSaturation(1.20f)
                val colorTint = floatArrayOf(
                    1.22f, 0f, 0f, 0f, 15f,
                    0f, 1.10f, 0f, 0f, 8f,
                    0f, 0f, 0.90f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                )
                val tintMatrix = ColorMatrix(colorTint)
                tintMatrix.postConcat(satMatrix)
                cm.set(tintMatrix)
            }
            PhotoFilter.STUDIO_BOKEH -> {
                // Soft portrait enhancement, smooth skin tone warm bias
                val portraitMatrix = floatArrayOf(
                    1.08f, 0f, 0f, 0f, 12f,
                    0f, 1.05f, 0f, 0f, 8f,
                    0f, 0f, 1.02f, 0f, 5f,
                    0f, 0f, 0f, 1f, 0f
                )
                val satMatrix = ColorMatrix()
                satMatrix.setSaturation(1.10f)
                val cmPortrait = ColorMatrix(portraitMatrix)
                cmPortrait.postConcat(satMatrix)
                cm.set(cmPortrait)
            }
            PhotoFilter.NOIR_DRAMA -> {
                // Rich monochrome high contrast
                cm.setSaturation(0f)
                val contrast = 1.35f
                val t = (1.0f - contrast) / 2.0f * 255.0f
                val contrastMatrix = floatArrayOf(
                    contrast, 0f, 0f, 0f, t,
                    0f, contrast, 0f, 0f, t,
                    0f, 0f, contrast, 0f, t,
                    0f, 0f, 0f, 1f, 0f
                )
                cm.postConcat(ColorMatrix(contrastMatrix))
            }
        }

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return output
    }

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, prefix: String = "NASO_4K"): File {
        val filename = "${prefix}_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 96, out)
        }
        return file
    }
}
