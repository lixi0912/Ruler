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
import kotlin.math.max

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


    internal val helper by lazy {
        RulerViewHelper(this)
    }


    private val scrollHelper by lazy {
        ScrollHelper(this, helper)
    }

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
                requestLayout()
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
                requestLayout()
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
                requestLayout()
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
                requestLayout()
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
                requestLayout()
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
                requestLayout()
            }
        }


    init {
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
            requestLayout()
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
            requestLayout()
        } else {
            invalidate()
        }
    }

    fun updateLabelOptions(onOptionsUpdated: (options: Options<*>) -> Boolean) {
        if (onOptionsUpdated(helper.labelHelper.labelOptions)) {
            requestLayout()
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


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
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

        helper.onDraw(canvas)

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

    override fun computeHorizontalScrollRange(): Int {
        return helper.horizontalScrollRange
    }

    override fun computeVerticalScrollRange(): Int {
        return helper.verticalScrollRange
    }


}
