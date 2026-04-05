package com.espimsystems.visionlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.espimsystems.visionlab.core.camera.CameraFrameSource
import com.espimsystems.visionlab.core.designsystem.theme.VisionLabTheme
import com.espimsystems.visionlab.feature.detection.presentation.DetectionViewModel
import com.espimsystems.visionlab.feature.detection.presentation.ui.DetectionScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: DetectionViewModel by viewModels()

    @Inject
    lateinit var cameraFrameSource: CameraFrameSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VisionLabTheme {
                DetectionScreen(
                    viewModel = viewModel,
                    frameSource = cameraFrameSource,
                )
            }
        }
    }
}
