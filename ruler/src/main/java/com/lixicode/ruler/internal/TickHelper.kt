package com.lixicode.ruler.internal

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.lixicode.ruler.R
import com.lixicode.ruler.data.Options
import com.lixicode.ruler.utils.ViewPortHandler
import kotlin.math.min

/**
 * <>
 * @author 陈晓辉
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


}
