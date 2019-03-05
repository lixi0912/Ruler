package com.lixicode.ruler.renderer

import android.graphics.Canvas
import android.view.View
import com.lixicode.ruler.XAxis
import com.lixicode.ruler.data.FSize
import com.lixicode.ruler.data.createPaint
import com.lixicode.ruler.data.offset
import com.lixicode.ruler.utils.Transformer
import com.lixicode.run.ui.view.RulerView
import kotlin.math.max

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
class XAxisRenderer(view: RulerView) : Renderer(view) {

    private val baseLinePaint by lazy {
        this.view.axis.baselineOptions.createPaint()
    }


    private val scaleLinePaint by lazy {
        this.view.axis.scaleLineOptions.createPaint()
    }

    private val dividerLinePaint by lazy {
        this.view.axis.dividerLineOptions.createPaint()
    }

    override fun computeSize(
        widthMeasureSpec: Int,
        minimunWidth: Float,
        heightMeasureSpec: Int,
        minimunHeight: Float
    ): FSize {

        val xAxis = view.axis

        val width = when {
            View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.EXACTLY -> {
                View.MeasureSpec.getSize(widthMeasureSpec).toFloat()
            }
            else -> {

                val minScaleLineWidth =
                    xAxis.scaleLineOptions.widthNeeded * xAxis.visibleRangeMinimum + xAxis.scaleLineOptions.offset() * 2


                val minDividerLineWidth: Float = if (xAxis.dividerLineOptions.enable) {
                    xAxis.dividerLineOptions.widthNeeded * xAxis.visibleDividerLineCount
                    +xAxis.visibleDividerLineSpacingCount * xAxis.dividerLineSpacing
                } else {
                    0F
                }

                max(minimunWidth, minScaleLineWidth + minDividerLineWidth)
            }
        }


        val height = when {
            View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.EXACTLY -> {
                val height = View.MeasureSpec.getSize(heightMeasureSpec).toFloat()
                xAxis.scaleLineOptions.size = height * xAxis.scaleLineOptions.ratioOfParent /
                        if (xAxis.repeat) 2 else 1

                if (xAxis.dividerLineOptions.enable) {
                    xAxis.dividerLineOptions.size = xAxis.scaleLineOptions.size * xAxis.dividerLineOptions.ratioOfParent
                }
                height
            }
            else -> {
                if (xAxis.scaleLineOptions.size == 0F && xAxis.scaleLineOptions.ratioOfParent > 0) {
                    xAxis.scaleLineOptions.size = minimunHeight / xAxis.scaleLineOptions.ratioOfParent
                }

                val baseLineWidth = if (xAxis.baselineOptions.enable) {
                    xAxis.baselineOptions.widthNeeded
                } else {
                    0F
                }

                val scaleLineSize = (xAxis.scaleLineOptions.size + baseLineWidth) *
                        if (xAxis.repeat) 2 else 1

                if (xAxis.dividerLineOptions.enable) {
                    xAxis.dividerLineOptions.size = xAxis.scaleLineOptions.size * xAxis.dividerLineOptions.ratioOfParent
                }

                minimunHeight + scaleLineSize
            }
        }


        val viewPort = view.viewPort
        viewPort.offsetRect.left += xAxis.scaleLineOptions.offset()
        if (xAxis.baselineOptions.enable) {
            viewPort.offsetRect.top += xAxis.baselineOptions.offset()
        }


        return FSize.obtain(width, height)
    }


    override fun draw(canvas: Canvas, transformer: Transformer) {
        val viewPort = view.viewPort
        val xAxis = view.axis

        // 绘制基准线
        if (xAxis.baselineOptions.enable) {

            val x = view.scrollX + view.paddingLeft
            val yPx = viewPort.contentTop
            canvas.drawLine(
                x.toFloat(),
                yPx,
                (x + view.width - view.paddingRight).toFloat(),
                yPx,
                baseLinePaint
            )
        }

        for (x in xAxis.minValue until xAxis.maxValue) {
            val y = when (x % xAxis.scaleLineStep) {
                0, xAxis.scaleLineStep -> 1F
                xAxis.scaleLineStep / 2 -> xAxis.dividerLineOptions.ratioOfParent + 0.1F
                else -> xAxis.dividerLineOptions.ratioOfParent
            }

            val isDividerLine = y != 1F
            if (isDividerLine) {
                if (xAxis.dividerLineOptions.enable) {
                    dividerLinePaint
                } else {
                    null
                }
            } else {
                scaleLinePaint
            }?.run {
                val point = FSize.obtain(x.toFloat(), y)
                transformer.pointValuesToPixel(point)
                canvas.drawLine(
                    point.x,
                    viewPort.contentTop,
                    point.x,
                    point.y,
                    this
                )
                point.recycle()
            }
        }
    }

}
