package com.lixicode.ruler.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.view.View
import com.lixicode.ruler.XAxis
import com.lixicode.ruler.utils.Utils
import com.lixicode.ruler.utils.ViewPortHandler

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
class LabelRenderer(
    val xAxis: XAxis,
    viewPort: ViewPortHandler,
    textColor: Int,
    textSize: Float,
    val textSizeFiducial: Float = Utils.spToPx(1)
) : Renderer(viewPort) {
    companion object {
        private const val DEMO_TEXT = "10"
    }


    private val textRect = Rect()
    private val paint: Paint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.textSize = textSize
        paint.color = textColor
    }

    fun setLabelTextSize(value: Float) {
        paint.textSize = value
    }

    fun setLabelTextColor(value: Int) {
        paint.color = value
    }


    override fun computeMinimumWidth(widthMeasureSpec: Int, minimunWidth: Float): Float {
        val mode = View.MeasureSpec.getMode(widthMeasureSpec)
        return if (mode == View.MeasureSpec.EXACTLY) {
            val viewWidth = View.MeasureSpec.getSize(widthMeasureSpec).toFloat()
            resizeTextSize(viewWidth / xAxis.visibleRangeMaximun, textSizeFiducial)
            viewWidth
        } else {
            paint.measureText(DEMO_TEXT) * xAxis.visibleRangeMaximun
        }
    }

    private fun resizeTextSize(specWidth: Float, textSizeFiducial: Float) {
        val specTextWidth = paint.measureText(DEMO_TEXT)
        if (specWidth < specTextWidth) {
            paint.textSize -= textSizeFiducial
            resizeTextSize(specWidth, textSizeFiducial)
        }
    }

    override fun computeMinimumHeight(heightMeasureSpec: Int, minimunHeight: Float): Float {
        return calcTextHeight(paint, DEMO_TEXT).toFloat()
    }


    fun calcTextHeight(paint: Paint, demoText: String): Int {
        val r = textRect
        r.set(0, 0, 0, 0)
        paint.getTextBounds(demoText, 0, demoText.length, r)
        return r.height()
    }


    override fun draw(canvas: Canvas) {

    }
}
