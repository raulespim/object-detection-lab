package com.espimsystems.visionlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.espimsystems.visionlab.feature.detection.presentation.DetectionViewModel
import com.espimsystems.visionlab.feature.detection.presentation.ui.DetectionScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: DetectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Aqui você aplicaria o seu Design System (Theme)
            DetectionScreen(viewModel = viewModel)
        }
    }
}
