package com.espimsystems.visionlab.core.common.domain.model

import java.nio.ByteBuffer

data class CameraFrame(
    val width: Int,
    val height: Int,
    val rotationDegrees: Int,
    val timestampNs: Long,
    val yBuffer: ByteBuffer,
    val uBuffer: ByteBuffer,
    val vBuffer: ByteBuffer,
    val yRowStride: Int,
    val uvRowStride: Int,
    val uvPixelStride: Int
)

data class PreprocessedFrame(
    val width: Int,
    val height: Int,
    val rgbBuffer: ByteBuffer
)

data class DetectionBatch(
    val detections: List<Detection>,
    val inferenceTimeMs: Double
)

data class Detection(
    val label: String,
    val score: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)