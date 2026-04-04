package com.espimsystems.visionlab.core.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.espimsystems.visionlab.core.common.domain.model.CameraFrame
import com.espimsystems.visionlab.core.common.domain.repository.FrameSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraFrameSource @Inject constructor() : FrameSource {

    // Executor para processar a análise fora da Main Thread
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    override val frames: Flow<CameraFrame> = callbackFlow {
        val analyzer = ImageAnalysis.Analyzer { imageProxy ->
            // Convertemos o frame da CameraX para o nosso modelo comum
            val frame = imageProxy.toCameraFrame()

            // Enviamos para o Flow. Se o buffer estiver cheio, descartamos (estratégia real-time)
            trySend(frame)

            // Importante: Fechar o imageProxy para liberar a câmera para o próximo frame
            imageProxy.close()
        }

        // Aqui você configurará o ImageAnalysis do CameraX depois no Passo da UI
        // Por enquanto, o Flow está pronto para receber os dados

        awaitClose {
            cameraExecutor.shutdown()
        }
    }

    // Função de extensão para converter ImageProxy em CameraFrame
    private fun ImageProxy.toCameraFrame(): CameraFrame {
        val planeY = planes[0]
        val planeU = planes[1]
        val planeV = planes[2]

        return CameraFrame(
            width = width,
            height = height,
            rotationDegrees = imageInfo.rotationDegrees,
            timestampNs = imageInfo.timestamp,
            yBuffer = planeY.buffer,
            uBuffer = planeU.buffer,
            vBuffer = planeV.buffer,
            yRowStride = planeY.rowStride,
            uvRowStride = planeU.rowStride,
            uvPixelStride = planeU.pixelStride
        )
    }
}
