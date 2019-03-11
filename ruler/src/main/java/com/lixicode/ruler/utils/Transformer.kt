package com.lixicode.ruler.utils

import android.graphics.Matrix
import com.lixicode.ruler.data.PointF
import com.lixicode.ruler.internal.RulerViewHelper

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/5
 */
internal class Transformer(private val viewPort: ViewPortHandler) {


    private var mMatrixPxToValue = Matrix()
    private var mMatrixValueToPx = Matrix()

    fun prepareMatrixValuePx(helper: RulerViewHelper) {

        val scaleX: Float
        val scaleY: Float
        val xMinimum: Float
        val yMinimun: Float
        if (helper.isHorizontal) {

            val deltaX = helper.visibleCountOfTick * helper.stepOfTicks
            val deltaY = helper.deltaTickWeightOfView

            scaleX = (viewPort.contentWidth / deltaX).letFinite()
            scaleY = (viewPort.contentHeight * deltaY).letFinite()

            xMinimum = helper.minimumOfTicks.toFloat()
            yMinimun = 0F
        } else {
            val deltaX = helper.weightOfView
            val deltaY = helper.visibleCountOfTick * helper.stepOfTicks

            scaleX = (viewPort.contentWidth / deltaX).letFinite()
            scaleY = (viewPort.contentHeight / deltaY).letFinite()

            xMinimum = 0F
            yMinimun = helper.minimumOfTicks.toFloat()
        }


        val matrix = mMatrixValueToPx
        matrix.reset()
        matrix.postTranslate(-xMinimum, -yMinimun)
        matrix.postScale(scaleX, scaleY)

        if (helper.isHorizontal) {
            matrix.postTranslate(0F, viewPort.contentTop)
        } else {
            matrix.postTranslate(viewPort.contentLeft, 0F)
        }

        matrix.invert(mMatrixPxToValue)
    }

    fun generateValueToPixel(value: Int): PointF {
        val pts = PointF.obtain(value, value)
        mMatrixValueToPx.mapPoints(pts)
        return pts
    }

    fun pointValuesToPixel(pts: PointF) {
        mMatrixValueToPx.mapPoints(pts)
    }

    fun invertPixelToValue(pts: PointF) {
        mMatrixPxToValue.mapPoints(pts)
    }

    fun invertPixelToValue(x: Int, y: Int): PointF {
        val pts = PointF.obtain(x, y)
        invertPixelToValue(pts)
        return pts
    }


}


internal fun Matrix.mapPoints(pts: PointF) {
    mapPoints(pts.array)
}
