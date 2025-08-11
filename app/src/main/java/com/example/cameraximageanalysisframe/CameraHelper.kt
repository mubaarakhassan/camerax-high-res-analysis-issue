package com.example.cameraximageanalysisframe

import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.util.Log
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraInfo

object CameraHelper {
    private const val TAG = "CameraHelper"

    @OptIn(ExperimentalCamera2Interop::class)
    fun getMaxResolution(cameraInfo: CameraInfo, fallbackSize: Size): Size {
        val cam2Info = Camera2CameraInfo.from(cameraInfo)
        val streamConfig = cam2Info.getCameraCharacteristic(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )

        val sizes = streamConfig?.getOutputSizes(ImageFormat.YUV_420_888)
        val maxSize = sizes?.maxByOrNull { it.width * it.height }

        Log.d(TAG, "Available YUV sizes: ${sizes?.joinToString()}")

        return maxSize ?: fallbackSize
    }

    @OptIn(ExperimentalCamera2Interop::class)
    fun getHardwareLevel(cameraInfo: CameraInfo): String {
        val cam2Info = Camera2CameraInfo.from(cameraInfo)
        val level = cam2Info.getCameraCharacteristic(
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL
        )

        return when(level) {
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "LEGACY"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "LIMITED"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "FULL"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "LEVEL_3"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "EXTERNAL"
            else -> "UNKNOWN"
        }.also {
            Log.d(TAG, "Hardware Level: $it")
        }
    }
}
