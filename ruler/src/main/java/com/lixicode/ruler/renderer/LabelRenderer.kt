package com.lixicode.ruler.renderer

import android.graphics.Canvas
import android.view.View
import com.lixicode.ruler.XAxis
import com.lixicode.ruler.data.*
import com.lixicode.ruler.utils.ViewPortHandler

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
class LabelRenderer(
    viewPort: ViewPortHandler,
    private val xAxis: XAxis
) : Renderer(viewPort) {


    override fun computeMinimumWidth(widthMeasureSpec: Int, minimunWidth: Float): Float {
        val labelOptions = xAxis.labelOptions
        val mode = View.MeasureSpec.getMode(widthMeasureSpec)
        return if (mode == View.MeasureSpec.EXACTLY) {
            val viewWidth = View.MeasureSpec.getSize(widthMeasureSpec).toFloat()
            labelOptions.resizeTextSize(viewWidth / xAxis.visibleRangeMaximun)
            viewWidth
        } else {
            val textMearsuredWidth = labelOptions.paint.measureText(labelOptions.measuredText)
            when (labelOptions.textMode) {
                LabelOptions.LEFT -> {
                    viewPort.offsetRect.left = textMearsuredWidth
                    textMearsuredWidth * xAxis.visibleRangeMaximun + viewPort.offsetLeft
                }
                LabelOptions.CENTER -> {
                    viewPort.offsetRect.left = textMearsuredWidth / 2
                    textMearsuredWidth * xAxis.visibleRangeMaximun + viewPort.offsetRect.left
                }
                else -> {
                    viewPort.offsetRect.right = textMearsuredWidth
                    textMearsuredWidth * xAxis.visibleRangeMaximun + viewPort.offsetRight
                }
            }
        }
    }


    override fun computeMinimumHeight(heightMeasureSpec: Int, minimunHeight: Float): Float {
        return xAxis.labelOptions.calcTextHeight().toFloat()
    }


    override fun draw(canvas: Canvas, buffer: RulerBuffer) {


    }
}
