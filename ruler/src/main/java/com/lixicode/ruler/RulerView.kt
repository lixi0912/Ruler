package com.lixicode.ruler

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.lixicode.ruler.data.Options
import com.lixicode.ruler.formatter.ValueFormatter
import com.lixicode.ruler.internal.RulerViewHelper
import com.lixicode.ruler.renderer.RulerViewRenderer
import com.lixicode.ruler.utils.Transformer
import com.lixicode.ruler.utils.ViewPortHandler
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
class RulerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = R.style.RulerView
) : View(context, attrs, defStyleAttr) {

    companion object {

        const val INVALID_POINTER = -1

        const val HORIZONTAL = 0
        const val VERTICAL = 1

        const val GRAVITY_START = 0
        const val GRAVITY_END = 1

        const val MAX_OVER_SCROLL_EDGE = 100

    }

    interface OnTickChangedListener {
        fun onTickChanged(value: Float, label: String)
    }


    internal val helper: RulerViewHelper = RulerViewHelper(this)

    internal val transformer: Transformer
        get() = helper.transformer

    internal val viewPort: ViewPortHandler
        get() = helper.viewPort

    private val scrollHelper: ScrollHelper

    var tickChangeListener: OnTickChangedListener? = null

    /**
     * 当前显示样式
     */
    val isHorizontal: Boolean
        get() = helper.isHorizontal


    /**
     * 用于格式化显示的 Label
     */
    var valueFormatter: ValueFormatter
        get() = helper.valueFormatter
        set(value) {
            helper.valueFormatter = value
            invalidate()
        }

    /**
     * 当前项的刻度值
     */
    var tick: Int = 0
        set(value) {
            field = helper.coerceInTicks(value)
            scrollHelper.scrollTo(field)
        }

    /**
     * 两个刻度相距的距离
     *
     * @see minimumOfTicks
     * @see maximumOfTicks
     */
    var stepOfTicks: Int
        get() {
            return helper.stepOfTicks
        }
        set(value) {
            if (value != helper.stepOfTicks) {
                helper.stepOfTicks = value
                requestLayoutInternal()
            }
        }

    /**
     * 最小刻度数
     *
     * @see stepOfTicks
     * @see maximumOfTicks
     */
    var minimumOfTicks: Int
        get() {
            return helper.minimumOfTicks
        }
        set(value) {
            if (value != helper.minimumOfTicks) {
                helper.minimumOfTicks = value
                requestLayoutInternal()
            }
        }

    /**
     * 最大刻度数
     *
     * @see stepOfTicks
     * @see minimumOfTicks
     */
    var maximumOfTicks: Int
        get() {
            return helper.maximumOfTicks
        }
        set(value) {
            if (value != helper.maximumOfTicks) {
                helper.maximumOfTicks = value
                requestLayoutInternal()
            }
        }


    /**
     * 刻度的显示方向
     *
     * [HORIZONTAL]
     * 1. [GRAVITY_START] 将会绘制顶部
     * 2. [GRAVITY_END] 将会绘制底部
     *
     * [VERTICAL]
     * 1. [GRAVITY_START] 将会绘制左边
     * 2. [GRAVITY_END] 将会绘制右边
     *
     * 注意，该设置在 [enableMirrorTick] = true 下设置无效
     *
     * @see enableMirrorTick
     */
    var gravityOfTick: Int
        get() {
            return helper.gravityOfTick
        }
        set(value) {
            if (value != helper.gravityOfTick) {
                helper.gravityOfTick = value
                requestLayoutInternal()
            }
        }

    /**
     * 当前项目的显示方向
     *
     * [HORIZONTAL]
     *
     * -----------
     *
     * -----------
     *
     * [VERTICAL]
     *
     * |  |
     * |  |
     * |  |
     * |  |
     *
     * @see HORIZONTAL
     * @see VERTICAL
     */
    var orientation: Int
        get() {
            return helper.orientation
        }
        set(value) {
            if (value != helper.orientation) {
                helper.orientation = value
                requestLayoutInternal()
            }
        }

    /**
     *  是否镜像绘制刻度线，比如上下翻转，左右翻转
     *
     *  [enableMirrorTick] = false
     *  -----------------------
     *  |     |
     *
     *  1     5
     *
     *  [enableMirrorTick] = true
     *  -----------------------
     *  |     |
     *
     *  1     5
     *
     *  |     |
     *  -----------------------
     *
     */
    var enableMirrorTick: Boolean
        get() {
            return helper.enableMirrorTick
        }
        set(value) {
            if (value != helper.enableMirrorTick) {
                helper.enableMirrorTick = value
                requestLayoutInternal()
            }
        }

    private val renderer: RulerViewRenderer

    private var forcedRemeasure = false

    init {

        scrollHelper = ScrollHelper(this, helper)

        renderer = RulerViewRenderer(helper)

        helper.loadFromAttributes(context, attrs, defStyleAttr, defStyleRes)
    }


    /**
     * @param tick [tick]
     * @return index of [minimumOfTicks]..[maximumOfTicks]
     */
    fun tickIndex(tick: Int): Int {
        return helper.tickIndex(tick)
    }

    fun updateBaseLineOptions(onOptionsUpdated: (options: Options<Drawable>) -> Boolean) {
        if (onOptionsUpdated(helper.tickHelper.baseLineOptions)) {
            requestLayoutInternal()
        } else {
            invalidate()
        }
    }


    fun updateTickOptions(onOptionsUpdated: (options: Options<Drawable>) -> Boolean) {
        if (onOptionsUpdated(helper.tickHelper.tickOptions)) {
            requestLayout()
        } else {
            invalidate()
        }
    }

    fun updateDividerLineOptions(onOptionsUpdated: (options: Options<Drawable>) -> Boolean) {
        if (onOptionsUpdated(helper.tickHelper.dividerTickOptions)) {
            requestLayoutInternal()
        } else {
            invalidate()
        }
    }

    fun updateLabelOptions(onOptionsUpdated: (options: Options<*>) -> Boolean) {
        if (onOptionsUpdated(helper.labelHelper.labelOptions)) {
            requestLayoutInternal()
        } else {
            invalidate()
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        helper.computeMeasureSize(widthMeasureSpec, heightMeasureSpec)
            .also {
                setMeasuredDimension(
                    resolveSize(
                        max(suggestedMinimumWidth, it.x.toInt())
                        , widthMeasureSpec
                    ), resolveSize(
                        max(suggestedMinimumHeight, it.y.toInt())
                        , heightMeasureSpec
                    )
                )
            }.run {
                recycle()
            }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed || forcedRemeasure) {
            dispatchOnSizeChanged(width, height)
        }
    }

    private fun dispatchOnSizeChanged(w: Int, h: Int) {
        helper.onSizeChanged(w, h)
        scrollHelper.onSizeChanged(w, h)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.clipRect(
            scrollX + paddingLeft,
            scrollY + paddingTop,
            scrollX + width - paddingRight,
            scrollY + height - paddingBottom
        )
        renderer.onDraw(this, canvas)

    }

    override fun onAttachedToWindow() {
        scrollHelper.onAttachedToWindow()
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        scrollHelper.onDetachedFromWindow()
        super.onDetachedFromWindow()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return scrollHelper.onTouchEvent(event!!)
    }

    override fun computeScroll() {
        super.computeScroll()
        scrollHelper.computeScroll()
    }

    private fun requestLayoutInternal() {
        forcedRemeasure = true
        requestLayout()
        invalidate()
    }


    override fun computeHorizontalScrollRange(): Int {
        return helper.horizontalScrollRange
    }

    override fun computeVerticalScrollRange(): Int {
        return helper.verticalScrollRange
    }


    internal fun computeCanvasPaddingByHorizontal(): Int {
        return paddingBottom.minus(paddingTop).takeIf {
            it > 0
        } ?: 0
    }


    internal fun computeCanvasPaddingByVertical(): Int {
        return paddingRight.minus(paddingLeft).takeIf {
            it > 0
        } ?: 0
    }

}
