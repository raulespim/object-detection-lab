package com.espimsystems.visionlab.core.camera

import android.util.Log
import androidx.camera.core.ImageProxy
import com.espimsystems.visionlab.core.common.domain.model.CameraFrame
import com.espimsystems.visionlab.core.common.domain.model.CameraPlane
import com.espimsystems.visionlab.core.common.domain.repository.FrameSource
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraFrameSource @Inject constructor() : FrameSource {

    private val _frameFlow = MutableSharedFlow<CameraFrame>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val frames: Flow<CameraFrame> = _frameFlow.asSharedFlow()

    fun processImageProxy(imageProxy: ImageProxy) {
        try {
            val frame = imageProxy.toOwnedCameraFrame()
            val emitted = _frameFlow.tryEmit(frame)
            if (!emitted) {
                Log.v(TAG, "Frame descartado por backpressure")
            }
        } catch (throwable: Throwable) {
            Log.e(TAG, "Falha ao converter ImageProxy em CameraFrame", throwable)
        } finally {
            imageProxy.close()
        }
    }

    private fun ImageProxy.toOwnedCameraFrame(): CameraFrame {
        require(planes.size == 3) { "Esperado YUV_420_888 com 3 planos, mas recebeu ${planes.size}" }

        val planeY = planes[0]
        val planeU = planes[1]
        val planeV = planes[2]

        return CameraFrame(
            width = width,
            height = height,
            rotationDegrees = imageInfo.rotationDegrees,
            timestampNs = imageInfo.timestamp,
            yPlane = planeY.toOwnedPlane(),
            uPlane = planeU.toOwnedPlane(),
            vPlane = planeV.toOwnedPlane(),
        )
    }

    private fun ImageProxy.PlaneProxy.toOwnedPlane(): CameraPlane {
        val source = buffer.duplicate().apply { rewind() }
        val ownedBuffer = ByteBuffer.allocateDirect(source.remaining())
            .order(ByteOrder.nativeOrder())
        ownedBuffer.put(source)
        ownedBuffer.rewind()

        return CameraPlane(
            buffer = ownedBuffer,
            rowStride = rowStride,
            pixelStride = pixelStride,
        )
    }

    private companion object {
        const val TAG = "CameraFrameSource"
    }
}
