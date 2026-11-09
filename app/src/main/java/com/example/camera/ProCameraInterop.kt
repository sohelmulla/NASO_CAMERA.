package com.example.camera

import android.hardware.camera2.CaptureRequest
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import com.example.model.ProSettings

@OptIn(ExperimentalCamera2Interop::class)
object ProCameraInterop {

    /**
     * Applies Camera2 CaptureRequest options when the device hardware supports manual controls.
     */
    fun applyProSettingsToPreview(builder: Preview.Builder, proSettings: ProSettings) {
        val extender = Camera2Interop.Extender(builder)

        // White Balance Preset
        when (proSettings.wb) {
            "Daylight" -> extender.setCaptureRequestOption(
                CaptureRequest.CONTROL_AWB_MODE,
                CaptureRequest.CONTROL_AWB_MODE_DAYLIGHT
            )
            "Cloudy" -> extender.setCaptureRequestOption(
                CaptureRequest.CONTROL_AWB_MODE,
                CaptureRequest.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT
            )
            "Tungsten" -> extender.setCaptureRequestOption(
                CaptureRequest.CONTROL_AWB_MODE,
                CaptureRequest.CONTROL_AWB_MODE_INCANDESCENT
            )
            "Fluorescent" -> extender.setCaptureRequestOption(
                CaptureRequest.CONTROL_AWB_MODE,
                CaptureRequest.CONTROL_AWB_MODE_FLUORESCENT
            )
            else -> extender.setCaptureRequestOption(
                CaptureRequest.CONTROL_AWB_MODE,
                CaptureRequest.CONTROL_AWB_MODE_AUTO
            )
        }

        // Manual ISO (Sensitivity)
        val isoInt = proSettings.iso.toIntOrNull()
        if (isoInt != null) {
            extender.setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
            extender.setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY, isoInt)
        }

        // Manual Shutter Speed
        val shutterNs = parseShutterSpeedToNanoseconds(proSettings.shutter)
        if (shutterNs != null) {
            extender.setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
            extender.setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, shutterNs)
        }

        // Manual Focus
        when (proSettings.focus) {
            "Macro" -> {
                extender.setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_MACRO)
            }
            "Infinity" -> {
                extender.setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
                extender.setCaptureRequestOption(CaptureRequest.LENS_FOCUS_DISTANCE, 0.0f)
            }
            else -> {
                extender.setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            }
        }
    }

    private fun parseShutterSpeedToNanoseconds(shutter: String): Long? {
        return when (shutter) {
            "1/1000s" -> 1_000_000_000L / 1000L
            "1/500s" -> 1_000_000_000L / 500L
            "1/250s" -> 1_000_000_000L / 250L
            "1/125s" -> 1_000_000_000L / 125L
            "1/60s" -> 1_000_000_000L / 60L
            "1/30s" -> 1_000_000_000L / 30L
            "1/15s" -> 1_000_000_000L / 15L
            "1s" -> 1_000_000_000L
            else -> null
        }
    }
}
