package com.espimsystems.visionlab.feature.detection.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.espimsystems.visionlab.core.common.domain.model.Detection

@Composable
fun DetectionOverlay(
    detections: List<Detection>,
    sourceWidth: Int,
    sourceHeight: Int,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (sourceWidth <= 0 || sourceHeight <= 0) return@Canvas

        val previewRect = calculateContainedRect(
            containerWidth = size.width,
            containerHeight = size.height,
            sourceWidth = sourceWidth.toFloat(),
            sourceHeight = sourceHeight.toFloat(),
        )

        detections.forEach { detection ->
            val left = previewRect.left + (detection.left.coerceIn(0f, 1f) * previewRect.width)
            val top = previewRect.top + (detection.top.coerceIn(0f, 1f) * previewRect.height)
            val right = previewRect.left + (detection.right.coerceIn(0f, 1f) * previewRect.width)
            val bottom = previewRect.top + (detection.bottom.coerceIn(0f, 1f) * previewRect.height)

            val rectWidth = right - left
            val rectHeight = bottom - top
            if (rectWidth <= 0f || rectHeight <= 0f) return@forEach

            drawRect(
                color = Color(0xFF00E676),
                topLeft = Offset(left, top),
                size = Size(rectWidth, rectHeight),
                style = Stroke(width = 3.dp.toPx()),
            )
        }
    }
}

private fun calculateContainedRect(
    containerWidth: Float,
    containerHeight: Float,
    sourceWidth: Float,
    sourceHeight: Float,
): Rect {
    val sourceAspect = sourceWidth / sourceHeight
    val containerAspect = containerWidth / containerHeight

    return if (sourceAspect > containerAspect) {
        val scaledHeight = containerWidth / sourceAspect
        val top = (containerHeight - scaledHeight) / 2f
        Rect(
            left = 0f,
            top = top,
            right = containerWidth,
            bottom = top + scaledHeight,
        )
    } else {
        val scaledWidth = containerHeight * sourceAspect
        val left = (containerWidth - scaledWidth) / 2f
        Rect(
            left = left,
            top = 0f,
            right = left + scaledWidth,
            bottom = containerHeight,
        )
    }
}