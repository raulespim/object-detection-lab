package com.espimsystems.visionlab.feature.detection.domain.usecase

import com.espimsystems.visionlab.core.common.domain.model.DetectionBatch
import com.espimsystems.visionlab.core.common.domain.repository.DetectorEngine
import com.espimsystems.visionlab.core.common.domain.repository.FrameSource
import com.espimsystems.visionlab.core.common.domain.repository.ImagePreprocessor
import com.espimsystems.visionlab.core.dispatchers.AppDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

class DetectObjectsUseCase @Inject constructor(
    private val frameSource: FrameSource,
    private val preprocessor: ImagePreprocessor,
    private val detector: DetectorEngine,
    private val dispatchers: AppDispatchers
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<DetectionBatch> = frameSource.frames
        .mapLatest { rawFrame ->
            val preprocessedFrame = preprocessor.preprocess(
                frame = rawFrame,
                targetWidth = detector.inputWidth,
                targetHeight = detector.inputHeight,
            )
            detector.detect(preprocessedFrame)
        }
        .flowOn(dispatchers.default)
}
