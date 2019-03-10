package com.lixicode.ruler

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
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

    var valueFormatter: ValueFormatter = object : ValueFormatter {}

    internal val helper by lazy {
        RulerViewHelper(this)
    }


    internal val scrollHelper by lazy {
        ScrollHelper(this, helper)
    }

    val isHorizontal: Boolean
        get() = helper.isHorizontal


    var tick: Int = 0
        set(value) {
            field = helper.coerceInTicks(value)
            scrollHelper.scrollTo(field)
        }

    init {
        helper.loadFromAttributes(context, attrs, defStyleAttr, defStyleRes)
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
