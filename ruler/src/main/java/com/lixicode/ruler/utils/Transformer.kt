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


    internal var labelMatrix = Matrix()
    internal var mMatrixPxToValue = Matrix()
    internal var mMatrixValueToPx = Matrix()

    fun prepareMatrixValuePx(helper: RulerViewHelper) {
        if (helper.isHorizontal) {
            prepareHorizontalMatrix(helper)
        } else {
            prepareVerticalMatrix(helper)
        }
    }


    private fun prepareHorizontalMatrix(helper: RulerViewHelper) {
        val deltaX = helper.visibleCountOfTick * helper.stepOfTicks
        val deltaY = helper.weightOfView

        val minimumWidth = viewPort.contentWidth.coerceAtLeast(helper.minimunMeasureWidth.toFloat())
        val minimumHeight = viewPort.contentHeight

        val scaleX = (minimumWidth / deltaX).letFinite()
        val scaleY = (minimumHeight / deltaY).letFinite()

        applyToMatrix(mMatrixValueToPx, scaleX, scaleY, 0F, viewPort.contentTop)
        mMatrixValueToPx.invert(mMatrixPxToValue)



        labelMatrix.set(mMatrixValueToPx)
//        labelMatrix.postTranslate(-helper.labelHelper.labelOptions.widthNeeded.div(2F), 0F)

    }


    private fun prepareVerticalMatrix(helper: RulerViewHelper) {
        val minimumWidth = viewPort.contentWidth
        val minimumHeight = viewPort.contentHeight.coerceAtLeast(helper.minimumMeasureHeight.toFloat())

        val deltaX = helper.weightOfView
        val deltaY = helper.visibleCountOfTick * helper.stepOfTicks

        val scaleX = (minimumWidth / deltaX).letFinite()
        val scaleY = (minimumHeight / deltaY).letFinite()

        applyToMatrix(mMatrixValueToPx, scaleX, scaleY, viewPort.contentLeft, 0F)
        mMatrixValueToPx.invert(mMatrixPxToValue)



        labelMatrix.set(mMatrixValueToPx)
//        labelMatrix.postTranslate(0F, -helper.labelHelper.labelOptions.heightNeeded.div(2F))

    }

    private fun applyToMatrix(
        matrix: Matrix,
        scaleX: Float,
        scaleY: Float,
        postTranslateX: Float,
        postTranslateY: Float
    ) {
        matrix.reset()
        matrix.postScale(scaleX, scaleY)
        matrix.postTranslate(postTranslateX, postTranslateY)


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
