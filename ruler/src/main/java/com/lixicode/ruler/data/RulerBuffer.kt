package com.lixicode.ruler.data

import android.graphics.Matrix
import com.lixicode.ruler.Axis
import com.lixicode.ruler.utils.ViewPortHandler
import kotlin.math.max

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/28
 */

class RulerBuffer(size: Int) {


    val matrix = Matrix()
    val dividerLineBuffer: FloatArray = FloatArray(size * 4 * 2)
    val scaleLineBuffer: FloatArray = FloatArray(size * 2)


    fun feed(axis: Axis, viewPort: ViewPortHandler) {
        var scaleLineIndex = 0
        var dividerLineIndex = 0


        val left = viewPort.contentLeft


        val offsetTopAndBottom = max(
            axis.scaleLineOptions.offset(),
            axis.baselineOptions?.offset() ?: 0F
        )
        viewPort.offsetRect.top = offsetTopAndBottom
        if (axis.repeat) {
            viewPort.offsetRect.bottom = offsetTopAndBottom
        }

        // 绘制刻度线
        val spacing = axis.dividerLineWidthWithSpacing + axis.scaleLineOptions.width
        val scaleLineOffset = axis.scaleLineOptions.offset()

        // 绘制刻度分隔线
        axis.dividerLineOptions?.run {
            val dividerLineSpacing = axis.dividerLineSpacing
            val dividerLineOffsetX = offset()

            repeat(axis.visibleRangeMaximun) { index ->
                val scaleLineStartX = left + spacing * index
                scaleLineBuffer[scaleLineIndex++] = scaleLineStartX + scaleLineOffset
                scaleLineBuffer[scaleLineIndex++] = axis.scaleLineOptions.size

                val dividerLineStartX = scaleLineStartX + axis.scaleLineOptions.width
                repeat(axis.dividerLineCount) { n ->
                    val dividerLineLeft = dividerLineStartX + width * n + dividerLineSpacing * (n + 1)
                    dividerLineBuffer[dividerLineIndex++] = dividerLineLeft + dividerLineOffsetX
                    dividerLineBuffer[dividerLineIndex++] = size
                }
            }
        } ?: repeat(axis.visibleRangeMaximun) { index ->
            val scaleLineStartX = left + axis.scaleLineOptions.width + spacing * index
            scaleLineBuffer[scaleLineIndex++] = scaleLineStartX + scaleLineOffset
            scaleLineBuffer[scaleLineIndex++] = axis.scaleLineOptions.size
        }

    }


    fun translation(dx: Float, dy: Float) {
        matrix.reset()
        matrix.postTranslate(dx, dy)
        matrix.mapPoints(scaleLineBuffer)
        matrix.mapPoints(dividerLineBuffer)
    }

}
