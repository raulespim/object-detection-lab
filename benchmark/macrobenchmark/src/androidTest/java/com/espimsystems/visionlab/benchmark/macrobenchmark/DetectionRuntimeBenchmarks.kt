package com.espimsystems.visionlab.benchmark.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalMetricApi::class)
@LargeTest
@RunWith(AndroidJUnit4::class)
class DetectionRuntimeBenchmarks {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun detectionScreen_runtime() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(
            FrameTimingMetric(),
            TraceSectionMetric("camera.frame.copy"),
            TraceSectionMetric("cpp.preprocess"),
            TraceSectionMetric("tflite.inference"),
        ),
        iterations = DEFAULT_ITERATIONS,
        compilationMode = CompilationMode.Partial(),
        startupMode = null,
    ) {
        pressHome()
        startActivityAndWait()

        // Janela simples para capturar runtime do pipeline já ativo.
        device.waitForIdle()
        Thread.sleep(5_000)
    }
}