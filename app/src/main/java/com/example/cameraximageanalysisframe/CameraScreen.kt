package com.example.cameraximageanalysisframe

import android.content.Context
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.example.cameraximageanalysisframe.CameraHelper.getHardwareLevel
import com.example.cameraximageanalysisframe.CameraHelper.getMaxResolution
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "CameraScreen"
private val FALLBACK_SIZE = Size(1920, 1080)

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    var state by remember { mutableStateOf(CameraState()) }

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    LaunchedEffect(state.useHighRes) {
        setupCamera(
            context = context,
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            executor = cameraExecutor,
            useHighRes = state.useHighRes,
            onStateChanged = { newState -> state = newState }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { previewView }
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(8.dp)
        ) {
            Text("HW: ${state.hwLevel}", color = Color.Cyan)
            Text("Frames: ${state.frameCount}", color = Color.White)
            Text("Requested: ${state.requestedRes}", color = Color.White)
            Text("Actual: ${state.actualRes}", color = Color.White)
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(state.status.color)
                .padding(8.dp)
        ) {
            Text(state.status.text, color = Color.White)
        }

        Button(
            onClick = {
                state = CameraState(useHighRes = !state.useHighRes)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Text(
                if (state.useHighRes) "Switch to ${FALLBACK_SIZE.width}x${FALLBACK_SIZE.height}"
                else "Switch to Max Resolution"
            )
        }
    }
}

private fun setupCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    executor: ExecutorService,
    useHighRes: Boolean,
    onStateChanged: (CameraState) -> Unit
) {
    val cameraProvider = ProcessCameraProvider.getInstance(context).get()

    cameraProvider.unbindAll()

    val backCameraInfo = cameraProvider.availableCameraInfos.firstOrNull {
        CameraSelector.DEFAULT_BACK_CAMERA.filter(listOf(it)).isNotEmpty()
    }

    if (backCameraInfo == null) {
        onStateChanged(
            CameraState(status = CameraStatus("No back camera", Color.Red))
        )
        Log.e(TAG, "No back camera found.")
        return
    }

    val hwLevel = getHardwareLevel(backCameraInfo)
    val targetRes = if (useHighRes) {
        getMaxResolution(backCameraInfo, FALLBACK_SIZE)
    } else {
        FALLBACK_SIZE
    }

    onStateChanged(
        CameraState(
            useHighRes = useHighRes,
            hwLevel = hwLevel,
            requestedRes = "${targetRes.width}x${targetRes.height}",
            status = CameraStatus("Binding...", Color.Yellow)
        )
    )

    Log.d(TAG, "Requesting: ${targetRes.width}x${targetRes.height}")

    val preview = Preview.Builder().build().apply {
        surfaceProvider = previewView.surfaceProvider
    }

    val imageAnalysis = ImageAnalysis.Builder()
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
        .setResolutionSelector(
            ResolutionSelector.Builder()
                .setResolutionStrategy(
                    ResolutionStrategy(
                        targetRes,
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                    )
                ).build()
        )
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    var frameCount = 0
    imageAnalysis.setAnalyzer(executor) { image ->
        frameCount++
        onStateChanged(
            CameraState(
                useHighRes = useHighRes,
                frameCount = frameCount,
                hwLevel = hwLevel,
                requestedRes = "${targetRes.width}x${targetRes.height}",
                actualRes = "${image.width}x${image.height}",
                status = CameraStatus("Working", Color.Green)
            )
        )

        if (frameCount % 30 == 0) {
            Log.v(TAG, "Frame $frameCount: ${image.width}x${image.height}")
        }

        image.close()
    }

    try {
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageAnalysis
        )
        Log.d(TAG, "Camera bound successfully.")
    } catch (e: Exception) {
        Log.e(TAG, "Binding failed", e)
        onStateChanged(
            CameraState(
                useHighRes = useHighRes,
                status = CameraStatus("Bind failed", Color.Red)
            )
        )
    }
}