package com.espimsystems.visionlab.feature.detection.presentation.contract

import com.espimsystems.visionlab.core.common.domain.model.Detection

data class DetectionState(
    val detections: List<Detection> = emptyList(),
    val inferenceTime: Double = 0.0,
    val frameWidth: Int = 0,
    val frameHeight: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

sealed interface DetectionIntent {
    data object StartDetection : DetectionIntent
    data object StopDetection : DetectionIntent
}