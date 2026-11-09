package com.example.model

import android.net.Uri

enum class NavDestination(val label: String) {
    CAMERA("Camera"),
    PRO_MODE("Pro Mode"),
    AI_STUDIO("AI Studio"),
    GALLERY("Gallery"),
    SETTINGS("Settings")
}

enum class CameraMode(val title: String, val badge: String) {
    AI_AUTO("AI AUTO", "AI 4K"),
    PORTRAIT("PORTRAIT", "BOKEH"),
    NIGHT("NIGHT MODE", "NIGHT HDR"),
    PRO("PRO CAMERA", "MANUAL"),
    VIDEO("4K VIDEO", "60 FPS")
}

enum class AspectRatioSetting(val label: String, val ratio: Float) {
    RATIO_4_3("4:3", 4f / 3f),
    RATIO_16_9("16:9", 16f / 9f),
    RATIO_1_1("1:1", 1f),
    RATIO_FULL("FULL", 19.5f / 9f)
}

enum class FlashMode(val label: String) {
    AUTO("Auto"),
    ON("On"),
    OFF("Off"),
    TORCH("Torch")
}

enum class TimerSetting(val seconds: Int, val label: String) {
    OFF(0, "Off"),
    SEC_3(3, "3s"),
    SEC_10(10, "10s")
}

enum class GridType(val label: String) {
    NONE("Off"),
    RULE_OF_THIRDS("3x3 Grid"),
    GOLDEN_RATIO("Golden Ratio"),
    CROSSHAIR("Crosshair")
}

enum class PhotoFilter(val displayName: String, val description: String) {
    ORIGINAL("Original", "Natural camera sensor data"),
    AI_VIBRANT("AI Vibrant Pro", "+25% Dynamic range & enhanced clarity"),
    NIGHT_BRIGHT("Night Clarity", "Multi-frame low-light shadow brightening"),
    CINEMATIC_GOLD("Cinematic Gold", "Warm gold highlights & deep rich shadows"),
    STUDIO_BOKEH("Studio Bokeh", "f/1.4 Creamy background blur simulation"),
    NOIR_DRAMA("Noir B&W", "High-contrast monochrome dynamic range")
}

data class ProSettings(
    val iso: String = "Auto",
    val ev: Float = 0.0f,
    val wb: String = "Auto",
    val shutter: String = "Auto",
    val focus: String = "Auto",
    val gridType: GridType = GridType.RULE_OF_THIRDS,
    val showLevel: Boolean = true,
    val aiSceneDetectionEnabled: Boolean = true,
    val watermarkEnabled: Boolean = true,
    val shutterSound: Boolean = true,
    val resolution4K: Boolean = true,
    val hdrEnabled: Boolean = true,
    val audioEnabled: Boolean = true
)

data class CapturedMedia(
    val id: String,
    val mediaUri: Uri? = null,
    val localFilePath: String? = null,
    val drawableResId: Int? = null,
    val mode: CameraMode,
    val timestamp: Long = System.currentTimeMillis(),
    val resolution: String = "3840 x 2160 (4K)",
    val iso: String = "ISO 100",
    val shutter: String = "1/250s",
    val aperture: String = "f/1.8",
    val focalLength: String = "24mm eq.",
    val aiScene: String = "AI Enhanced",
    val filterApplied: PhotoFilter = PhotoFilter.AI_VIBRANT,
    val isVideo: Boolean = false,
    val videoDurationSec: Int = 0,
    val isFavorite: Boolean = false,
    val fileSizeBytes: Long = 0L
)
