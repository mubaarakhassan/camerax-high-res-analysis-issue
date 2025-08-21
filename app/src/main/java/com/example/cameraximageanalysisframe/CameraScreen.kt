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
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.cameraximageanalysisframe.CameraHelper.getAllResolutions
import com.example.cameraximageanalysisframe.CameraHelper.getHardwareLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "CameraScreen"

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraState by remember { mutableStateOf(CameraState()) }
    var availableResolutions by remember { mutableStateOf(listOf<Size>()) }
    var selectedResolution by remember { mutableStateOf<Size?>(null) }
    var showDropdown by remember { mutableStateOf(false) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { createPreviewView(context) }

    LaunchedEffect(Unit) {
        initializeCamera(
            context = context,
            onResolutionsLoaded = { resolutions ->
                availableResolutions = resolutions
                if (selectedResolution == null && resolutions.isNotEmpty()) {
                    selectedResolution = resolutions.first()
                }
            },
            onStateChanged = { cameraState = it }
        )
    }

    LaunchedEffect(selectedResolution) {
        selectedResolution?.let { resolution ->
            cameraState = cameraState.copy(frameCount = 0)
            setupCamera(
                context = context,
                lifecycleOwner = lifecycleOwner,
                previewView = previewView,
                executor = cameraExecutor,
                targetResolution = resolution,
                onStateChanged = { cameraState = it }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    CameraContent(
        previewView = previewView,
        cameraState = cameraState,
        availableResolutions = availableResolutions,
        selectedResolution = selectedResolution,
        showDropdown = showDropdown,
        onResolutionSelected = {
            selectedResolution = it
            showDropdown = false
        },
        onDropdownToggle = { showDropdown = it }
    )
}

@Composable
private fun CameraContent(
    previewView: PreviewView,
    cameraState: CameraState,
    availableResolutions: List<Size>,
    selectedResolution: Size?,
    showDropdown: Boolean,
    onResolutionSelected: (Size) -> Unit,
    onDropdownToggle: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { previewView }
        )

        CameraInfoOverlay(
            cameraState = cameraState,
            modifier = Modifier
                .align(Alignment.TopStart)
                .safeContentPadding()
        )

        StatusIndicator(
            status = cameraState.status,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .safeContentPadding()
        )

        ResolutionSelector(
            selectedResolution = selectedResolution,
            availableResolutions = availableResolutions,
            showDropdown = showDropdown,
            onResolutionSelected = onResolutionSelected,
            onDropdownToggle = onDropdownToggle,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeContentPadding()
        )
    }
}

@Composable
private fun CameraInfoOverlay(
    cameraState: CameraState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(8.dp)
    ) {
        Text("HW: ${cameraState.hwLevel}", color = Color.Cyan)
        Text("Frames: ${cameraState.frameCount}", color = Color.White)
        Text("Requested: ${cameraState.requestedRes}", color = Color.White)
        Text("Actual: ${cameraState.actualRes}", color = Color.White)
    }
}

@Composable
private fun StatusIndicator(
    status: CameraStatus,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(status.color)
            .padding(8.dp)
    ) {
        Text(status.text, color = Color.White)
    }
}

@Composable
private fun ResolutionSelector(
    selectedResolution: Size?,
    availableResolutions: List<Size>,
    showDropdown: Boolean,
    onResolutionSelected: (Size) -> Unit,
    onDropdownToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(bottom = 24.dp)
            .padding(8.dp)
    ) {
        Button(
            onClick = { onDropdownToggle(true) },
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    selectedResolution?.let { formatResolutionWithMp(it) }
                        ?: "Select Resolution"
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Show more resolution"
                )
            }
        }

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { onDropdownToggle(false) }
        ) {
            availableResolutions.forEach { resolution ->
                DropdownMenuItem(
                    text = {
                        Text(formatResolutionWithMp(resolution))
                    },
                    onClick = { onResolutionSelected(resolution) },
                    enabled = resolution != selectedResolution
                )
            }
        }
    }
}

private fun createPreviewView(context: Context): PreviewView {
    return PreviewView(context).apply {
        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
    }
}

