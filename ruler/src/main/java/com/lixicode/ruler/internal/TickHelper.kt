package com.lixicode.ruler.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.lixicode.ruler.R
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.data.PointF
import com.lixicode.ruler.data.Options
import com.lixicode.ruler.data.setBounds
import com.lixicode.ruler.utils.ViewPortHandler
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/7
 */
internal class TickHelper(val view: RulerView) {


    internal val tickOptions: Options<Drawable> = Options()

    internal val baseLineOptions: Options<Drawable> = Options()

    internal val dividerTickOptions: Options<Drawable> = Options()

    internal val cursorOptions: Options<Drawable> = Options()


    fun loadFromAttributes(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.RulerView,
            defStyleAttr, defStyleRes
        )
        OptionsHelper.applyAttributes(
            context,
            a.getResourceId(R.styleable.RulerView_ruler_tickOptions, -1),
            tickOptions
        )
        OptionsHelper.applyAttributes(
            context,
            a.getResourceId(R.styleable.RulerView_ruler_dividerTickOptions, -1),
            dividerTickOptions
        )
        OptionsHelper.applyAttributes(
            context,
            a.getResourceId(R.styleable.RulerView_ruler_cursorOptions, -1),
            cursorOptions
        )
        OptionsHelper.applyAttributes(
            context,
            a.getResourceId(R.styleable.RulerView_ruler_baseLineOptions, -1),
            baseLineOptions
        )

        a.recycle()


