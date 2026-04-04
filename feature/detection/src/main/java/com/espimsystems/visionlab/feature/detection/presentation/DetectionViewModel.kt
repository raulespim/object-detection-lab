package com.espimsystems.visionlab.feature.detection.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espimsystems.visionlab.feature.detection.domain.usecase.DetectObjectsUseCase
import com.espimsystems.visionlab.feature.detection.presentation.contract.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetectionViewModel @Inject constructor(
    private val detectObjectsUseCase: DetectObjectsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DetectionState())
    val state: StateFlow<DetectionState> = _state.asStateFlow()

    fun handleIntent(intent: DetectionIntent) {
        when (intent) {
            DetectionIntent.StartDetection -> startPipeline()
            DetectionIntent.StopDetection -> stopPipeline()
        }
    }

    private fun startPipeline() {
        viewModelScope.launch {
            detectObjectsUseCase()
                .onStart { _state.update { it.copy(isLoading = false) } }
                .catch { e -> _state.update { it.copy(errorMessage = e.message) } }
                .collect { batch ->
                    _state.update {
                        it.copy(
                            detections = batch.detections,
                            inferenceTime = batch.inferenceTimeMs
                        )
                    }
                }
        }
    }

    private fun stopPipeline() { /* Lógica para cancelar o Job se necessário */ }
}
