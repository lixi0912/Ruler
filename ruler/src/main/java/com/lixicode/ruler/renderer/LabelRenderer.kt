package com.lixicode.ruler.renderer

import android.graphics.Canvas
import android.text.TextUtils
import android.view.View
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.data.*
import com.lixicode.ruler.formatter.ValueFormatter
import com.lixicode.ruler.utils.Transformer
import kotlin.math.abs
import kotlin.math.min

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
class LabelRenderer(
    view: RulerView
) : Renderer(view) {

    override fun computeSize(
        widthMeasureSpec: Int,
        minimunWidth: Float,
        heightMeasureSpec: Int,
        minimunHeight: Float
    ): FSize {
        val viewPort = view.viewPort
        val xAxis = view.axis
        val labelOptions = xAxis.labelOptions

        val measuredText = view.getLongestMeasuredText()

        val width = when {
            View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.EXACTLY -> {
                View.MeasureSpec.getSize(
                    widthMeasureSpec
                ).toFloat().apply {
                    labelOptions.autoTextSize(measuredText, this / xAxis.visibleRangeMinimum)
                }
            }
            else -> {
                val longestTextWidth = labelOptions.measureLongestTextWidth(measuredText)
                when {
                    labelOptions.textMode == LabelOptions.ALIGN_LEFT -> viewPort.offsetRect.right += longestTextWidth
                    labelOptions.textMode == LabelOptions.ALIGN_CENTER -> {
                        val halfOfTextWidth = longestTextWidth / 2
                        viewPort.offsetRect.left += halfOfTextWidth
                        viewPort.offsetRect.right += halfOfTextWidth
                    }
                    labelOptions.textMode == LabelOptions.ALIGIN_RIGHT -> viewPort.offsetRect.left += longestTextWidth
                }
                longestTextWidth * xAxis.visibleRangeMinimum + viewPort.offsetRect.left + viewPort.offsetRect.right
            }
        }

        xAxis.labelOptions.size = xAxis.labelOptions.calcTextHeight(measuredText).toFloat()
        val height = xAxis.labelOptions.size
        return FSize.obtain(width, height)

    }


    override fun draw(canvas: Canvas, transformer: Transformer) {
        val xAxis = view.axis

        val startValue = view.getCurrentScaleValue()
        val endValue = min(startValue + view.getScaleValueRangePerScreen(), xAxis.maxValue)

        for (x in startValue..endValue) {
            val remainder = (x - xAxis.minValue).rem(xAxis.scaleLineStep)
            if (remainder == 0) {
                // 说明当前为起始刻度
                val point = FSize.obtain(x.toFloat(), 2F)
                transformer.pointValuesToPixel(point)

                val text = view.valueFormatter.formatValue(x.toFloat())
                val textOffset = when (xAxis.labelOptions.textMode) {
                    LabelOptions.ALIGN_LEFT -> {
                        0F
                    }
                    LabelOptions.ALIGIN_RIGHT -> {
                        -xAxis.labelOptions.measureLongestTextWidth(text)
                    }
                    else -> {
                        -xAxis.labelOptions.measureLongestTextWidth(text) / 2
                    }
                }


                canvas.drawText(
                    text,
                    point.x + textOffset,
                    point.y,
                    xAxis.labelOptions.paint
                )
                point.recycle()
            }
        }

    }
}
