package com.lixicode.ruler

import android.content.Context
import android.graphics.Canvas
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.OverScroller
import com.lixicode.ruler.data.*
import com.lixicode.ruler.formatter.ValueFormatter
import com.lixicode.ruler.renderer.LabelRenderer
import com.lixicode.ruler.renderer.Renderer
import com.lixicode.ruler.renderer.XAxisRenderer
import com.lixicode.ruler.utils.Transformer
import com.lixicode.ruler.utils.Utils
import com.lixicode.ruler.utils.ViewPortHandler
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
class RulerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val HORIZONTAL = LinearLayout.HORIZONTAL
        const val VERTICAL = LinearLayout.VERTICAL

        const val MAX_OVER_SCROLL_EDGE = 100
    }

    var axis: Axis
    var axisRenderer: Renderer
    var origintation = HORIZONTAL

    val viewPort = ViewPortHandler()
    val labelRenderer: Renderer

    val scroller: OverScroller = OverScroller(context)


    private var minScrollPosition: Int = 0
    private var maxScrollPosition: Int = 0

    private var totalScrollRangeX: Int = 0
    private var totalScrollRangeY: Int = 0

    private val transformer: Transformer
    var valueFormatter: ValueFormatter = object : ValueFormatter {}

    init {
        Utils.init(context)
        this.transformer = Transformer(viewPort)


        val xAxis = XAxis()
        this.axis = xAxis

        xAxis.labelOptions = LabelOptions()
        xAxis.baselineOptions = LineOptions()
        xAxis.scaleLineOptions = LineOptions()
        xAxis.dividerLineOptions = LineOptions()


        this.labelRenderer = LabelRenderer(this)
        this.axisRenderer = XAxisRenderer(this)

    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {


        // reset offset
        viewPort.offsetRect.setEmpty()

        val labelSize = labelRenderer.computeSize(widthMeasureSpec, 0F, heightMeasureSpec, 0F)
        val axisSize = axisRenderer.computeSize(widthMeasureSpec, labelSize.x, heightMeasureSpec, labelSize.y)
        labelSize.recycle()

        setMeasuredDimension(
            resolveSize(
                max(suggestedMinimumWidth, axisSize.x.toInt())
                , widthMeasureSpec
            ), resolveSize(
                max(suggestedMinimumHeight, axisSize.y.toInt())
                , heightMeasureSpec
            )
        )
        axisSize.recycle()

        viewPort.setDimens(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            measuredWidth.toFloat() - paddingRight,
            measuredHeight.toFloat() - paddingBottom
        )

        computeXAxisSize()
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val startTimeMillis = System.currentTimeMillis()

        canvas?.run {

            axisRenderer.draw(this, transformer)
            if (axis.repeat) {
                val saveId = save()
                scale(1F, -1F)
                translate(0F, -height.toFloat())
                axisRenderer.draw(this, transformer)
                restoreToCount(saveId)
            }

            labelRenderer.draw(this, transformer)
        }


        val endTimeMillis = System.currentTimeMillis()
        val usedTimeMillis = endTimeMillis - startTimeMillis

        Log.e(RulerView::class.java.simpleName, "cost $usedTimeMillis milliseconds on draw event")

    }

    fun getLongestMeasuredText(): String {
        return if (TextUtils.isEmpty(axis.labelOptions.longestLabelText)) {
            if (axis.labelOptions.identicalLengthOfLabel) {
                valueFormatter.formatValue(axis.minValue.toFloat())
            } else {
                var longestString = ""
                var longestLength = 0
                for (index in 0..axis.range) {
                    val formatted = valueFormatter.formatValue((axis.minValue + (index * axis.scaleLineStep)).toFloat())
                    if (formatted.length > longestLength) {
                        longestString = formatted
                        longestLength = formatted.length
                    }
                }
                longestString
            }.apply {
                axis.labelOptions.longestLabelText = this
            }
        } else {
            axis.labelOptions.longestLabelText
        }
    }

    fun getCurrentScaleValue(): Int {
        val scrollPts = FSize.obtain(scrollX.toFloat(), scrollY.toFloat())
        transformer.invertPixelToValue(scrollPts)
        return if (origintation == RulerView.HORIZONTAL) {
            when {
                scrollPts.x < axis.minValue -> axis.minValue
                scrollPts.x > axis.maxValue -> axis.maxValue
                else -> scrollPts.x.roundToInt()
            }
        } else {
            when {
                scrollPts.y < axis.minValue -> axis.minValue
                scrollPts.y > axis.maxValue -> axis.maxValue
                else -> scrollPts.y.roundToInt()
            }
        }.apply {
            scrollPts.recycle()
        }
    }


    fun getScaleValueRangePerScreen(): Int {
        val scrollPts = FSize.obtain(width.toFloat(), height.toFloat())
        transformer.invertPixelToValue(scrollPts)
        return if (origintation == RulerView.HORIZONTAL) {
            scrollPts.x.roundToInt() - axis.minValue
        } else {
            scrollPts.y.roundToInt() - axis.minValue
        }.apply {
            scrollPts.recycle()
        }
    }


    private var mLastTouchPoint: FSize? = null
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val isHorizontal = origintation == HORIZONTAL
        return event?.run {
            val lastPoint = (mLastTouchPoint ?: FSize.obtain(x, y)).apply {
                mLastTouchPoint = this
            }


            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    abortScrollAnimation()
                    // disable parent scroll event
                    parent?.requestDisallowInterceptTouchEvent(true)
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isHorizontal) {
                        val offsetX = lastPoint.offsetX(x)
                        overScrollBy(-offsetX.roundToInt(), 0)
                    } else {
                        val offsetY = lastPoint.offsetY(y)
                        scrollBy(0, offsetY.toInt())
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // release parent scroll event
                    parent?.requestDisallowInterceptTouchEvent(false)

                    recycleTouchPoint()

                    scrollToNearByValue()
                }
            }
            true
        } ?: super.onTouchEvent(event)
    }


    private fun scrollToNearByValue() {
        var dx = 0
        var dy = 0

        var isDirty = false
        if (origintation == HORIZONTAL) {
            if (scrollX < minScrollPosition) {
                isDirty = true
                dx = minScrollPosition
            } else if (scrollX > maxScrollPosition) {
                isDirty = true
                dx = maxScrollPosition
            }
            dy = 0
        } else {
            // TODO 竖排模式回弹
        }

        if (isDirty) {
            scroller.startScroll(
                scrollX,
                scrollY,
                dx - scrollX,
                dy - scrollY

            )
            invalidate()
        }

        // TODO 滑动结束移动到最近一项
    }

    private fun recycleTouchPoint() {
        mLastTouchPoint?.recycle()
        mLastTouchPoint = null
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, scroller.currY)
            invalidate()
        }
    }


    fun abortScrollAnimation() {
        if (!scroller.isFinished) {
            scroller.abortAnimation()
        }
    }


    private fun overScrollBy(x: Int, y: Int): Boolean {
        val canScrollHorizontal =
            overScrollMode != OVER_SCROLL_NEVER && computeHorizontalScrollRange() > computeHorizontalScrollExtent()
        val canScrollVertical =
            overScrollMode != OVER_SCROLL_NEVER && computeVerticalScrollRange() > computeVerticalScrollExtent()
        val overScrollHorizontal =
            overScrollMode == OVER_SCROLL_ALWAYS || overScrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollHorizontal
        val overScrollVertical =
            overScrollMode == OVER_SCROLL_ALWAYS || overScrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollVertical


        var newScrollX = scrollX + x
        val maxOverScrollX = if (!overScrollHorizontal) {
            0
        } else {
            MAX_OVER_SCROLL_EDGE
        }

        var newScrollY = scrollY + y
        val maxOverScrollY = if (!overScrollVertical) {
            0
        } else {
            MAX_OVER_SCROLL_EDGE
        }

        var clampedX = false
        if (canScrollHorizontal) {
            val left = minScrollPosition - maxOverScrollX
            val right = maxOverScrollX + maxScrollPosition

            if (newScrollX > right) {
                newScrollX = right
                clampedX = true
            } else if (newScrollX < left) {
                newScrollX = left
                clampedX = true
            }

        }

        var clampedY = false
        if (canScrollVertical) {
            val top = -maxOverScrollY
            val bottom = maxOverScrollY + maxScrollPosition

            if (newScrollY > bottom) {
                newScrollY = bottom
                clampedY = true
            } else if (newScrollY < top) {
                newScrollY = top
                clampedY = true
            }
        }

        onOverScrolled(newScrollX, newScrollY, clampedX, clampedY)

        return clampedX || clampedY
    }


    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        scrollTo(scrollX, scrollY)
    }


    private fun computeXAxisSize() {
        val minValue = axis.minValue.toFloat()
        val maxValue = axis.maxValue.toFloat()

        transformer.prepareMatrixValuePx(
            minValue,
            axis.visibleDividerLineCount,
            0F, 3
        )

        val maxScrollValuePts = FSize.obtain(maxValue, maxValue)
        val minxScrollValuePts = FSize.obtain(minValue, minValue)


        transformer.pointValuesToPixel(maxScrollValuePts)
        transformer.pointValuesToPixel(minxScrollValuePts)

        if (origintation == HORIZONTAL) {
            minScrollPosition = (minxScrollValuePts.x - viewPort.offsetLeft).roundToInt()
            maxScrollPosition =
                (maxScrollValuePts.x - width + axis.scaleLineOptions.widthNeeded + viewPort.offsetRight).roundToInt()

            scrollX = minScrollPosition

            totalScrollRangeX = maxScrollPosition - minScrollPosition
        } else {

            minScrollPosition = minxScrollValuePts.y.roundToInt()
            maxScrollPosition = maxScrollValuePts.y.roundToInt() - height

            scrollY = minScrollPosition

            totalScrollRangeY = maxScrollPosition - minScrollPosition
        }


        maxScrollValuePts.recycle()
        minxScrollValuePts.recycle()
    }

    override fun computeHorizontalScrollRange(): Int {
        return totalScrollRangeX
    }

    override fun computeVerticalScrollRange(): Int {
        return totalScrollRangeY
    }

} 
