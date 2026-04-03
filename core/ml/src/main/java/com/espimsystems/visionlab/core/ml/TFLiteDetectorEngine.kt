package com.espimsystems.visionlab.core.ml

import com.espimsystems.visionlab.core.common.domain.model.DetectionBatch
import com.espimsystems.visionlab.core.common.domain.model.PreprocessedFrame
import com.espimsystems.visionlab.core.common.domain.repository.DetectorEngine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TFLiteDetectorEngine @Inject constructor(
    // Aqui injetaremos o Interpreter do TFLite depois via Hilt
) : DetectorEngine {

    override suspend fun detect(frame: PreprocessedFrame): DetectionBatch {
        // 1. Aqui entrará a lógica de inferência do TFLite
        // 2. Por enquanto, retornamos um mock para validar a arquitetura
        return DetectionBatch(
            detections = emptyList(),
            inferenceTimeMs = 0.0
        )
    }
}
