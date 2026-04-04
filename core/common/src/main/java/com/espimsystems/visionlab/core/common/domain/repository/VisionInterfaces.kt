package com.espimsystems.visionlab.core.common.domain.repository

import com.espimsystems.visionlab.core.common.domain.model.CameraFrame
import com.espimsystems.visionlab.core.common.domain.model.DetectionBatch
import com.espimsystems.visionlab.core.common.domain.model.PreprocessedFrame
import kotlinx.coroutines.flow.Flow

interface FrameSource {
    val frames: Flow<CameraFrame>
}

interface ImagePreprocessor {
    suspend fun preprocess(
        frame: CameraFrame,
        targetWidth: Int,
        targetHeight: Int,
    ): PreprocessedFrame
}

interface DetectorEngine {
    val inputWidth: Int
    val inputHeight: Int

    suspend fun detect(frame: PreprocessedFrame): DetectionBatch
}