package com.lixicode.ruler.renderer

import android.graphics.Canvas
import android.text.TextUtils
import android.view.View
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.data.*
import com.lixicode.ruler.formatter.ValueFormatter
import com.lixicode.ruler.utils.Transformer
import kotlin.math.min

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
class LabelRenderer(
    view: RulerView,
    var valueFormatter: ValueFormatter = object : ValueFormatter {}
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

        val measuredText = if (TextUtils.isEmpty(labelOptions.longestLabelText)) {
            var longestLength = 0
            var longestString = ""
            for (index in 0..xAxis.range) {
                val formatted = valueFormatter.formatValue((xAxis.minValue + (index * xAxis.scaleLineStep)).toFloat())
                if (formatted.length > longestLength) {
                    longestString = formatted
                    longestLength = formatted.length
                }
            }
            labelOptions.longestLabelText = longestString
            longestString
        } else {
            labelOptions.longestLabelText
        }

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
                    labelOptions.textMode == LabelOptions.ALIGN_LEFT -> viewPort.offsetRect.left += longestTextWidth
                    labelOptions.textMode == LabelOptions.ALIGN_CENTER -> viewPort.offsetRect.left += longestTextWidth / 2
                    labelOptions.textMode == LabelOptions.ALIGIN_RIGHT -> viewPort.offsetRect.right += longestTextWidth
                }
                longestTextWidth * xAxis.visibleRangeMinimum + viewPort.offsetRect.width()
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

        for (x in startValue until endValue) {
            val remainder = (x - xAxis.minValue).rem(xAxis.scaleLineStep)
            if (remainder == 0) {
                // 说明当前为起始刻度
                val point = FSize.obtain(x.toFloat(), 2F)
                transformer.pointValuesToPixel(point)
                canvas.drawText(
                    valueFormatter.formatValue(x.toFloat()),
                    point.x,
                    point.y,
                    xAxis.labelOptions.paint
                )
                point.recycle()
            }
        }

    }
}
