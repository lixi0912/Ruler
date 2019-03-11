package com.lixicode.ruler.internal

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.lixicode.ruler.R
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.data.FSize
import com.lixicode.ruler.formatter.ValueFormatter
import com.lixicode.ruler.utils.Transformer
import com.lixicode.ruler.utils.ViewPortHandler
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/7
 */
internal class RulerViewHelper(private val view: RulerView) {

    internal val tickHelper = TickHelper(view)
    internal val viewPort = ViewPortHandler()
    internal val transformer: Transformer = Transformer(viewPort)


    /**
     * 所有刻度中最长的文本
     */
    internal val labelHelper = LabelHelper(view)

    /**
     * 最小刻度
     */
    var minimumOfTicks: Int = 0

    /**
     * 最大刻度
     */
    var maximumOfTicks: Int = 100

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

    var valueFormatter: ValueFormatter = object : ValueFormatter {}


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

    var significantTickWeight: Float = 0F


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
    val weightOfView: Int
        get() {
            val tickWeight = when {
                enableMirrorTick -> weightOfTick.times(2)
                else -> weightOfTick
            }
            return weightOfLabel.plus(tickWeight).roundToInt()
        }

    /**
     * 每次绘制的刻度数
     */
    var visibleTickCount: Int = 0


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

        val minimumOfTicks = a.getInt(R.styleable.RulerView_ruler_minimumOfTicks, minimumOfTicks)
        val maximumOfTicks = a.getInt(R.styleable.RulerView_ruler_maximumOfTicks, maximumOfTicks)
        val stepOfTicks = a.getInt(R.styleable.RulerView_ruler_stepOfTicks, stepOfTicks)
        val enableMirrorTick = a.getBoolean(R.styleable.RulerView_ruler_enableMirrorTick, enableMirrorTick)
        val orientation = a.getInt(R.styleable.RulerView_android_orientation, orientation)
        val gravityOfTick = a.getInt(R.styleable.RulerView_ruler_gravityOfTick, gravityOfTick)
        val visibleCountOfTick = a.getInt(R.styleable.RulerView_ruler_visibleCountOfTick, visibleCountOfTick)
        val significantTickWeight = a.getFloat(R.styleable.RulerView_ruler_significantTickWeight, significantTickWeight)

        val tick = a.getInt(R.styleable.RulerView_ruler_tick, 0)
        a.recycle()

        this.visibleCountOfTick = visibleCountOfTick
        this.gravityOfTick = gravityOfTick
        this.orientation = orientation
        this.stepOfTicks = stepOfTicks
        this.minimumOfTicks = minimumOfTicks
        this.maximumOfTicks = maximumOfTicks
        this.enableMirrorTick = enableMirrorTick


        labelHelper.loadFromAttributes(context, attrs, defStyleAttr, defStyleRes)
        tickHelper.loadFromAttributes(context, attrs, defStyleAttr, defStyleRes)

        this.significantTickWeight =
            significantTickWeight.coerceIn(tickHelper.dividerTickOptions.weight, tickHelper.tickOptions.weight)


