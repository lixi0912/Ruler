package com.lixicode.ruler.internal

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import com.lixicode.ruler.Adapter
import com.lixicode.ruler.R
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.data.release
import com.lixicode.ruler.utils.RectPool
import com.lixicode.ruler.utils.Transformer
import com.lixicode.ruler.utils.ViewPortHandler
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/7
 */
internal class RulerViewHelper(private val view: RulerView) {

    internal val tickHelper = TickHelper()
    internal val viewPort = ViewPortHandler()
    internal val transformer: Transformer = Transformer(viewPort)


    /**
     * 所有刻度中最长的文本
     */
    internal val labelHelper = LabelHelper(view)


    /**
     * 两个刻度间有多少格
     */
    var stepOfTicks: Int = 4

    /**
     * 界面上显示刻度的个数
     */
    var visibleCountOfTick: Int = 4

    /**
     *  是否绘制镜像刻度线，比如上下翻转，左右翻转
     */
    var enableMirrorTick: Boolean = true


    var orientation = RulerView.HORIZONTAL

    val isHorizontal
        get() = orientation == RulerView.HORIZONTAL


    var gravityOfTick = RulerView.GRAVITY_START


    /**
     *  label 相对于 [RulerView] 宽/高的权重
     */
    val weightOfLabel: Float
        get() {
            return labelHelper.labelOptions.weight
        }

    val weightOfTick: Float
        get() {
            return tickHelper.tickOptions.weight
        }


    /**
     *  tick 相对于 [weightOfView] 的权重占比
     */
    val deltaTickWeightOfView: Float
        get() = weightOfTick.div(weightOfView)

    /**
     *  tick 相对于 [weightOfView] 的权重占比
     */
    val deltaLabelWeightOfView: Float
        get() = weightOfLabel.div(weightOfView)


    /**
     *  总权重等于 [weightOfLabel] 和 [weightOfTick] 之和
     *
     *  Note: 当 [enableMirrorTick] = true 时，总权重应该是 [weightOfLabel] + [weightOfTick] * 2
     */
    val weightOfView: Float
        get() {
            val tickWeight = when {
                enableMirrorTick -> weightOfTick.times(2)
                else -> weightOfTick
            }
            return weightOfLabel.plus(tickWeight)
        }

    var horizontalScrollRange: Int = 0
    var verticalScrollRange: Int = 0


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

        val minimumOfTicks = a.getInt(R.styleable.RulerView_ruler_minimumOfTicks, 0)
        val maximumOfTicks = a.getInt(R.styleable.RulerView_ruler_maximumOfTicks, 100)
        val stepOfTicks = a.getInt(R.styleable.RulerView_ruler_stepOfTicks, stepOfTicks)
        val enableMirrorTick = a.getBoolean(R.styleable.RulerView_ruler_enableMirrorTick, enableMirrorTick)
        val orientation = a.getInt(R.styleable.RulerView_android_orientation, orientation)
        val gravityOfTick = a.getInt(R.styleable.RulerView_ruler_gravityOfTick, gravityOfTick)
        val visibleCountOfTick = a.getInt(R.styleable.RulerView_ruler_visibleCountOfTick, visibleCountOfTick)

        val tick = a.getInt(R.styleable.RulerView_ruler_tick, 0)
        a.recycle()

        this.visibleCountOfTick = visibleCountOfTick
        this.gravityOfTick = gravityOfTick
        this.orientation = orientation
        this.stepOfTicks = stepOfTicks
        this.enableMirrorTick = enableMirrorTick

        labelHelper.loadFromAttributes(context, attrs, defStyleAttr, defStyleRes)
        tickHelper.loadFromAttributes(context, attrs, defStyleAttr, defStyleRes)

        val adapter = object : Adapter() {
            override val itemCount: Int
                get() = maximumOfTicks - minimumOfTicks
        }

        view.setAdapter(adapter)

        setTickValue(tick)
    }

    /**
     * @return true [tick] is remainder of ticks
     */
    fun remOfTick(tick: Int): Int {
        return abs(tick.rem(view.getAdapter().itemCount).rem(stepOfTicks))
    }


    private fun setTickValue(tick: Int) {
        this.view.tick = tick
    }

    fun resetLongestLabel(label: String? = null) {
        labelHelper.resetLongestLabel(label)
    }


    var minimunMeasureWidth = 0
    var minimumMeasureHeight = 0

    fun computeMeasureSize(): Rect {
        // reset offset
        RectPool.obtain()
            .also {
                viewPort.setOffset(it)
            }
            .release()

        // reset textSize
        labelHelper.resetTextSize()

        return if (isHorizontal) {
            computeHorizontalSize(viewPort)
        } else {
            computeVerticalSize(viewPort)
        }
    }


    private fun computeHorizontalSize(viewPort: ViewPortHandler): Rect {
        // 计算所需要的偏移值
        tickHelper.computeHorizontalOffset(viewPort)

        val visibleTextWidth = labelHelper.visibleWidthNeeded(visibleCountOfTick, stepOfTicks)
        val visibleTickWidth = tickHelper.visibleWidthNeeded(visibleCountOfTick, stepOfTicks)

        minimunMeasureWidth = max(visibleTextWidth, visibleTickWidth)
        minimumMeasureHeight = labelHelper.visibleHeightNeeded().times(weightOfView).roundToInt()


        val width = minimunMeasureWidth.plus(view.paddingLeft).plus(view.paddingRight)
        val height = minimumMeasureHeight.plus(view.paddingTop).plus(view.paddingBottom)

        return RectPool.obtain().also { it.set(0, 0, width, height) }
    }


    private fun computeVerticalSize(viewPort: ViewPortHandler): Rect {
        // 计算所需要的偏移值
        tickHelper.computeVerticalOffset(viewPort)

        minimunMeasureWidth = labelHelper.labelOptions.widthNeeded.times(weightOfView)
            .plus(labelHelper.labelOptions.spacing.times(2)).roundToInt()


        val visibleTextHeight = labelHelper.visibleHeightNeeded(visibleCountOfTick, stepOfTicks)
        val visibleTickWidth = tickHelper.visibleHeightNeeded(visibleCountOfTick, stepOfTicks)

        minimumMeasureHeight = max(visibleTextHeight, visibleTickWidth)

        val width = minimunMeasureWidth.plus(view.paddingLeft).plus(view.paddingRight)
        val height = minimumMeasureHeight.plus(view.paddingTop).plus(view.paddingBottom)
        return RectPool.obtain().also { it.set(0, 0, width, height) }
    }

    fun onSizeChanged(w: Int, h: Int) {
        RectPool.obtain()
            .also {
                it.set(
                    view.paddingLeft,
                    view.paddingTop,
                    w - view.paddingRight,
                    h - view.paddingBottom
                )
                viewPort.setDimens(it)
            }.release()


        labelHelper.autoTextSize(viewPort)

        transformer.prepareMatrixValuePx(this)
    }

}
