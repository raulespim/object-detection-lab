package com.espimsystems.visionlab.core.cpp

import com.espimsystems.visionlab.core.common.domain.model.CameraFrame
import com.espimsystems.visionlab.core.common.domain.model.PreprocessedFrame
import com.espimsystems.visionlab.core.common.domain.repository.ImagePreprocessor
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NativeImageProcessor @Inject constructor() : ImagePreprocessor {

    // Load the lib .so compiled from C++
    companion object {
        init {
            System.loadLibrary("vision_native")
        }
    }

    override suspend fun preprocess(frame: CameraFrame): PreprocessedFrame {
        // Chamada para o C++ (NDK)
        processFrameNative(
            yBuffer = frame.yBuffer,
            width = frame.width,
            height = frame.height
        )

        return PreprocessedFrame(
            width = frame.width,
            height = frame.height,
            rgbBuffer = frame.yBuffer // Mock por enquanto
        )
    }

    // O nome aqui deve bater com o que o C++ espera
    private external fun processFrameNative(
        yBuffer: ByteBuffer,
        width: Int,
        height: Int
    )
}
