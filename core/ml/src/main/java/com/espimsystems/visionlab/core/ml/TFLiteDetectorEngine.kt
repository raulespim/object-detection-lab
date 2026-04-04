package com.espimsystems.visionlab.core.ml

import android.content.Context
import android.util.Log
import com.espimsystems.visionlab.core.common.domain.model.Detection
import com.espimsystems.visionlab.core.common.domain.model.DetectionBatch
import com.espimsystems.visionlab.core.common.domain.model.PreprocessedFrame
import com.espimsystems.visionlab.core.common.domain.repository.DetectorEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureNanoTime

@Singleton
class TFLiteDetectorEngine @Inject constructor(
    @ApplicationContext private val context: Context,
) : DetectorEngine {

    private val gpuDelegate: GpuDelegate?
    private val interpreter: Interpreter
    private val inputDataType: DataType

    override val inputWidth: Int
    override val inputHeight: Int

    private val inputChannels: Int
    private val maxDetections: Int
    private val floatInputBuffer: ByteBuffer?

    init {
        val compatibilityList = CompatibilityList()

        val localGpuDelegate = if (compatibilityList.isDelegateSupportedOnThisDevice) {
            try {
                Log.i(TAG, "GPU delegate habilitado")
                GpuDelegate(compatibilityList.bestOptionsForThisDevice)
            } catch (t: Throwable) {
                Log.w(TAG, "Falha ao criar GPU delegate. Fallback para CPU.", t)
                null
            }
        } else {
            Log.i(TAG, "GPU delegate indisponível. Fallback para CPU com $CPU_THREADS threads")
            null
        }

        gpuDelegate = localGpuDelegate

        val options = Interpreter.Options().apply {
            if (localGpuDelegate != null) {
                addDelegate(localGpuDelegate)
            } else {
                setNumThreads(CPU_THREADS)
            }
        }

        val modelBuffer = loadModelFile(context, MODEL_FILE_NAME)
        interpreter = Interpreter(modelBuffer, options)
        interpreter.allocateTensors()

        val inputTensor = interpreter.getInputTensor(0)
        val inputShape = inputTensor.shape()
        require(inputShape.size == 4) {
            "Shape de input inesperado: ${inputShape.contentToString()}"
        }

        inputDataType = inputTensor.dataType()
        inputHeight = inputShape[1]
        inputWidth = inputShape[2]
        inputChannels = inputShape[3]

        require(inputChannels == 3) {
            "Modelo esperado RGB, mas recebeu $inputChannels canais"
        }

        val scoresShape = interpreter.getOutputTensor(2).shape()
        require(scoresShape.size >= 2) {
            "Shape de scores inesperado: ${scoresShape.contentToString()}"
        }
        maxDetections = scoresShape[1]

        floatInputBuffer = if (inputDataType == DataType.FLOAT32) {
            ByteBuffer.allocateDirect(
                inputWidth * inputHeight * inputChannels * FLOAT_SIZE_BYTES
            ).order(ByteOrder.nativeOrder())
        } else {
            null
        }

        logModelSignature()
    }

    override suspend fun detect(frame: PreprocessedFrame): DetectionBatch {
        val locations = Array(1) { Array(maxDetections) { FloatArray(4) } }
        val classes = Array(1) { FloatArray(maxDetections) }
        val scores = Array(1) { FloatArray(maxDetections) }
        val numDetections = FloatArray(1)

        val outputMap = mutableMapOf<Int, Any>(
            0 to locations,
            1 to classes,
            2 to scores,
            3 to numDetections,
        )

        val inputBuffer = prepareInputBuffer(frame)

        val elapsedNs = measureNanoTime {
            interpreter.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputMap)
        }

        val detections = buildList {
            val count = numDetections[0].toInt().coerceIn(0, maxDetections)

            for (index in 0 until count) {
                val score = scores[0][index]
                if (score < SCORE_THRESHOLD) continue

                val box = locations[0][index]

                add(
                    Detection(
                        label = "Class ${classes[0][index].toInt()}",
                        score = score,
                        top = box[0].coerceIn(0f, 1f),
                        left = box[1].coerceIn(0f, 1f),
                        bottom = box[2].coerceIn(0f, 1f),
                        right = box[3].coerceIn(0f, 1f),
                    )
                )
            }
        }

        return DetectionBatch(
            detections = detections,
            inferenceTimeMs = elapsedNs / 1_000_000.0,
            sourceWidth = frame.sourceWidth,
            sourceHeight = frame.sourceHeight,
        )
    }

    fun close() {
        try {
            interpreter.close()
        } catch (t: Throwable) {
            Log.w(TAG, "Falha ao fechar Interpreter", t)
        }

        try {
            gpuDelegate?.close()
        } catch (t: Throwable) {
            Log.w(TAG, "Falha ao fechar GpuDelegate", t)
        }
    }

    private fun prepareInputBuffer(frame: PreprocessedFrame): Any {
        require(frame.modelInputWidth == inputWidth && frame.modelInputHeight == inputHeight) {
            "Frame pré-processado incompatível. Esperado ${inputWidth}x${inputHeight}, " +
                    "recebido ${frame.modelInputWidth}x${frame.modelInputHeight}"
        }

        frame.rgbBuffer.rewind()

        return when (inputDataType) {
            DataType.UINT8 -> {
                frame.rgbBuffer
            }

            DataType.FLOAT32 -> {
                val target = requireNotNull(floatInputBuffer)
                target.rewind()

                while (frame.rgbBuffer.hasRemaining()) {
                    val channel = frame.rgbBuffer.get().toInt() and 0xFF
                    target.putFloat(channel / 255f)
                }

                target.rewind()
                target
            }

            else -> error("DataType de input não suportado: $inputDataType")
        }
    }

    private fun logModelSignature() {
        Log.i(
            TAG,
            "Modelo carregado. input=${interpreter.getInputTensor(0).shape().contentToString()} $inputDataType | " +
                    "out0=${interpreter.getOutputTensor(0).shape().contentToString()} | " +
                    "out1=${interpreter.getOutputTensor(1).shape().contentToString()} | " +
                    "out2=${interpreter.getOutputTensor(2).shape().contentToString()} | " +
                    "out3=${interpreter.getOutputTensor(3).shape().contentToString()}"
        )
    }

    private fun loadModelFile(context: Context, assetName: String): ByteBuffer {
        context.assets.openFd(assetName).use { assetFileDescriptor ->
            FileInputStream(assetFileDescriptor.fileDescriptor).channel.use { fileChannel ->
                return fileChannel.map(
                    FileChannel.MapMode.READ_ONLY,
                    assetFileDescriptor.startOffset,
                    assetFileDescriptor.declaredLength,
                )
            }
        }
    }

    private companion object {
        const val TAG = "TFLiteDetector"
        const val MODEL_FILE_NAME = "model.tflite"
        const val SCORE_THRESHOLD = 0.25f
        const val CPU_THREADS = 4
        const val FLOAT_SIZE_BYTES = 4
    }
}