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
package com.lixicode.ruler.internal

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.lixicode.ruler.R
import com.lixicode.ruler.data.Options
import com.lixicode.ruler.data.release
import com.lixicode.ruler.data.set
import com.lixicode.ruler.utils.RectFPool
import com.lixicode.ruler.utils.RectPool
import com.lixicode.ruler.utils.ViewPortHandler
import kotlin.math.min

/**
 * <>
 * @author lixi
 * @date 2019/3/7
 */
internal class TickHelper {


    internal val tickOptions: Options<Drawable> = Options()

    internal val baseLineOptions: Options<Drawable> = Options()

    internal val dividerTickOptions: Options<Drawable> = Options()

    internal val cursorOptions: Options<Drawable> = Options()


    /**
     * 比如
     *
     * [stepOfTicks] = 10
     *
     * [significantTickWeight] 代表中间项 5 的权重，其距离应大等于 [1..4],[6..9] 的权重，小等于 0，10 的权重
     *      。
     * -----------
     * |    |    |
     * |         |
     * 0         10
     *
     *
     * @see TickHelper.dividerTickOptions.weight
     *
     *
     */
    var significantTickWeight: Float = 0F
        set(value) {
            field = value.coerceIn(
                dividerTickOptions.weight,
                tickOptions.weight
            )
        }

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
        val significantTickWeight = a.getFloat(R.styleable.RulerView_ruler_significantTickWeight, significantTickWeight)

        a.recycle()

        this.significantTickWeight = significantTickWeight


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

    fun visibleHeightNeeded(visibleCountOfTick: Int, stepOfTicks: Int): Int {
        val spacingCount = stepOfTicks.plus(1)
        val visibleDividerSpacingNeeded = spacingCount.times(dividerTickOptions.spacing)
            .times(visibleCountOfTick).times(stepOfTicks)

        val visibleDividerWidthNeeded = stepOfTicks.times(dividerTickOptions.heightNeeded)

        val visibleTickWidthNeeded = tickOptions.heightNeeded.times(visibleCountOfTick)
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
        val size = if (baseLineOptions.enable) {
            min(baseLineOptions.widthNeeded, baseLineOptions.heightNeeded).times(2)
        } else {
            min(tickOptions.widthNeeded, tickOptions.heightNeeded).times(2)
        }

        RectPool.obtain()
            .also {
                it.set(size, size, size, size)
                viewPort.setOffset(it)
                it.release()
            }
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
        val size = if (baseLineOptions.enable) {
            min(baseLineOptions.widthNeeded, baseLineOptions.heightNeeded).times(2)
        } else {
            min(tickOptions.widthNeeded, tickOptions.heightNeeded).times(2)
        }

        RectPool.obtain()
            .also {
                it.set(size, size, size, size)
                viewPort.setOffset(it)
                it.release()
            }
    }


}
