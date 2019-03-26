package com.lixicode.ruler.renderer

import android.graphics.Canvas
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.data.PointF
import com.lixicode.ruler.data.setBounds
import com.lixicode.ruler.internal.TickHelper
import kotlin.math.roundToInt

/**
 * @author 陈晓辉
 * @description <>
 * @date 2019/3/20
 */

internal class TickRenderer(private val helper: TickHelper) {

    fun onDrawHorizontal(
        view: RulerView,
        canvas: Canvas,
        tick: Int,
        remainderValue: Int,
        significantBetweenTick: Boolean
    ) {
        drawHorizontalTick(view, canvas, tick, remainderValue, significantBetweenTick)
    }


    fun onDrawVertical(
        view: RulerView, canvas: Canvas, tick: Int, remainderOfTick: Int, significantBetweenTick: Boolean
    ) {

        drawVerticalTick(view, canvas, tick, remainderOfTick, significantBetweenTick)
    }


    fun onDrawHorizontalOver(view: RulerView, canvas: Canvas) {
        drawHorizontalCursor(view, canvas)
        drawHorizontalBaseLine(view, canvas)
    }


    fun onDrawVerticalOver(view: RulerView, canvas: Canvas) {
        drawVerticalCursor(view, canvas)
        drawVerticalBaseLine(view, canvas)
    }

    private fun drawHorizontalTick(
        view: RulerView, canvas: Canvas, tick: Int, remainderValue: Int, significantBetweenTick: Boolean
    ) {
        val y = when {
            remainderValue == 0 -> helper.tickOptions.weight
            significantBetweenTick -> helper.significantTickWeight
            else -> helper.dividerTickOptions.weight
        }

        PointF.obtain(tick.toFloat(), y)
            .also {
                view.transformer.pointValuesToPixel(it)
            }
            .also {
                val isDividerLine = y != helper.tickOptions.weight
                if (isDividerLine) {
                    helper.dividerTickOptions
                } else {
                    helper.tickOptions
                }.run {

                    // bounds
                    setBounds(
                        it.x.roundToInt(),
                        view.viewPort.contentTop.roundToInt(),
                        it.x.roundToInt(),
                        it.y.minus(view.viewPort.contentTop).roundToInt()
                    )

                    // draw
                    getDrawable()?.draw(canvas)
                }
            }.also {
                it.recycle()
            }
    }


    private fun drawHorizontalCursor(view: RulerView, canvas: Canvas) {
        if (!helper.cursorOptions.visible) {
            return
        }
        PointF.obtain(view.tick.toFloat(), helper.tickOptions.weight)
            .also {
                view.transformer.pointValuesToPixel(it)
            }.also {
                it.x = (view.scrollX + view.width.div(2)).toFloat()
            }.also {
                // bounds
                helper.cursorOptions.setBounds(
                    it.x.roundToInt(),
                    view.viewPort.contentTop.roundToInt(),
                    it.x.roundToInt(),
                    it.y.minus(view.viewPort.contentTop).roundToInt()
                )

                // draw
                helper.cursorOptions.getDrawable()?.draw(canvas)
            }.also {
                it.recycle()
            }
    }

    private fun drawHorizontalBaseLine(view: RulerView, canvas: Canvas) {
        if (!helper.baseLineOptions.visible) {
            return
        }

        // 绘制基准线
        val x = view.scrollX
        val yPx = view.viewPort.contentRect.top.roundToInt() + helper.baseLineOptions.heightNeeded

        helper.baseLineOptions.setBounds(x + view.paddingLeft, yPx, x + view.width - view.paddingRight, yPx)

        // draw
        helper.baseLineOptions.getDrawable()?.draw(canvas)
    }


    private fun drawVerticalTick(
        view: RulerView,
        canvas: Canvas,
        tick: Int,
        remainderOfTick: Int,
        significantBetweenTick: Boolean
    ) {
        val x = when {
            remainderOfTick == 0 -> helper.tickOptions.weight
            significantBetweenTick -> helper.significantTickWeight
            else -> helper.dividerTickOptions.weight
        }

        PointF.obtain(x, tick.toFloat())
            .also {
                view.transformer.pointValuesToPixel(it)
            }
            .also {
                val isDividerLine = x != helper.tickOptions.weight
                if (isDividerLine) {
                    helper.dividerTickOptions
                } else {
                    helper.tickOptions
                }.run {

                    // bounds
                    setBounds(
                        view.viewPort.contentLeft.roundToInt(),
                        it.y.roundToInt(),
                        it.x.minus(view.viewPort.contentLeft).roundToInt(),
                        it.y.roundToInt()
                    )

                    // draw
                    getDrawable()?.draw(canvas)
                }
            }.also {
                it.recycle()
            }
    }

    private fun drawVerticalCursor(view: RulerView, canvas: Canvas) {
        if (!helper.cursorOptions.visible) {
            return
        }
        PointF.obtain(helper.tickOptions.weight, view.tick.toFloat())
            .also {
                view.transformer.pointValuesToPixel(it)
            }.also {
                it.y = (view.scrollY + view.height.div(2)).toFloat()
            }.also {
                // bounds
                helper.cursorOptions.setBounds(
                    view.viewPort.contentLeft.roundToInt(),
                    it.y.roundToInt(),
                    it.x.minus(view.viewPort.contentLeft).roundToInt(),
                    it.y.roundToInt()
                )

                // draw
                helper.cursorOptions.getDrawable()?.draw(canvas)
            }.also {
                it.recycle()
            }
    }


    private fun drawVerticalBaseLine(view: RulerView, canvas: Canvas) {
        if (!helper.baseLineOptions.visible) {
            return
        }
        // 绘制基准线
        val yPx = view.scrollY + view.paddingTop
        val xPx = view.viewPort.contentRect.left.roundToInt()
        helper.baseLineOptions.setBounds(
            xPx,
            yPx,
            xPx + helper.baseLineOptions.widthNeeded,
            yPx + view.height - view.paddingBottom
        )

        // draw
        helper.baseLineOptions.getDrawable()?.draw(canvas)
    }
}