        setTickValue(tick)
    }

    fun rangeOfTickWithScrollOffset(): IntRange {
        val startValue = FSize.obtain(view.scrollX.toFloat(), view.scrollY.toFloat())
            .also {
                transformer.invertPixelToValue(it)
            }.let {
                if (isHorizontal) {
                    coerceInTicks(it.x.roundToInt())
                } else {
                    coerceInTicks(it.y.roundToInt())
                }.apply {
                    it.recycle()
                }
            }

        val endValue = (startValue + visibleTickCount).coerceAtMost(maximumOfTicks)
        return startValue..endValue
    }

    /**
     * @return true [tick] is remainder of ticks
     */
    fun remOfTick(tick: Int): Int {
        val tickIndex = coerceInTicks(tick).minus(minimumOfTicks)
        return tickIndex.rem(stepOfTicks)
    }

    fun tickIndex(tick: Int): Int {
        return coerceInTicks(tick).minus(minimumOfTicks)
    }


    fun coerceInTicks(value: Int): Int {
        return value.coerceIn(minimumOfTicks, maximumOfTicks)
    }


    private fun setTickValue(tick: Int) {
        this.view.tick = tick
    }

    fun setLongestLabel(label: String?, sameLengthOfLabel: Boolean) {
        labelHelper.setLongestLabel(label, sameLengthOfLabel)
    }


    private fun computeCanvasPaddingByHorizontal(): Int {
        return view.paddingBottom.minus(view.paddingTop)
            .takeIf {
                it > 0
            } ?: 0
    }


    private fun computeCanvasPaddingByVertical(): Int {
        return view.paddingRight.minus(view.paddingLeft)
            .takeIf {
                it > 0
            } ?: 0
    }

    private fun drawOnMirrorTick(canvas: Canvas, drawMirror: (Canvas) -> Unit) {
        if (enableMirrorTick || gravityOfTick == RulerView.GRAVITY_END) {
            val saveId = canvas.save()
            if (isHorizontal) {
                canvas.scale(1F, -1F)

                val deltaY = (-view.height).plus(computeCanvasPaddingByHorizontal())
                    .toFloat()

                canvas.translate(0F, deltaY)
            } else {
                // TODO fix me
                canvas.scale(-1F, 1F)

                val deltaX = (-view.width).plus(computeCanvasPaddingByVertical())
                    .toFloat()

                canvas.translate(deltaX, 0F)
            }

            drawMirror(canvas)
            canvas.restoreToCount(saveId)
        }
    }

    fun computeMeasureSize(widthMeasureSpec: Int, heightMeasureSpec: Int): FSize {
        // reset offset
        viewPort.offsetRect.setEmpty()

        return if (isHorizontal) {
            computeHorizontalSize(viewPort, widthMeasureSpec, heightMeasureSpec)
        } else {
            computeVerticalSize(viewPort, widthMeasureSpec, heightMeasureSpec)
        }
    }


    private fun computeHorizontalSize(
        viewPort: ViewPortHandler,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ): FSize {

        // 计算所需要的偏移值
        tickHelper.computeHorizontalOffset(viewPort)

        val width = when {
            View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.EXACTLY -> {
                View.MeasureSpec.getSize(
                    widthMeasureSpec
                ).apply {
                    TODO("auto text size if need")
                }
            }
            else -> {
                val visibleTextWidth = labelHelper.visibleWidthNeeded(visibleCountOfTick, stepOfTicks)
                val visibleTickWidth = tickHelper.visibleWidthNeeded(visibleCountOfTick, stepOfTicks)
                max(visibleTextWidth, visibleTickWidth)
            }
        }.plus(view.paddingLeft).plus(view.paddingRight)

        val height = when {
            View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.EXACTLY -> {
                View.MeasureSpec.getSize(
                    heightMeasureSpec
                ).apply {
                    TODO("auto text size if need")
                }
            }
            else -> {
                labelHelper.visibleHeightNeeded()
                    .times(weightOfView)
            }
        }.plus(view.paddingTop).plus(view.paddingBottom)


        return FSize.obtain(width, height)
    }

    private fun computeVerticalSize(
        viewPort: ViewPortHandler,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ): FSize {
        // 计算所需要的偏移值
        tickHelper.computeVerticalOffset(viewPort)

        val width = when {
            View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.EXACTLY -> {
                View.MeasureSpec.getSize(
                    widthMeasureSpec
                ).apply {
                    TODO("auto text size if need")
                }
            }
            else -> {
                labelHelper.labelOptions.widthNeeded.times(weightOfView)
                    .plus(labelHelper.labelOptions.spacing.times(2))
            }
        }.plus(view.paddingLeft).plus(view.paddingRight)

        val height = when {
            View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.EXACTLY -> {
                View.MeasureSpec.getSize(
                    heightMeasureSpec
                ).apply {
                    TODO("auto text size if need")
                }
            }
            else -> {
                val visibleTextHeight = labelHelper.visibleHeightNeeded() * visibleCountOfTick

                val visibleTickWidth = tickHelper.visibleWidthNeeded(visibleCountOfTick, stepOfTicks)
                max(visibleTextHeight, visibleTickWidth)


            }
        }.plus(view.paddingTop).plus(view.paddingBottom)


        return FSize.obtain(width, height)
    }

    fun onSizeChanged(w: Int, h: Int) {
        val left = view.paddingLeft.toFloat()
        val top = view.paddingTop.toFloat()
        val right = w.toFloat() - view.paddingRight
        val bottom = h.toFloat() - view.paddingBottom

        viewPort.setDimens(left, top, right, bottom)

        transformer.prepareMatrixValuePx(this)

        this.visibleTickCount = FSize.obtain(w.toFloat(), h.toFloat())
            .also {
                transformer.invertPixelToValue(it)
            }.let {
                if (isHorizontal) {
                    it.x.roundToInt() - minimumOfTicks
                } else {
                    it.y.roundToInt() - minimumOfTicks
                }.apply {
                    it.recycle()
                }
            }
    }


    fun onDraw(canvas: Canvas) {

        // draw tick
        if (gravityOfTick == RulerView.GRAVITY_START || enableMirrorTick) {
            tickHelper.onDraw(canvas)
        }

        // draw on mirror  
        drawOnMirrorTick(canvas) {

            tickHelper.onDraw(it)
        }

        // draw label
        labelHelper.onDraw(canvas)
    }


    fun generateValueToPixel(value: Int): FSize {
        return transformer.generateValueToPixel(value)
    }

    fun invertPixelToValue(x: Int, y: Int): FSize {
        return transformer.invertPixelToValue(x, y)
    }


}
