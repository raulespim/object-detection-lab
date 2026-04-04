package com.espimsystems.visionlab.feature.detection.domain.usecase

import com.espimsystems.visionlab.core.common.domain.model.DetectionBatch
import com.espimsystems.visionlab.core.common.domain.repository.DetectorEngine
import com.espimsystems.visionlab.core.common.domain.repository.FrameSource
import com.espimsystems.visionlab.core.common.domain.repository.ImagePreprocessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Este UseCase é o "Cérebro" da Feature.
 * Ele não sabe COMO a câmera funciona, nem COMO o C++ processa.
 * Ele apenas dita a ORDEM das operações.
 */
class DetectObjectsUseCase @Inject constructor(
    private val frameSource: FrameSource,           // Vem do :core:camera
    private val preprocessor: ImagePreprocessor,    // Vem do :core:cpp (JNI)
    private val detector: DetectorEngine            // Vem do :core:ml (TFLite)
) {

    operator fun invoke(): Flow<DetectionBatch> {
        return frameSource.frames
            .map { rawFrame ->
                // 1. Passa pelo C++ para preparar a imagem (Redimensionar, YUV -> RGB)
                preprocessor.preprocess(rawFrame)
            }
            .map { preprocessedFrame ->
                // 2. Passa pela IA para detectar os objetos
                detector.detect(preprocessedFrame)
            }
    }
}
