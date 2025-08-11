package com.example.cameraximageanalysisframe

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

// Repro for the CameraX resolution bug where ImageAnalysis ignores
// high res requests...
class MainActivity : ComponentActivity() {
    private val hiRes = mutableStateOf(true)   // using high res by default to see the bug immediately
    private val frames = mutableIntStateOf(0)
    private val requestedRes = mutableStateOf("Unknown")
    private val actualRes = mutableStateOf("Unknown")
    private val statusTxt = mutableStateOf("No frames")
    private val statusCol = mutableStateOf(Color.Red)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val launcher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                finish() // bail if no camera permission
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(Manifest.permission.CAMERA)
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ShowCamera()
                }
            }
        }
    }

    @Composable
    private fun ShowCamera() {
        val ctx = LocalContext.current

        val preview = remember {
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        }

        // restart camera when resolution changes
        LaunchedEffect(hiRes.value) {
            setupAndStartCamera(preview)
        }

        Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { preview }
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                Text("Frames: ${frames.intValue}", color = Color.White)
                Text("Requested: ${requestedRes.value}", color = Color.White)
                Text("Actual: ${actualRes.value}", color = Color.White)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(statusCol.value)
                    .padding(8.dp)
            ) {
                Text(statusTxt.value, color = Color.White)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Button(
                    onClick = {
                        hiRes.value = !hiRes.value

                        // reset everything when switching
                        frames.intValue = 0
                        requestedRes.value = "Unknown"
                        actualRes.value = "Unknown"
                        statusTxt.value = "No frames"
                        statusCol.value = Color.Red
                    },
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        if (hiRes.value)
                            "Switch to ${FALLBACK_RES.width}x${FALLBACK_RES.height}"
                        else
                            "Switch to Max Resolution"
                    )
                }
            }
        }
    }

    private fun setupAndStartCamera(previewView: PreviewView) {
        val cameraProviderInstance = ProcessCameraProvider.getInstance(this)

        cameraProviderInstance.addListener({
            val provider = cameraProviderInstance.get()

            val camInfo = provider.availableCameraInfos.first {
                CameraSelector.DEFAULT_BACK_CAMERA.filter(listOf(it)).isNotEmpty()
            }

            val maxAvailable = getMaxRes(camInfo)
            val targetRes = if (hiRes.value) maxAvailable else Size(1920, 1080)

            requestedRes.value = "${targetRes.width}x${targetRes.height}"
            Log.d(TAG, "Requesting: ${requestedRes.value}")

            val prev = Preview.Builder().build().apply {
                surfaceProvider = previewView.surfaceProvider
            }

            val analysis = ImageAnalysis.Builder()
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .setResolutionSelector(
                    ResolutionSelector.Builder()
                        .setResolutionStrategy(
                            ResolutionStrategy(
                                targetRes,
                                ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                            )
                        )
                        .build()
                )
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { img ->
                frames.intValue++

                actualRes.value = "${img.width}x${img.height}"

                statusTxt.value = "Getting frames"
                statusCol.value = Color.Green

                if (frames.intValue % 30 == 0) {
                    Log.v(TAG, "Frame ${frames.intValue}: ${img.width}x${img.height}")
                }

                img.close()
            }

            try {
                provider.unbindAll()

                provider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    prev,
                    analysis
                )

                Log.d(TAG, "Bound camera with preview + analysis")

            } catch (e: Exception) {
                Log.e(TAG, "Camera bind failed", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalCamera2Interop::class)
    private fun getMaxRes(cameraInfo: androidx.camera.core.CameraInfo): Size {
        val cam2Info = Camera2CameraInfo.from(cameraInfo)
        val streamConfigMap = cam2Info.getCameraCharacteristic(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )
        val sizes = streamConfigMap?.getOutputSizes(ImageFormat.YUV_420_888)
        val biggest = sizes?.maxByOrNull { it.width * it.height }

        Log.d(TAG, "Standard YUV_420_888 sizes: ${sizes?.joinToString()}")

        return biggest ?: FALLBACK_RES
    }

    companion object {
        private const val TAG = "TEST"
        private val FALLBACK_RES = Size(1920, 1080)
    }
}
