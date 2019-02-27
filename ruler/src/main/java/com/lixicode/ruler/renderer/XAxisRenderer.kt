package com.lixicode.ruler.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import com.lixicode.ruler.XAxis
import com.lixicode.ruler.utils.ViewPortHandler
import kotlin.math.max

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
class XAxisRenderer(val xAxis: XAxis, viewPort: ViewPortHandler) : Renderer(viewPort) {

    private val baseLinePaint by lazy {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        xAxis.baselineOptions?.run {
            paint.color = color
            paint.strokeWidth = width
            paint.strokeCap = Paint.Cap.ROUND
        }
        paint
    }


    override fun computeMinimumWidth(widthMeasureSpec: Int, minimunWidth: Float): Float {
        val mode = View.MeasureSpec.getMode(widthMeasureSpec)
        return if (mode == View.MeasureSpec.EXACTLY) {
            val viewWidth = View.MeasureSpec.getSize(widthMeasureSpec).toFloat()
            val scaleWidth = xAxis.scaleLineOptions.width * xAxis.visibleRangeMaximun
            xAxis.dividerOptions?.run {
                val usedWidth = scaleWidth + width * xAxis.visibleDividerCount
                xAxis.dividerSpacing = (viewWidth - usedWidth) / xAxis.visibleDividerSpacingCount
            }
            viewWidth
        } else {
            val minScaleWidth = xAxis.scaleLineOptions.width * xAxis.visibleRangeMaximun
            val minDividerWidth: Float = xAxis.dividerOptions?.run {
                width * xAxis.visibleDividerCount + xAxis.visibleDividerSpacingCount * xAxis.dividerSpacing
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
            xAxis.dividerOptions?.run {
                size = xAxis.scaleLineOptions.size / ratioOfParent
            }
            height
        } else {
            if (xAxis.scaleLineOptions.size == 0F && xAxis.scaleLineOptions.ratioOfParent > 0) {
                xAxis.scaleLineOptions.size = minimunHeight / xAxis.scaleLineOptions.ratioOfParent
            }
            val scaleLineSize = (xAxis.scaleLineOptions.size + (xAxis.baselineOptions?.width ?: 0F)) *
                    if (xAxis.repeat) 2 else 1
            minimunHeight + scaleLineSize
        }
    }

    override fun draw(canvas: Canvas) {
        xAxis.baselineOptions?.run {
            // draw base line
            val yPx = viewPort.contentTop + width
            canvas.drawLine(
                viewPort.contentLeft + width,
                yPx,
                viewPort.contentRight - width,
                yPx,
                baseLinePaint
            )
        }


        xAxis.scaleLineOptions.run {





        }
    }

}
