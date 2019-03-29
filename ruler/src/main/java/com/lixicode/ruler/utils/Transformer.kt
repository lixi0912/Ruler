/**
 * MIT License
 *
 * Copyright (c) 2019 lixi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.lixicode.ruler.utils

import android.graphics.Matrix
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.data.clone
import com.lixicode.ruler.data.invert
import com.lixicode.ruler.internal.RulerViewHelper

/**
 * <>
 * @author lixi
 * @date 2019/3/5
 */
internal class Transformer(private val viewPort: ViewPortHandler) {


    private var scrollOffsetMatrix = Matrix()
    private var valueToPxMatrix = Matrix()

    internal fun prepareScrollOffset(dx: Float, dy: Float) {
        scrollOffsetMatrix.reset()
        scrollOffsetMatrix.postTranslate(dx, dy)
    }

    fun prepareMatrixValuePx(helper: RulerViewHelper) {
        if (helper.isHorizontal) {
            prepareHorizontalMatrix(helper)
        } else {
            prepareVerticalMatrix(helper)
        }
    }


    private fun prepareHorizontalMatrix(helper: RulerViewHelper) {
        val deltaX = helper.visibleCountOfTick * helper.stepOfTicks
        val minimumWidth = if (helper.autoSpacingMode == RulerView.EXPAND_SPACING_ALWAYS) {
            viewPort.contentWidth.coerceAtLeast(helper.minimunMeasureWidth.toFloat())
        } else {
            helper.minimunMeasureWidth.toFloat()
        }

        val scaleX = minimumWidth / deltaX
        val scaleY = viewPort.contentHeight / helper.weightOfView

        applyToMatrix(valueToPxMatrix, scaleX, scaleY, 0F, viewPort.contentTop)


    }


    private fun prepareVerticalMatrix(helper: RulerViewHelper) {
        val scaleX = viewPort.contentWidth / helper.weightOfView

        val minimumHeight = if (helper.autoSpacingMode == RulerView.EXPAND_SPACING_ALWAYS) {
            viewPort.contentHeight.coerceAtLeast(helper.minimumMeasureHeight.toFloat())
        } else {
            helper.minimumMeasureHeight.toFloat()
        }

        val deltaY = helper.visibleCountOfTick * helper.stepOfTicks
        val scaleY = minimumHeight / deltaY

        applyToMatrix(valueToPxMatrix, scaleX, scaleY, viewPort.contentLeft, 0F)
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

    fun obtainScrollOffsetMatrix(): Matrix {
        return scrollOffsetMatrix.clone()
    }

    fun obtainValueToPxMatrix(): Matrix {
        return valueToPxMatrix.clone()
    }

    fun obtainPxToValueMatrix(): Matrix {
        return valueToPxMatrix.invert()
    }

}
