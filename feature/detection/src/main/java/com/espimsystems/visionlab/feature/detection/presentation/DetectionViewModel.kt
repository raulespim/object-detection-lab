package com.espimsystems.visionlab.feature.detection.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espimsystems.visionlab.feature.detection.domain.usecase.DetectObjectsUseCase
import com.espimsystems.visionlab.feature.detection.presentation.contract.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetectionViewModel @Inject constructor(
    private val detectObjectsUseCase: DetectObjectsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(DetectionState())
    val state = _state.asStateFlow()

    private var detectionJob: Job? = null

    fun handleIntent(intent: DetectionIntent) {
        when (intent) {
            DetectionIntent.StartDetection -> startPipeline()
            DetectionIntent.StopDetection -> stopPipeline()
        }
    }

    private fun startPipeline() {
        if (detectionJob?.isActive == true) return

        detectionJob = viewModelScope.launch(Dispatchers.Default) {
            detectObjectsUseCase()
                .catch { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Erro desconhecido no pipeline",
                        )
                    }
                }
                .collect { batch ->
                    _state.update {
                        it.copy(
                            detections = batch.detections,
                            inferenceTime = batch.inferenceTimeMs,
                            frameWidth = batch.sourceWidth,
                            frameHeight = batch.sourceHeight,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
        }
    }

    private fun stopPipeline() {
        detectionJob?.cancel()
        detectionJob = null
        _state.update { current ->
            current.copy(
                detections = emptyList(),
                inferenceTime = 0.0,
            )
        }
    }

    override fun onCleared() {
        stopPipeline()
        super.onCleared()
    }
}
