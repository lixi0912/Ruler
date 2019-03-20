package com.lixicode.ruler.renderer

import android.graphics.Canvas
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.internal.RulerViewHelper

/**
 * @author 陈晓辉
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

        val enableSignificantTick = helper.stepOfTicks.rem(2) == 0
        val halfOfStep = helper.stepOfTicks.div(2)
        for (tick in helper.rangeOfTickWithScrollOffset()) {
            val remainderOfTick = helper.remOfTick(tick)
            val significantBetweenTick = enableSignificantTick && remainderOfTick == halfOfStep

            // draw tick
            if (helper.gravityOfTick == RulerView.GRAVITY_START || helper.enableMirrorTick) {
                tickRenderer.onDrawHorizontal(view, canvas, tick, remainderOfTick, significantBetweenTick)
            }

            drawOnMirrorTick(view, canvas) { _, _ ->
                tickRenderer.onDrawHorizontal(view, canvas, tick, remainderOfTick, significantBetweenTick)
            }

            labelRenderer.onDrawHorizontal(view, canvas, tick, remainderOfTick)
        }

        tickRenderer.onDrawHorizontalOver(view, canvas)

        drawOnMirrorTick(view, canvas) { _, _ ->
            tickRenderer.onDrawHorizontalOver(view, canvas)
        }

        labelRenderer.finishDraw()


    }


    private fun drawVertical(view: RulerView, canvas: Canvas) {
        val helper = view.helper

        labelRenderer.onPreDraw(view)


        val enableSignificantTick = helper.stepOfTicks.rem(2) == 0
        val halfOfStep = helper.stepOfTicks.div(2)
        for (tick in helper.rangeOfTickWithScrollOffset()) {
            val remainderOfTick = helper.remOfTick(tick)
            val significantBetweenTick = enableSignificantTick && remainderOfTick == halfOfStep

            // draw tick
            if (helper.gravityOfTick == RulerView.GRAVITY_START || helper.enableMirrorTick) {
                tickRenderer.onDrawVertical(view, canvas, tick, remainderOfTick, significantBetweenTick)
            }

            drawOnMirrorTick(view, canvas) { _, _ ->
                tickRenderer.onDrawVertical(view, canvas, tick, remainderOfTick, significantBetweenTick)
            }
            labelRenderer.onDrawVertical(view, canvas, tick, remainderOfTick)
        }

        tickRenderer.onDrawVerticalOver(view, canvas)

        drawOnMirrorTick(view, canvas) { _, _ ->
            tickRenderer.onDrawVerticalOver(view, canvas)
        }

        labelRenderer.finishDraw()

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
