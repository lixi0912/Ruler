package com.lixicode.ruler.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.lixicode.ruler.R
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.data.FSize
import com.lixicode.ruler.data.Options
import com.lixicode.ruler.utils.ViewPortHandler
import kotlin.math.roundToInt

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/7
 */
internal class TickHelper(val view: RulerView) {


    val baseLineOptions: Options<Drawable> = Options()

    val tickOptions: Options<Drawable> = Options()

    val dividerTickOptions: Options<Drawable> = Options()

    val cursorOptions: Options<Drawable> = Options()


    fun loadFromAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.RulerView,
            defStyleAttr, 0
        )
        val weightOfTick = a.getFloat(R.styleable.RulerView_weightOfTick, 1F)
        a.recycle()

        tickOptions.weight = weightOfTick
    }


    fun visibleWidthNeeded(visibleCountOfTick: Int, stepOfTicks: Int): Int {
        val visibleTickWidthNeeded = visibleCountOfTick.times(tickOptions.widthNeeded)
        val dividerWidthNeeded = dividerTickOptions.widthNeeded
        val visibleDividerSpacingNeeded = stepOfTicks.plus(1).times(dividerTickOptions.spacing)
        val visibleDividerWidthNeeded = stepOfTicks.times(dividerWidthNeeded)
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
                        dividerTickOptions.getDrawable()
                    } else {
                        tickOptions.getDrawable()
                    }?.run {

                        // bounds
                        setBounds(
                            it.x.roundToInt(),
                            helper.viewPort.contentTop.roundToInt(),
                            it.x.roundToInt(),
                            it.y.roundToInt()
                        )

                        // draw
                        draw(canvas)
                    }
                }.also {
                    it.recycle()
                }
        }

        cursorOptions.getDrawable()?.run {
            FSize.obtain(view.getCurrentScaleValue().toFloat(), tickOptions.weight)
                .also {
                    helper.transformer.pointValuesToPixel(it)
                }.also {
                    it.x = (view.scrollX + view.width.div(2)).toFloat()
                }.also {
                    // bounds
                    setBounds(
                        it.x.roundToInt(),
                        helper.viewPort.contentTop.roundToInt(),
                        it.x.roundToInt(),
                        it.y.roundToInt()
                    )

                    // draw
                    draw(canvas)
                }.run {
                    recycle()
                }
        }

        // TODO draw event on vertical mode

    }

}
