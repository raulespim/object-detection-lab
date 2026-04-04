package com.espimsystems.visionlab.core.cpp

import android.util.Log
import com.espimsystems.visionlab.core.common.domain.model.CameraFrame
import com.espimsystems.visionlab.core.common.domain.model.PreprocessedFrame
import com.espimsystems.visionlab.core.common.domain.repository.ImagePreprocessor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NativeImageProcessor @Inject constructor() : ImagePreprocessor {

    override suspend fun preprocess(
        frame: CameraFrame,
        targetWidth: Int,
        targetHeight: Int,
    ): PreprocessedFrame {
        val outputBuffer = ByteBuffer.allocateDirect(targetWidth * targetHeight * RGB_CHANNELS)
            .order(ByteOrder.nativeOrder())

        processFrameNative(
            yBuffer = frame.yPlane.buffer,
            yRowStride = frame.yPlane.rowStride,
            yPixelStride = frame.yPlane.pixelStride,
            uBuffer = frame.uPlane.buffer,
            uRowStride = frame.uPlane.rowStride,
            uPixelStride = frame.uPlane.pixelStride,
            vBuffer = frame.vPlane.buffer,
            vRowStride = frame.vPlane.rowStride,
            vPixelStride = frame.vPlane.pixelStride,
            width = frame.width,
            height = frame.height,
            rotationDegrees = frame.rotationDegrees,
            targetWidth = targetWidth,
            targetHeight = targetHeight,
            outputBuffer = outputBuffer,
        )

        val sourceWidth = if (frame.rotationDegrees == 90 || frame.rotationDegrees == 270) {
            frame.height
        } else {
            frame.width
        }

        val sourceHeight = if (frame.rotationDegrees == 90 || frame.rotationDegrees == 270) {
            frame.width
        } else {
            frame.height
        }

        outputBuffer.rewind()
        Log.d(TAG, "Frame pré-processado: ${frame.width}x${frame.height} rot=${frame.rotationDegrees} -> ${targetWidth}x${targetHeight}")

        return PreprocessedFrame(
            modelInputWidth = targetWidth,
            modelInputHeight = targetHeight,
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            rgbBuffer = outputBuffer,
        )
    }

    private external fun processFrameNative(
        yBuffer: ByteBuffer,
        yRowStride: Int,
        yPixelStride: Int,
        uBuffer: ByteBuffer,
        uRowStride: Int,
        uPixelStride: Int,
        vBuffer: ByteBuffer,
        vRowStride: Int,
        vPixelStride: Int,
        width: Int,
        height: Int,
        rotationDegrees: Int,
        targetWidth: Int,
        targetHeight: Int,
        outputBuffer: ByteBuffer,
    )

    private companion object {
        init {
            System.loadLibrary("vision_native")
        }

        const val TAG = "NativeImageProcessor"
        const val RGB_CHANNELS = 3
    }
}