private suspend fun initializeCamera(
    context: Context,
    onResolutionsLoaded: (List<Size>) -> Unit,
    onStateChanged: (CameraState) -> Unit
) {
    withContext(Dispatchers.Main) {
        try {
            val cameraProvider = ProcessCameraProvider.getInstance(context).get()
            val backCameraInfo = cameraProvider.availableCameraInfos.firstOrNull {
                CameraSelector.DEFAULT_BACK_CAMERA.filter(listOf(it)).isNotEmpty()
            }

            if (backCameraInfo == null) {
                onStateChanged(
                    CameraState(status = CameraStatus("No back camera", Color.Red))
                )
                return@withContext
            }

            val resolutions = getAllResolutions(backCameraInfo)
            onResolutionsLoaded(resolutions)

            onStateChanged(
                CameraState(
                    hwLevel = getHardwareLevel(backCameraInfo),
                    status = CameraStatus("Ready", Color.Blue)
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize camera", e)
            onStateChanged(
                CameraState(status = CameraStatus("Init failed", Color.Red))
            )
        }
    }
}

private suspend fun setupCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    executor: ExecutorService,
    targetResolution: Size,
    onStateChanged: (CameraState) -> Unit
) {
    withContext(Dispatchers.Main) {
        try {
            val cameraProvider = ProcessCameraProvider.getInstance(context).get()
            cameraProvider.unbindAll()

            val backCameraInfo = cameraProvider.availableCameraInfos.firstOrNull {
                CameraSelector.DEFAULT_BACK_CAMERA.filter(listOf(it)).isNotEmpty()
            } ?: return@withContext

            val hwLevel = getHardwareLevel(backCameraInfo)

            onStateChanged(
                CameraState(
                    hwLevel = hwLevel,
                    requestedRes = formatResolution(targetResolution),
                    status = CameraStatus("Binding...", Color.Yellow)
                )
            )

            val preview = Preview.Builder().build().apply {
                surfaceProvider = previewView.surfaceProvider
            }

            val imageAnalysis =
                createImageAnalysis(targetResolution, executor, hwLevel, onStateChanged)

            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )

            Log.d(
                TAG,
                "Camera bound successfully with resolution ${formatResolution(targetResolution)}"
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup camera", e)
            onStateChanged(
                CameraState(status = CameraStatus("Bind failed", Color.Red))
            )
        }
    }
}

private fun createImageAnalysis(
    targetResolution: Size,
    executor: ExecutorService,
    hwLevel: String,
    onStateChanged: (CameraState) -> Unit
): ImageAnalysis {
    return ImageAnalysis.Builder()
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
        .setResolutionSelector(
            ResolutionSelector.Builder()
// To use resolution strategy comment out the setResolutionFilter and uncomment the setResolutionStrategy
//                .setResolutionStrategy(
//                    ResolutionStrategy(
//                        targetResolution,
//                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
//                    )
//                ).build()
                .setResolutionFilter { outputSizes, rotationDegrees ->
                    // Check if our target resolution is in the available sizes
                    if (outputSizes.contains(targetResolution)) {
                        Collections.singletonList(targetResolution)
                    } else {
                        Log.w(
                            TAG,
                            "Target resolution $targetResolution not available, using first available"
                        )
                        Collections.singletonList(outputSizes.first())
                    }
                }.build()
        )
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .apply {
            var frameCount = 0
            setAnalyzer(executor) { image ->
                frameCount++

                onStateChanged(
                    CameraState(
                        frameCount = frameCount,
                        hwLevel = hwLevel,
                        requestedRes = formatResolution(targetResolution),
                        actualRes = formatResolution(Size(image.width, image.height)),
                        status = CameraStatus("Working", Color.Green)
                    )
                )

                if (frameCount % 30 == 0) {
                    Log.v(TAG, "Frame $frameCount: ${image.width}x${image.height}")
                }

                image.close()
            }
        }
}

private fun formatResolution(size: Size): String = "${size.width}x${size.height}"

private fun formatResolutionWithMp(size: Size): String {
    val megapixels = size.width * size.height / 1_000_000.0
    return "${formatResolution(size)} (${String.format(Locale.ROOT, "%.1f", megapixels)}MP)"
}