package com.lixicode.ruler.renderer

import android.graphics.Canvas
import android.view.View
import com.lixicode.ruler.XAxis
import com.lixicode.ruler.data.*
import com.lixicode.ruler.utils.Transformer
import com.lixicode.run.ui.view.RulerView

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
        val width = when {
            View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.EXACTLY -> {
                View.MeasureSpec.getSize(
                    widthMeasureSpec
                ).toFloat().apply {
                    labelOptions.autoTextSize(this / xAxis.visibleRangeMinimum)
                }
            }
            else -> {
                val longestTextWidth = labelOptions.measureLongestTextWidth()
                when {
                    labelOptions.textMode == LabelOptions.ALIGN_LEFT -> viewPort.offsetRect.left += longestTextWidth
                    labelOptions.textMode == LabelOptions.ALIGN_CENTER -> viewPort.offsetRect.left += longestTextWidth / 2
                    labelOptions.textMode == LabelOptions.ALIGIN_RIGHT -> viewPort.offsetRect.right += longestTextWidth
                }
                longestTextWidth * xAxis.visibleRangeMinimum + viewPort.offsetRect.width()
            }
        }
        val height = xAxis.labelOptions.calcTextHeight().toFloat()
        return FSize.obtain(width, height)

    }


    override fun draw(canvas: Canvas, transformer: Transformer) {


    }
}
