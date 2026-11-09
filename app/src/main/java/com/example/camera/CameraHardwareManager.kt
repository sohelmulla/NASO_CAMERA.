package com.example.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.util.Range
import android.util.Rational

data class CameraHardwareSpecs(
    val cameraId: String,
    val facing: String,
    val hardwareLevel: String,
    val isoRange: Range<Int>?,
    val exposureTimeRangeNs: Range<Long>?,
    val exposureCompensationRange: Range<Int>?,
    val exposureCompensationStep: Rational?,
    val isFlashSupported: Boolean,
    val isManualSensorSupported: Boolean,
    val isManualPostProcessingSupported: Boolean,
    val maxZoomRatio: Float,
    val isHdrSupported: Boolean
)

object CameraHardwareManager {

    fun inspectHardware(context: Context): List<CameraHardwareSpecs> {
        val list = mutableListOf<CameraHardwareSpecs>()
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return list
            for (id in cameraManager.cameraIdList) {
                val chars = cameraManager.getCameraCharacteristics(id)
                val facingInt = chars.get(CameraCharacteristics.LENS_FACING)
                val facing = when (facingInt) {
                    CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                    CameraCharacteristics.LENS_FACING_BACK -> "Rear"
                    CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
                    else -> "Unknown"
                }

                val levelInt = chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                val hardwareLevel = when (levelInt) {
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "Legacy"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "Limited"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "Full"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "Level 3 Pro"
                    else -> "External"
                }

                val isoRange = chars.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
                val exposureTimeRange = chars.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
                val evRange = chars.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
                val evStep = chars.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)
                val flash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false

                val capabilities = chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES) ?: intArrayOf()
                val manualSensor = capabilities.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)
                val manualPost = capabilities.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING)

                val maxZoom = chars.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 1.0f

                val aeModes = chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES) ?: intArrayOf()
                val hdr = aeModes.contains(CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE) ||
                        aeModes.contains(CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH)

                list.add(
                    CameraHardwareSpecs(
                        cameraId = id,
                        facing = facing,
                        hardwareLevel = hardwareLevel,
                        isoRange = isoRange,
                        exposureTimeRangeNs = exposureTimeRange,
                        exposureCompensationRange = evRange,
                        exposureCompensationStep = evStep,
                        isFlashSupported = flash,
                        isManualSensorSupported = manualSensor,
                        isManualPostProcessingSupported = manualPost,
                        maxZoomRatio = maxZoom,
                        isHdrSupported = hdr
                    )
                )
            }
        } catch (e: Exception) {
            // Graceful fallback if camera service unavailable
        }
        return list
    }
}
