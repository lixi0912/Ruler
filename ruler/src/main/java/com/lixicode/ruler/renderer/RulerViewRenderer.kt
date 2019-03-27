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
package com.lixicode.ruler.renderer

import android.graphics.Canvas
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.internal.RulerViewHelper

/**
 * @author lixi
 * @description <>
 * @date 2019/3/20
 */

internal class RulerViewRenderer(helper: RulerViewHelper) {

    private val tickRenderer = TickRenderer(helper.tickHelper)
    private val labelRenderer = LabelRenderer(helper.labelHelper)


    fun onDraw(view: RulerView, canvas: Canvas) {
        if (view.isHorizontal) {
            drawHorizontal(view, canvas)
        } else {
            drawVertical(view, canvas)
        }
    }

    private fun drawHorizontal(view: RulerView, canvas: Canvas) {
        val helper = view.helper

        labelRenderer.onPreDraw(view)

        val significantPosition = helper.stepOfTicks.rem(2) == 0
        val halfOfStep = helper.stepOfTicks.div(2)
        for (position in view.positionRangeWithOffset()) {
            val remainderValue = helper.remOfTick(position)
            val significantBetweenTick = significantPosition && remainderValue == halfOfStep

            // draw tick
            if (helper.gravityOfTick == RulerView.GRAVITY_START || helper.enableMirrorTick) {
                tickRenderer.onDrawHorizontal(view, canvas, position, remainderValue, significantBetweenTick)
            }

            drawOnMirrorTick(view, canvas) { _, _ ->
                tickRenderer.onDrawHorizontal(view, canvas, position, remainderValue, significantBetweenTick)
            }

            labelRenderer.onDrawHorizontal(view, canvas, position, remainderValue)
        }

        if (helper.gravityOfTick == RulerView.GRAVITY_START || helper.enableMirrorTick) {
            tickRenderer.onDrawHorizontalOver(view, canvas)
        }

        drawOnMirrorTick(view, canvas) { _, _ ->
            tickRenderer.onDrawHorizontalOver(view, canvas)
        }

        labelRenderer.finishDraw(view)


    }


    private fun drawVertical(view: RulerView, canvas: Canvas) {
        val helper = view.helper

        labelRenderer.onPreDraw(view)


        val enableSignificantTick = helper.stepOfTicks.rem(2) == 0
        val halfOfStep = helper.stepOfTicks.div(2)
        for (position in view.positionRangeWithOffset()) {
            val remainderOfTick = helper.remOfTick(position)
            val significantBetweenTick = enableSignificantTick && remainderOfTick == halfOfStep

            // draw tick
            if (helper.gravityOfTick == RulerView.GRAVITY_START || helper.enableMirrorTick) {
                tickRenderer.onDrawVertical(view, canvas, position, remainderOfTick, significantBetweenTick)
            }

            drawOnMirrorTick(view, canvas) { _, _ ->
                tickRenderer.onDrawVertical(view, canvas, position, remainderOfTick, significantBetweenTick)
            }
            labelRenderer.onDrawVertical(view, canvas, position, remainderOfTick)
        }
        if (helper.gravityOfTick == RulerView.GRAVITY_START || helper.enableMirrorTick) {
            tickRenderer.onDrawVerticalOver(view, canvas)
        }

        drawOnMirrorTick(view, canvas) { _, _ ->
            tickRenderer.onDrawVerticalOver(view, canvas)
        }

        labelRenderer.finishDraw(view)

    }


    private fun drawOnMirrorTick(view: RulerView, canvas: Canvas, drawMirror: (RulerView, Canvas) -> Unit) {
        val helper = view.helper
        if (helper.enableMirrorTick || helper.gravityOfTick == RulerView.GRAVITY_END) {
            val saveId = canvas.save()
            if (helper.isHorizontal) {
                canvas.scale(1F, -1F)

                val deltaY = (-view.height).plus(view.computeCanvasPaddingByHorizontal())
                    .toFloat()

                canvas.translate(0F, deltaY)
            } else {
                canvas.scale(-1F, 1F)

                val deltaX = (-view.width).plus(view.computeCanvasPaddingByVertical())
                    .toFloat()

                canvas.translate(deltaX, 0F)
            }

            drawMirror(view, canvas)
            canvas.restoreToCount(saveId)
        }
    }


}
