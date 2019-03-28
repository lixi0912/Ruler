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
package com.lixicode.ruler

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.lixicode.ruler.data.*
import com.lixicode.ruler.formatter.ValueFormatter
import com.lixicode.ruler.internal.RulerViewHelper
import com.lixicode.ruler.renderer.RulerViewRenderer
import com.lixicode.ruler.utils.RectPool
import com.lixicode.ruler.utils.Transformer
import com.lixicode.ruler.utils.ViewPortHandler
import kotlin.math.abs
import kotlin.math.max

/**
 * <>
 * @author lixi
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

        @Deprecated(
            message = "this method will be removed in a future release",
            replaceWith = ReplaceWith("onTickChange(oldValue,value,label)"),
            level = DeprecationLevel.WARNING
        )
        fun onTickChanged(value: Float, label: String) {

        }

        /**
         * 值发生改变
         *
         * @since 1.0-rc1
         */
        fun onTickChanged(oldValue: Int, newValue: Int, label: String) {
            onTickChanged(newValue.toFloat(), label)
        }
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
    @Deprecated(message = "use adapter instead", level = DeprecationLevel.WARNING)
    var valueFormatter: ValueFormatter?
        get() = adapter.formatter
        set(value) {
            adapter.formatter = value
            invalidate()
        }

    /**
     * 数据适配器
     *
     * @since 1.0-rc1
     */
    private lateinit var adapter: Adapter


    /**
     * 当前项的刻度值
     *
     */
    internal var tick: Int = 0


    /**
     * 获取当前刻度
     *
     * @since 1.0-rc1
     */
    fun getTick(): Int {
        return tick
    }

    /**
     * 设置当前刻度
     *
     * @param tick current item value
     * @param notify if it is true, callback will be called
     * @since 1.0-rc1
     */
    fun setTick(tick: Int, notify: Boolean = false) {
        setTickInternal(tick, animateTo = false, notify = notify)
    }

    internal fun setTickInternal(position: Int, animateTo: Boolean, notify: Boolean = true) {
        val tick = position.rem(adapter.itemCount + 1)
        val absTick = abs(tick)
        val oldTick = this.tick
        this.tick = absTick
        if (width > 0 && height > 0) {
            scrollHelper.scrollTo(position, animateTo)
        }
        if (notify) {
            dispatchOnTickChanged(oldTick, absTick)
        }
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
    var minimumOfTicks: Int = 0

    /**
     * 最大刻度数
     *
     * @see stepOfTicks
     * @see minimumOfTicks
     */
    var maximumOfTicks: Int = 0


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
     * 获取数据适配器
     *
     * @return 适配器
     * @since 1.0-rc1
     */
    fun getAdapter(): Adapter {
        return adapter
    }

    /**
     * 设置数据适配器
     *
     * @param adapter 适配器
     * @since 1.0-rc1
     */
    fun setAdapter(adapter: Adapter) {
        this.adapter = adapter
        helper.resetLongestLabel()
    }

    /**
     * @param tick [tick]
     * @return index of [minimumOfTicks]..[maximumOfTicks]
     */
    @Deprecated(
        message = "this method will be removed in a future release",
        replaceWith = ReplaceWith("tick"),
        level = DeprecationLevel.WARNING
    )
    fun tickIndex(tick: Int): Int {
        return tick
    }

    var infiniteMode: Boolean
        get() = scrollHelper.infiniteMode
        set(value) {
            scrollHelper.infiniteMode = value
            requestLayoutInternal()
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
        helper.computeMeasureSize()
            .also {
                setMeasuredDimension(
                    resolveSize(
                        max(suggestedMinimumWidth, it.width())
                        , widthMeasureSpec
                    ), resolveSize(
                        max(suggestedMinimumHeight, it.height())
                        , heightMeasureSpec
                    )
                )
            }.run {
                release()
            }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed || forcedRemeasure) {
            forcedRemeasure = false
            dispatchOnSizeChanged(width, height)
        }
    }


    private fun dispatchOnSizeChanged(w: Int, h: Int) {
        helper.onSizeChanged(w, h)
        scrollHelper.onSizeChanged(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.clipRect(
            scrollX + paddingLeft,
            scrollY + paddingTop,
            scrollX + width - paddingRight,
            scrollY + height - paddingBottom
        )

        super.onDraw(canvas)

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

    internal fun positionRangeWithOffset(): IntRange {
        return RectPool.obtain()
            .also {
                it.set(scrollX, scrollY, scrollX, scrollY)
            }
            .mapToRectF()
            .also {
                it.inset(-viewPort.contentWidth, -viewPort.contentHeight)
                if (scrollHelper.minScrollPosition == Int.MIN_VALUE || scrollHelper.maxScrollPosition == Int.MAX_VALUE) {
                    return@also
                }
                RectPool.obtain()
                    .apply {
                        set(
                            scrollHelper.minScrollPosition,
                            scrollHelper.minScrollPosition,
                            scrollHelper.maxScrollPosition,
                            scrollHelper.maxScrollPosition
                        )
                    }
                    .concat(transformer.mMatrixScrollOffset)
                    .mapToRectF()
                    .apply {
                        it.coerceIn(this)
                    }
                    .release()
            }
            .concat(transformer.mMatrixPxToValue)
            .mapToRect()
            .let {
                val range = if (isHorizontal) {
                    it.rangeHorizontal()
                } else {
                    it.rangeVertical()
                }
                it.release()
                range
            }
    }

    private fun dispatchOnTickChanged(oldValue: Int, newValue: Int) {
        tickChangeListener?.onTickChanged(oldValue, newValue, getAdapter().getItemTitle(newValue))
    }


}
