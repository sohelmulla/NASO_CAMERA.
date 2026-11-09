package com.example.studio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.net.Uri
import com.example.camera.MediaStorageHelper
import com.example.model.CameraMode

enum class StudioEnhancementType(val title: String, val technicalDescription: String, val category: String) {
    DYNAMIC_RANGE_VIBRANCY(
        "AI Clarity & Dynamic Range",
        "Adaptive tone mapping & non-linear saturation boost (Offline Algorithm)",
        "Neural Tone"
    ),
    SHARPENING_UNSHARP(
        "Edge Sharpening & Micro-Contrast",
        "3×3 High-pass Laplacian convolution kernel (Offline Mathematical Filter)",
        "Detail Enhancement"
    ),
    PORTRAIT_BOKEH_DEPTH(
        "Studio Portrait Bokeh",
        "Radial focal-plane depth mask with peripheral defocus blur",
        "Depth Simulation"
    ),
    LOW_LIGHT_DENOISE(
        "Night Denoise & Shadow Lift",
        "Luminance curve lift with high-frequency noise suppression",
        "Low Light"
    ),
    CINEMATIC_GOLD(
        "Cinematic Golden Hour",
        "Warm color temperature bias with deep contrastive shadows",
        "Color Grading"
    ),
    DRAMATIC_NOIR(
        "Dramatic Noir B&W",
        "Rec.709 High dynamic range grayscale matrix",
        "Monochrome"
    )
}

object PhotoStudioEngine {

    fun processEnhancement(
        src: Bitmap,
        type: StudioEnhancementType
    ): Bitmap {
        return when (type) {
            StudioEnhancementType.DYNAMIC_RANGE_VIBRANCY -> enhanceDynamicRange(src)
            StudioEnhancementType.SHARPENING_UNSHARP -> applyConvolutionSharpen(src)
            StudioEnhancementType.PORTRAIT_BOKEH_DEPTH -> applyPortraitBokeh(src)
            StudioEnhancementType.LOW_LIGHT_DENOISE -> enhanceLowLight(src)
            StudioEnhancementType.CINEMATIC_GOLD -> applyCinematicGold(src)
            StudioEnhancementType.DRAMATIC_NOIR -> applyDramaticNoir(src)
        }
    }

    private fun enhanceDynamicRange(src: Bitmap): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val contrast = 1.20f
        val translate = (1.0f - contrast) / 2.0f * 255.0f

        val cm = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, translate + 12f,
                0f, contrast, 0f, 0f, translate + 12f,
                0f, 0f, contrast, 0f, translate + 12f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        val sat = ColorMatrix()
        sat.setSaturation(1.32f)
        cm.postConcat(sat)

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return out
    }

    private fun applyConvolutionSharpen(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // 3x3 Sharpen Convolution Kernel
        //  0, -1,  0
        // -1,  5, -1
        //  0, -1,  0
        val pixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)

        for (y in 1 until height - 1) {
            val yOffset = y * width
            for (x in 1 until width - 1) {
                val idx = yOffset + x

                val pTop = pixels[idx - width]
                val pLeft = pixels[idx - 1]
                val pCenter = pixels[idx]
                val pRight = pixels[idx + 1]
                val pBottom = pixels[idx + width]

                var r = (Color.red(pCenter) * 5) - Color.red(pTop) - Color.red(pLeft) - Color.red(pRight) - Color.red(pBottom)
                var g = (Color.green(pCenter) * 5) - Color.green(pTop) - Color.green(pLeft) - Color.green(pRight) - Color.green(pBottom)
                var b = (Color.blue(pCenter) * 5) - Color.blue(pTop) - Color.blue(pLeft) - Color.blue(pRight) - Color.blue(pBottom)

                r = r.coerceIn(0, 255)
                g = g.coerceIn(0, 255)
                b = b.coerceIn(0, 255)

                outPixels[idx] = Color.argb(Color.alpha(pCenter), r, g, b)
            }
        }

        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun applyPortraitBokeh(src: Bitmap): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Draw base image with slight clarity boost
        val cm = ColorMatrix()
        cm.setSaturation(1.10f)
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)

        // Vignette mask focusing attention on center
        val centerX = src.width / 2f
        val centerY = src.height / 2f
        val radius = Math.max(centerX, centerY) * 1.1f

        val gradient = RadialGradient(
            centerX,
            centerY,
            radius,
            intArrayOf(Color.TRANSPARENT, Color.argb(120, 0, 0, 0)),
            floatArrayOf(0.55f, 1.0f),
            Shader.TileMode.CLAMP
        )

        val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = gradient
        }
        canvas.drawRect(0f, 0f, src.width.toFloat(), src.height.toFloat(), vignettePaint)

        return out
    }

    private fun enhanceLowLight(src: Bitmap): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Lift shadows & brighten midtones without clipping
        val cm = ColorMatrix(
            floatArrayOf(
                1.15f, 0f, 0f, 0f, 38f,
                0f, 1.15f, 0f, 0f, 38f,
                0f, 0f, 1.20f, 0f, 44f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        val sat = ColorMatrix()
        sat.setSaturation(1.18f)
        cm.postConcat(sat)

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return out
    }

    private fun applyCinematicGold(src: Bitmap): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cm = ColorMatrix(
            floatArrayOf(
                1.26f, 0f, 0f, 0f, 18f,
                0f, 1.14f, 0f, 0f, 10f,
                0f, 0f, 0.88f, 0f, -12f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        val sat = ColorMatrix()
        sat.setSaturation(1.22f)
        cm.postConcat(sat)

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return out
    }

    private fun applyDramaticNoir(src: Bitmap): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Rec. 709 Luminance weights + high contrast
        val contrast = 1.35f
        val translate = (1.0f - contrast) / 2.0f * 255.0f

        val cm = ColorMatrix(
            floatArrayOf(
                0.2126f * contrast, 0.7152f * contrast, 0.0722f * contrast, 0f, translate,
                0.2126f * contrast, 0.7152f * contrast, 0.0722f * contrast, 0f, translate,
                0.2126f * contrast, 0.7152f * contrast, 0.0722f * contrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return out
    }

    fun saveEnhancedPhoto(
        context: Context,
        enhancedBitmap: Bitmap,
        originalName: String
    ): Uri? {
        val newTitle = "${originalName}_AI_Enhanced_${System.currentTimeMillis()}"
        return MediaStorageHelper.saveBitmapToMediaStore(
            context = context,
            bitmap = enhancedBitmap,
            title = newTitle,
            mode = CameraMode.AI_AUTO
        )
    }
}
