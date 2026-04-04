package com.espimsystems.visionlab.feature.detection.presentation.ui

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espimsystems.visionlab.feature.detection.presentation.DetectionViewModel
import com.espimsystems.visionlab.feature.detection.presentation.component.CameraPreview
import com.espimsystems.visionlab.feature.detection.presentation.component.PermissionBox
import com.espimsystems.visionlab.feature.detection.presentation.contract.DetectionIntent

@Composable
fun DetectionScreen(viewModel: DetectionViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Gerenciador do ciclo de vida da câmera para evitar re-bindings desnecessários
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // Dispara a lógica de detecção na ViewModel
    LaunchedEffect(Unit) {
        viewModel.handleIntent(DetectionIntent.StartDetection)
    }

    // Primeiro garantimos a permissão, depois mostramos a UI "foda"
    PermissionBox {
        Box(modifier = Modifier.fillMaxSize()) {

            // Camada 1: Câmera (Hardware)
            CameraPreview { previewView ->
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    // Configura o Stream de visualização
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    // Configura a Análise (IA) com estratégia de descarte para Baixa Latência
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build()

                    try {
                        // Limpa bindings anteriores e vincula ao ciclo de vida do Compose
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (exc: Exception) {
                        // Log sênior: Em produção, enviaríamos para o Crashlytics aqui
                        exc.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))
            }

            // Camada 2: Overlay de Desenho (Visão Computacional)
            // Desenhamos diretamente sobre o frame para garantir 60 FPS
            Canvas(modifier = Modifier.fillMaxSize()) {
                state.detections.forEach { detection ->
                    drawRect(
                        color = Color.Green,
                        topLeft = androidx.compose.ui.geometry.Offset(
                            x = detection.left * size.width,
                            y = detection.top * size.height
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            width = (detection.right - detection.left) * size.width,
                            height = (detection.bottom - detection.top) * size.height
                        ),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Camada 3: UI de Feedback (HUD)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color.Black.copy(alpha = 0.6f),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Espim Systems - Vision Lab",
                        color = Color.Cyan,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Objetos: ${state.detections.size} | Latência: ${state.inferenceTime}ms",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
