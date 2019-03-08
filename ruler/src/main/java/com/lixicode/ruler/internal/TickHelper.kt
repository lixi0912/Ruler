package com.lixicode.ruler.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import com.lixicode.ruler.R
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.data.FSize
import com.lixicode.ruler.data.Options
import com.lixicode.ruler.data.setBounds
import com.lixicode.ruler.utils.ViewPortHandler
import kotlin.math.roundToInt

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/7
 */
internal class TickHelper(val view: RulerView) {


    val tickOptions: Options<Drawable> = Options()

    private val baseLineOptions: Options<Drawable> = Options()

    private val dividerTickOptions: Options<Drawable> = Options()

    private val cursorOptions: Options<Drawable> = Options()


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
            a.getResourceId(R.styleable.RulerView_tickOptions, -1),
            tickOptions
        )
        OptionsHelper.applyAttributes(
            context,
            a.getResourceId(R.styleable.RulerView_dividerTickOptions, -1),
            dividerTickOptions
        )
        OptionsHelper.applyAttributes(
            context,
            a.getResourceId(R.styleable.RulerView_cursorOptions, -1),
            cursorOptions
        )
        OptionsHelper.applyAttributes(
            context,
            a.getResourceId(R.styleable.RulerView_baseLineOptions, -1),
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
        val width = baseLineOptions.widthNeeded.toFloat()
        val height = baseLineOptions.heightNeeded.toFloat()

        viewPort.offsetRect.set(width, height, width, height)
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
        val width = baseLineOptions.widthNeeded.toFloat()
        viewPort.offsetRect.set(width, width, width, width)
    }

    fun onDraw(canvas: Canvas) {
        val helper = view.helper
        if (helper.isHorizontal) {
            drawHorizontalBaseLine(helper, canvas)
            drawHorizontalTick(helper, canvas)
            drawHorizontalCursor(helper, canvas)
        } else {
            drawVerticalBaseLine(helper, canvas)
            drawVerticalTick(helper, canvas)
            drawVerticalCursor(helper, canvas)
        }
    }


    private fun drawVerticalBaseLine(helper: RulerViewHelper, canvas: Canvas) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun drawVerticalTick(helper: RulerViewHelper, canvas: Canvas) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun drawVerticalCursor(helper: RulerViewHelper, canvas: Canvas) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private fun drawHorizontalBaseLine(helper: RulerViewHelper, canvas: Canvas) {
        if (!baseLineOptions.enable) {
            return
        }
        // 绘制基准线
        val x = view.scrollX
        val yPx = helper.viewPort.contentTop.roundToInt()

        baseLineOptions.setBounds(x + view.paddingLeft, yPx, x + view.width - view.paddingRight, yPx)

        // draw
        baseLineOptions.getDrawable()?.draw(canvas)
    }

    private fun drawHorizontalTick(helper: RulerViewHelper, canvas: Canvas) {
        if (!tickOptions.enable) {
            return
        }
        // TODO need to fix significantBetweenScaleLine
        val significantBetweenScaleLine: Boolean = helper.stepOfTicks.rem(2) == 1

        for (x in helper.rangeOfTickWithScrollOffset()) {
            val remainderOfTick = helper.remOfTick(x)

            val y = when {
                remainderOfTick -> tickOptions.weight
                significantBetweenScaleLine -> dividerTickOptions.weight + 0.1F
                else -> dividerTickOptions.weight
            }
            FSize.obtain(x.toFloat(), y)
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

    private fun drawHorizontalCursor(helper: RulerViewHelper, canvas: Canvas) {
        if (!cursorOptions.enable) {
            return
        }
        FSize.obtain(view.getCurrentScaleValue().toFloat(), tickOptions.weight)
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
                    it.y.roundToInt()
                )

                // draw
                cursorOptions.getDrawable()?.draw(canvas)
            }.run {
                recycle()
            }
    }


}
