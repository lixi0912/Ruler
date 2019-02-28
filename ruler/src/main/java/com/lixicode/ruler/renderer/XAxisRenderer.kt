package com.lixicode.ruler.renderer

import android.graphics.Canvas
import android.view.View
import com.lixicode.ruler.XAxis
import com.lixicode.ruler.data.RulerBuffer
import com.lixicode.ruler.data.createPaint
import com.lixicode.ruler.data.offset
import com.lixicode.ruler.utils.ViewPortHandler
import kotlin.math.max

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
class XAxisRenderer(viewPort: ViewPortHandler, private val xAxis: XAxis) : Renderer(viewPort) {

    private val baseLinePaint by lazy {
        xAxis.baselineOptions?.createPaint()
    }


    private val scaleLinePaint by lazy {
        xAxis.scaleLineOptions.createPaint()
    }

    private val dividerLinePaint by lazy {
        xAxis.dividerLineOptions?.createPaint()
    }


    override fun computeMinimumWidth(widthMeasureSpec: Int, minimunWidth: Float): Float {
        val mode = View.MeasureSpec.getMode(widthMeasureSpec)
        return if (mode == View.MeasureSpec.EXACTLY) {
            val viewWidth = View.MeasureSpec.getSize(widthMeasureSpec).toFloat()
            val scaleLineWidth = xAxis.scaleLineOptions.width * xAxis.visibleRangeMaximun
            xAxis.dividerLineOptions?.run {
                val usedWidth = scaleLineWidth + width * xAxis.visibleDividerLineCount
                xAxis.dividerLineSpacing = (viewWidth - usedWidth) / xAxis.visibleDividerLineSpacingCount
            }
            viewWidth
        } else {
            val minScaleWidth =
                xAxis.scaleLineOptions.width * xAxis.visibleRangeMaximun + xAxis.scaleLineOptions.offset() * 2

            val minDividerWidth: Float = xAxis.dividerLineOptions?.run {
                val minWidth =
                    width * xAxis.visibleDividerLineCount + xAxis.visibleDividerLineSpacingCount * xAxis.dividerLineSpacing
                if (minimunWidth - minScaleWidth - minWidth > 0) {
                    xAxis.dividerLineSpacing = (minimunWidth - minScaleWidth - width
                            * xAxis.visibleDividerLineCount) / xAxis.visibleDividerLineSpacingCount
                }
                minWidth
            } ?: 0F

            max(minimunWidth, minScaleWidth + minDividerWidth)
        }
    }

    override fun computeMinimumHeight(heightMeasureSpec: Int, minimunHeight: Float): Float {
        val mode = View.MeasureSpec.getMode(heightMeasureSpec)
        return if (mode == View.MeasureSpec.EXACTLY) {
            val height = View.MeasureSpec.getSize(heightMeasureSpec).toFloat()
            xAxis.scaleLineOptions.size = height * xAxis.scaleLineOptions.ratioOfParent /
                    if (xAxis.repeat) 2 else 1
            xAxis.dividerLineOptions?.run {
                size = xAxis.scaleLineOptions.size * ratioOfParent
            }
            height
        } else {
            if (xAxis.scaleLineOptions.size == 0F && xAxis.scaleLineOptions.ratioOfParent > 0) {
                xAxis.scaleLineOptions.size = minimunHeight / xAxis.scaleLineOptions.ratioOfParent
            }
            val scaleLineSize = (xAxis.scaleLineOptions.size + (xAxis.baselineOptions?.width ?: 0F)) *
                    if (xAxis.repeat) 2 else 1
            xAxis.dividerLineOptions?.run {
                size = xAxis.scaleLineOptions.size * ratioOfParent
            }
            minimunHeight + scaleLineSize
        }
    }

    override fun draw(canvas: Canvas, buffer: RulerBuffer) {

        // 绘制基准线
        xAxis.baselineOptions?.run {
            // draw base line
            val yPx = viewPort.contentTop
            canvas.drawLine(
                viewPort.contentLeft,
                yPx,
                viewPort.contentRight,
                yPx,
                baseLinePaint!!
            )
        }


        // 绘制 刻度线
        for (index in 0 until buffer.scaleLineBuffer.size step 2) {
            val x = buffer.scaleLineBuffer[index]
            val y = buffer.scaleLineBuffer[index + 1]

            canvas.drawLine(
                x,
                viewPort.contentTop,
                x,
                y,
                scaleLinePaint
            )
        }


        // 绘制刻度间隔线
        for (index in 0 until buffer.dividerLineBuffer.size step 2) {

            val x = buffer.dividerLineBuffer[index]
            val y = buffer.dividerLineBuffer[index + 1]

            canvas.drawLine(
                x,
                viewPort.contentTop,
                x,
                y,
                dividerLinePaint!!
            )
        }
    }

}
