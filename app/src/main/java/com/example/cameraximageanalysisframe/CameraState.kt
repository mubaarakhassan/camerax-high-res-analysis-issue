package com.example.cameraximageanalysisframe

import androidx.compose.ui.graphics.Color

data class CameraState(
    val frameCount: Int = 0,
    val requestedRes: String = "Unknown",
    val actualRes: String = "Unknown",
    val hwLevel: String = "Unknown",
    val status: CameraStatus = CameraStatus("Initializing", Color.Gray)
)

data class CameraStatus(
    val text: String,
    val color: Color
)
