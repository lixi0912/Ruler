package com.lixicode.ruler.renderer

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.data.*
import com.lixicode.ruler.internal.TickHelper
import com.lixicode.ruler.utils.RectFPool
import com.lixicode.ruler.utils.RectPool

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

        val options: Options<Drawable>
        val weight: Float
        when {
            remainderValue == 0 -> {
                options = helper.tickOptions
                weight = helper.tickOptions.weight
            }
            significantBetweenTick -> {
                options = helper.dividerTickOptions
                weight = helper.significantTickWeight
            }
            else -> {
                options = helper.dividerTickOptions
                weight = helper.dividerTickOptions.weight
            }
        }

        val tickValue = tick.toFloat()
        val rect = RectFPool.obtain()
            .also {
                it.set(tickValue, 0F, tickValue, weight)
            }
            .concat(view.transformer.mMatrixValueToPx)
            .mapToRect()


        drawWithBounds(
            view, canvas, rect, options
        )
    }

    private fun drawVerticalTick(
        view: RulerView,
        canvas: Canvas,
        tick: Int,
        remainderValue: Int,
        significantBetweenTick: Boolean
    ) {
        val options: Options<Drawable>
        val weight: Float
        when {
            remainderValue == 0 -> {
                options = helper.tickOptions
                weight = helper.tickOptions.weight
            }
            significantBetweenTick -> {
                options = helper.dividerTickOptions
                weight = helper.significantTickWeight
            }
            else -> {
                options = helper.dividerTickOptions
                weight = helper.dividerTickOptions.weight
            }
        }

        val tickValue = tick.toFloat()
        val rect = RectFPool.obtain()
            .also {
                it.set(0F, tickValue, weight, tickValue)
            }
            .concat(view.transformer.mMatrixValueToPx)
            .mapToRect()


        drawWithBounds(
            view, canvas, rect, options
        )

    }


    private fun drawHorizontalCursor(view: RulerView, canvas: Canvas) {
        if (!helper.cursorOptions.visible) {
            return
        }
        val rect = RectFPool.obtain()
            .also {
                it.set(0F, 0F, 0F, helper.tickOptions.weight)
            }
            .concat(view.transformer.mMatrixValueToPx)
            .mapToRect()
            .also {
                it.left = view.scrollX + view.width.div(2)
                it.right = it.left
            }
        drawWithBounds(
            view, canvas, rect, helper.cursorOptions
        )
    }

    private fun drawVerticalCursor(view: RulerView, canvas: Canvas) {
        if (!helper.cursorOptions.visible) {
            return
        }
        val rect = RectFPool.obtain()
            .also {
                it.set(0F, view.tick.toFloat(), helper.tickOptions.weight, view.tick.toFloat())
            }
            .concat(view.transformer.mMatrixValueToPx)
            .mapToRect()
            .also {
                it.bottom = view.scrollY + view.height.div(2)
                it.top = it.bottom
            }

        drawWithBounds(
            view, canvas, rect, helper.cursorOptions
        )
    }


    private fun drawHorizontalBaseLine(view: RulerView, canvas: Canvas) {
        if (!helper.baseLineOptions.visible) {
            return
        }

        // 绘制基准线
        RectPool.obtain()
            .also {
                it.left = view.scrollX + view.paddingLeft
                it.right = view.scrollX + view.width - view.paddingRight
            }
            .mapToRectF()
            .also {
                it.offset(0F, view.viewPort.contentTop)
            }
            .mapToRect()
            .let {
                helper.baseLineOptions.setBounds(it)
            }.getDrawable()?.draw(canvas)
    }

    private fun drawVerticalBaseLine(view: RulerView, canvas: Canvas) {
        if (!helper.baseLineOptions.visible) {
            return
        }

        // 绘制基准线
        RectPool.obtain()
            .also {
                it.top = view.scrollY + view.paddingTop
                it.bottom = view.scrollY + view.height - view.paddingBottom
            }
            .mapToRectF()
            .also {
                it.offset(view.viewPort.contentLeft, 0F)
            }
            .mapToRect()
            .let {
                helper.baseLineOptions.setBounds(it)
            }.getDrawable()?.draw(canvas)
    }


    private fun <T : Drawable> drawWithBounds(
        view: RulerView,
        canvas: Canvas,
        rect: Rect,
        options: Options<T>
    ) {
        options.setBounds(rect, onExpanded = {
            it.mapToRectF()
                .apply {
                    // force start on content top
                    if (view.isHorizontal) {
                        offsetTo(left, view.viewPort.contentTop)
                    } else {
                        offsetTo(view.viewPort.contentLeft, top)
                    }
                }
                .mapToRect()
        }).getDrawable()?.draw(canvas)
    }

}
