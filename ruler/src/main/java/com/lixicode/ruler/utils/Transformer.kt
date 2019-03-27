package com.lixicode.ruler.utils

import android.graphics.Matrix
import androidx.core.util.Pools
import com.lixicode.ruler.internal.RulerViewHelper

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/5
 */
internal class Transformer(private val viewPort: ViewPortHandler) {


    internal var mMatrixScrollOffset = Matrix()
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

        val scaleX = minimumWidth / deltaX
        val scaleY = minimumHeight / deltaY

        applyToMatrix(mMatrixValueToPx, scaleX, scaleY, 0F, viewPort.contentTop)

        mMatrixValueToPx.invert(mMatrixPxToValue)

        labelMatrix.set(mMatrixValueToPx)

    }


    private fun prepareVerticalMatrix(helper: RulerViewHelper) {
        val minimumWidth = viewPort.contentWidth
        val minimumHeight = viewPort.contentHeight.coerceAtLeast(helper.minimumMeasureHeight.toFloat())

        val deltaX = helper.weightOfView
        val deltaY = helper.visibleCountOfTick * helper.stepOfTicks

        val scaleX = minimumWidth / deltaX
        val scaleY = minimumHeight / deltaY

        applyToMatrix(mMatrixValueToPx, scaleX, scaleY, viewPort.contentLeft, 0F)

        mMatrixValueToPx.invert(mMatrixPxToValue)

        labelMatrix.set(mMatrixValueToPx)
    }


    internal fun prepareScrollOffset(dx: Float, dy: Float) {
        mMatrixScrollOffset.reset()
        mMatrixScrollOffset.postTranslate(dx, dy)
    }

    private fun applyToMatrix(
        matrix: Matrix,
        scaleX: Float,
        scaleY: Float,
        postTranslateX: Float,
        postTranslateY: Float
    ) {
        matrix.reset()
        if (scaleX.isFinite()) {
            matrix.postScale(scaleX, 1F)
        }
        if (scaleY.isFinite()) {
            matrix.postScale(1F, scaleY)
        }
        matrix.postTranslate(postTranslateX, postTranslateY)
    }


}
