package com.espimsystems.visionlab.feature.detection.presentation.ui

import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espimsystems.visionlab.core.camera.CameraFrameSource
import com.espimsystems.visionlab.feature.detection.presentation.DetectionViewModel
import com.espimsystems.visionlab.feature.detection.presentation.component.CameraPreview
import com.espimsystems.visionlab.feature.detection.presentation.component.DetectionOverlay
import com.espimsystems.visionlab.feature.detection.presentation.component.PermissionBox
import com.espimsystems.visionlab.feature.detection.presentation.contract.DetectionIntent
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun DetectionScreen(
    viewModel: DetectionViewModel,
    frameSource: CameraFrameSource,
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val analysisExecutor = rememberSingleThreadExecutor()

    LaunchedEffect(Unit) {
        viewModel.handleIntent(DetectionIntent.StartDetection)
    }

    PermissionBox {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview { previewView ->
                cameraProviderFuture.addListener(
                    {
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder()
                            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                            .build()
                            .also { it.surfaceProvider = previewView.surfaceProvider }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                    frameSource.processImageProxy(imageProxy)
                                }
                            }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis,
                            )
                        } catch (throwable: Throwable) {
                            throwable.printStackTrace()
                        }
                    },
                    ContextCompat.getMainExecutor(context),
                )
            }

            DetectionOverlay(
                detections = state.detections,
                sourceWidth = state.frameWidth,
                sourceHeight = state.frameHeight,
                modifier = Modifier.fillMaxSize(),
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color.Black.copy(alpha = 0.65f),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Espim Systems - Vision Lab",
                        color = Color.Cyan,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Objetos: ${state.detections.size} | Latência: ${"%.1f".format(state.inferenceTime)} ms",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (state.errorMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.errorMessage,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            viewModel.handleIntent(DetectionIntent.StopDetection)
            analysisExecutor.shutdown()
            runCatching { cameraProviderFuture.get().unbindAll() }
        }
    }
}

@Composable
private fun rememberSingleThreadExecutor(): ExecutorService = remember {
    Executors.newSingleThreadExecutor()
}