        // overrider dividerTick weight
        if (tickOptions.weight == dividerTickOptions.weight) {
            dividerTickOptions.weight = dividerTickOptions.weight.div(2)
        }

    }


    fun visibleWidthNeeded(visibleCountOfTick: Int, stepOfTicks: Int): Int {
        val spacingCount = stepOfTicks.plus(1)
        val visibleDividerSpacingNeeded = spacingCount.times(dividerTickOptions.spacing)
            .times(visibleCountOfTick).times(stepOfTicks)

        val visibleDividerWidthNeeded = stepOfTicks.times(dividerTickOptions.widthNeeded)

        val visibleTickWidthNeeded = tickOptions.widthNeeded.times(visibleCountOfTick)
        return visibleTickWidthNeeded.plus(visibleDividerSpacingNeeded).plus(visibleDividerWidthNeeded)
    }

    /**
     * ------
     *
     * ------
     *
     * 计算横向时, 所需要的偏移值
     */
    fun computeHorizontalOffset(viewPort: ViewPortHandler) {
        val size: Float = if (baseLineOptions.enable) {
            min(baseLineOptions.widthNeeded, baseLineOptions.heightNeeded).times(2).toFloat()
        } else {
            min(tickOptions.widthNeeded, tickOptions.heightNeeded).times(2).toFloat()
        }
        viewPort.offsetRect.set(size, size, size, size)
    }

    /**
     *
     * | |
     * | |
     * | |
     *
     * 计算横向时, 所需要的偏移值
     */
    fun computeVerticalOffset(viewPort: ViewPortHandler) {
        val size: Float = if (baseLineOptions.enable) {
            min(baseLineOptions.widthNeeded, baseLineOptions.heightNeeded).times(2).toFloat()
        } else {
            min(tickOptions.widthNeeded, tickOptions.heightNeeded).times(2).toFloat()
        }
        viewPort.offsetRect.set(size, size, size, size)
    }

    fun onDraw(canvas: Canvas) {
        val helper = view.helper
        if (helper.isHorizontal) {
            drawHorizontalTick(helper, canvas)
            drawHorizontalCursor(helper, canvas)
            drawHorizontalBaseLine(helper, canvas)
        } else {
            drawVerticalTick(helper, canvas)
            drawVerticalCursor(helper, canvas)
            drawVerticalBaseLine(helper, canvas)
        }
    }


    private fun drawVerticalBaseLine(helper: RulerViewHelper, canvas: Canvas) {
        if (!baseLineOptions.visible) {
            return
        }
        // 绘制基准线
        val yPx = view.scrollY + view.paddingTop
        val xPx = helper.viewPort.contentRect.left.roundToInt()

        baseLineOptions.setBounds(
            xPx,
            yPx,
            xPx + baseLineOptions.widthNeeded,
            yPx + view.height - view.paddingBottom
        )

        // draw
        baseLineOptions.getDrawable()?.draw(canvas)

    }

    private fun drawVerticalTick(helper: RulerViewHelper, canvas: Canvas) {
        if (!tickOptions.visible) {
            return
        }

        val enableSignificantTick = helper.stepOfTicks.rem(2) == 0
        val halfOfStep = helper.stepOfTicks.div(2)

        for (y in helper.rangeOfTickWithScrollOffset()) {
            val remainderOfTick = helper.remOfTick(y)

            val significantBetweenTick = enableSignificantTick && remainderOfTick == halfOfStep
            val x = when {
                remainderOfTick == 0 -> tickOptions.weight
                significantBetweenTick -> helper.significantTickWeight
                else -> dividerTickOptions.weight
            }

            PointF.obtain(x, y.toFloat())
                .also {
                    helper.transformer.pointValuesToPixel(it)
                }
                .also {
                    val isDividerLine = x != tickOptions.weight
                    if (isDividerLine) {
                        dividerTickOptions
                    } else {
                        tickOptions
                    }.run {

                        // bounds
                        setBounds(
                            helper.viewPort.contentLeft.roundToInt(),
                            it.y.roundToInt(),
                            it.x.minus(helper.viewPort.contentLeft).roundToInt(),
                            it.y.roundToInt()
                        )

                        // draw
                        getDrawable()?.draw(canvas)
                    }
                }.also {
                    it.recycle()
                }
        }

    }

    private fun drawVerticalCursor(helper: RulerViewHelper, canvas: Canvas) {
        if (!cursorOptions.visible) {
            return
        }
        PointF.obtain(tickOptions.weight, view.tick.toFloat())
            .also {
                helper.transformer.pointValuesToPixel(it)
            }.also {
                it.y = (view.scrollY + view.height.div(2)).toFloat()
            }.also {
                // bounds
                cursorOptions.setBounds(
                    helper.viewPort.contentLeft.roundToInt(),
                    it.y.roundToInt(),
                    it.x.minus(helper.viewPort.contentLeft).roundToInt(),
                    it.y.roundToInt()
                )

                // draw
                cursorOptions.getDrawable()?.draw(canvas)
            }.run {
                recycle()
            }
    }


    private fun drawHorizontalBaseLine(helper: RulerViewHelper, canvas: Canvas) {
        if (!baseLineOptions.visible) {
            return
        }
        // 绘制基准线
        val x = view.scrollX
        val yPx = helper.viewPort.contentRect.top.roundToInt() + baseLineOptions.heightNeeded

        baseLineOptions.setBounds(x + view.paddingLeft, yPx, x + view.width - view.paddingRight, yPx)

        // draw
        baseLineOptions.getDrawable()?.draw(canvas)
    }

    private fun drawHorizontalTick(helper: RulerViewHelper, canvas: Canvas) {
        if (!tickOptions.visible) {
            return
        }

        val enableSignificantTick = helper.stepOfTicks.rem(2) == 0
        val halfOfStep = helper.stepOfTicks.div(2)

        for (x in helper.rangeOfTickWithScrollOffset()) {
            val remainderOfTick = helper.remOfTick(x)

            val significantBetweenTick = enableSignificantTick && remainderOfTick == halfOfStep
            val y = when {
                remainderOfTick == 0 -> tickOptions.weight
                significantBetweenTick -> helper.significantTickWeight
                else -> dividerTickOptions.weight
            }

            PointF.obtain(x.toFloat(), y)
                .also {
                    helper.transformer.pointValuesToPixel(it)
                }
                .also {
                    val isDividerLine = y != tickOptions.weight
                    if (isDividerLine) {
                        dividerTickOptions
                    } else {
                        tickOptions
                    }.run {

                        // bounds
                        setBounds(
                            it.x.roundToInt(),
                            helper.viewPort.contentTop.roundToInt(),
                            it.x.roundToInt(),
                            it.y.minus(helper.viewPort.contentTop).roundToInt()
                        )

                        // draw
                        getDrawable()?.draw(canvas)
                    }
                }.also {
                    it.recycle()
                }
        }
    }

    private fun drawHorizontalCursor(helper: RulerViewHelper, canvas: Canvas) {
        if (!cursorOptions.visible) {
            return
        }
        PointF.obtain(view.tick.toFloat(), tickOptions.weight)
            .also {
                helper.transformer.pointValuesToPixel(it)
            }.also {
                it.x = (view.scrollX + view.width.div(2)).toFloat()
            }.also {
                // bounds
                cursorOptions.setBounds(
                    it.x.roundToInt(),
                    helper.viewPort.contentTop.roundToInt(),
                    it.x.roundToInt(),
                    it.y.minus(helper.viewPort.contentTop).roundToInt()
                )

                // draw
                cursorOptions.getDrawable()?.draw(canvas)
            }.run {
                recycle()
            }
    }


}
