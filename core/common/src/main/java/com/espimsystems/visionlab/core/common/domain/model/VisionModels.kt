package com.espimsystems.visionlab.core.common.domain.model

import java.nio.ByteBuffer

data class CameraPlane(
    val buffer: ByteBuffer,
    val rowStride: Int,
    val pixelStride: Int
)

data class CameraFrame(
    val width: Int,
    val height: Int,
    val rotationDegrees: Int,
    val timestampNs: Long,
    val yPlane: CameraPlane,
    val uPlane: CameraPlane,
    val vPlane: CameraPlane
)

data class PreprocessedFrame(
    val modelInputWidth: Int,
    val modelInputHeight: Int,
    val sourceWidth: Int,
    val sourceHeight: Int,
    val rgbBuffer: ByteBuffer
)

data class DetectionBatch(
    val detections: List<Detection>,
    val inferenceTimeMs: Double,
    val sourceWidth: Int,
    val sourceHeight: Int
)

data class Detection(
    val label: String,
    val score: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)