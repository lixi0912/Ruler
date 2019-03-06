package com.lixicode.ruler

import android.content.Context
import android.graphics.Canvas
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.LinearLayout
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper.INVALID_POINTER
import com.lixicode.ruler.data.*
import com.lixicode.ruler.formatter.ValueFormatter
import com.lixicode.ruler.renderer.CursorRenderer
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


        const val TOUCH_STATE_INIT = 0
        const val TOUCH_STATE_MOVING = 1
        const val TOUCH_STATE_FLING = 2
        const val TOUCH_STATE_REST = 3

    }

    var axis: Axis
    var axisRenderer: Renderer
    var cursorRenderer: Renderer
    var origintation = HORIZONTAL

    val viewPort = ViewPortHandler()
    val labelRenderer: Renderer

    val scroller: OverScroller = OverScroller(context)


    private var minScrollPosition: Int = 0
    private var maxScrollPosition: Int = 0


    private val transformer: Transformer
    var valueFormatter: ValueFormatter = object : ValueFormatter {}


    private val mTouchSlop: Int
    private val mMinimumVelocity: Int
    private val mMaximumVelocity: Int

    init {
        Utils.init(context)

        val configuration = ViewConfiguration.get(getContext())
        mTouchSlop = configuration.scaledTouchSlop
        mMinimumVelocity = configuration.scaledMinimumFlingVelocity
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity

        this.transformer = Transformer(viewPort)


        val xAxis = XAxis()
        this.axis = xAxis

        xAxis.labelOptions = LabelOptions()
        xAxis.baselineOptions = LineOptions()
        xAxis.scaleLineOptions = LineOptions()
        xAxis.dividerLineOptions = LineOptions()


        this.labelRenderer = LabelRenderer(this)
        this.axisRenderer = XAxisRenderer(this)
        this.cursorRenderer = CursorRenderer(this, CursorOptions(null))

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

        computeXAxisSize()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        val startTimeMillis = System.currentTimeMillis()
        canvas.clipRect(
            scrollX,
            scrollY,
            scrollX + width,
            scrollY + height
        )

        axisRenderer.draw(canvas, transformer)
        cursorRenderer.draw(canvas, transformer)

        if (axis.repeat) {
            val saveId = canvas.save()
            canvas.scale(1F, -1F)
            canvas.translate(0F, -height.toFloat())
            axisRenderer.draw(canvas, transformer)
            cursorRenderer.draw(canvas, transformer)
            canvas.restoreToCount(saveId)
        }

        labelRenderer.draw(canvas, transformer)


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

    internal fun getStartScaleValue(): Int {
        val scrollPts = FSize.obtain(scrollX.toFloat(), scrollY.toFloat())
        transformer.invertPixelToValue(scrollPts)
        return if (isHorizontal) {
            scrollPts.x.roundToInt().coerceIn(axis.minValue, axis.maxValue)
        } else {
            scrollPts.y.roundToInt().coerceIn(axis.minValue, axis.maxValue)
        }.apply {
            scrollPts.recycle()
        }
    }


    private var scaleValueRangePerScreen: Int = 0

    fun getScaleValueRangePerScreen(): Int {
        return scaleValueRangePerScreen
    }

    val isHorizontal
        get() = origintation == HORIZONTAL


    private var mActivePointerId = INVALID_POINTER
    private var mVelocityTracker: VelocityTracker? = null
    private var mTouchState: Int = 0
    private var mLastTouchPoint: FSize? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        initVelocityTrackerIfNotExists()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mLastTouchPoint = FSize.obtain(event.x, event.y)

                initOrResetVelocityTracker()

                // disable parent scroll event
                requestDisallowInterceptTouchEvent(true)

                abortScrollAnimation()


                mActivePointerId = event.getPointerId(0)

                mVelocityTracker!!.addMovement(event)
            }

            MotionEvent.ACTION_MOVE -> {
                val touchPoint = mLastTouchPoint!!
                val velocityTracker = mVelocityTracker!!

                velocityTracker.addMovement(event)
                val activePointerIndex = event.findPointerIndex(mActivePointerId)
                if (activePointerIndex != -1) {
                    mTouchState = TOUCH_STATE_MOVING
                    if (isHorizontal) {
                        val deltaX = touchPoint.offsetX(event.getX(activePointerIndex))
                        if (overScrollByCompat(-deltaX.roundToInt(), 0)) {
                            velocityTracker.clear()
                        }
                    } else {
                        val deltaY = touchPoint.offsetY(event.getY(activePointerIndex))
                        scrollBy(0, deltaY.toInt())
                    }

                }
            }
            MotionEvent.ACTION_UP -> {
                val velocityTracker = mVelocityTracker!!
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                if (isHorizontal) {
                    val velocity = velocityTracker.getXVelocity(mActivePointerId)
                    if (Math.abs(velocity) > mMinimumVelocity) {
                        mTouchState = TOUCH_STATE_FLING
                        scroller.fling(
                            scrollX, scrollY,
                            -velocity.roundToInt(), 0,
                            minScrollPosition, maxScrollPosition,
                            0, 0,
                            MAX_OVER_SCROLL_EDGE, MAX_OVER_SCROLL_EDGE
                        )
                        ViewCompat.postInvalidateOnAnimation(this)
                    } else if (scroller.springBack(scrollX, scrollY, minScrollPosition, maxScrollPosition, 0, 0)) {
                        mTouchState = TOUCH_STATE_REST
                        ViewCompat.postInvalidateOnAnimation(this)
                    } else {
                        mTouchState = TOUCH_STATE_REST
                        scrollToNearByValue()
                    }
                } else {
                    val velocity = velocityTracker.getYVelocity(mActivePointerId)
                    if (Math.abs(velocity) > mMinimumVelocity) {
                        scroller.fling(
                            scrollX, scrollY,
                            0, -velocity.roundToInt(),
                            0, 0,
                            minScrollPosition, maxScrollPosition,
                            MAX_OVER_SCROLL_EDGE, MAX_OVER_SCROLL_EDGE
                        )
                        mTouchState = TOUCH_STATE_FLING
                    } else if (scroller.springBack(scrollX, scrollY, 0, 0, minScrollPosition, maxScrollPosition)) {
                        ViewCompat.postInvalidateOnAnimation(this)
                        mTouchState = TOUCH_STATE_REST
                    } else {
                        mTouchState = TOUCH_STATE_REST
                        scrollToNearByValue()
                    }
                }

                // release parent scroll event
                requestDisallowInterceptTouchEvent(false)

                recycleTouchPoint()

                mActivePointerId = INVALID_POINTER
                return false
            }
            MotionEvent.ACTION_CANCEL -> {

                // release parent scroll event
                requestDisallowInterceptTouchEvent(false)

                recycleTouchPoint()

                mTouchState = TOUCH_STATE_REST
                mActivePointerId = INVALID_POINTER
                return false
            }
        }

        return true
    }


    private var currentScaleValue: Int = 0

    fun getCurrentScaleValue(): Int {
        return currentScaleValue
    }

    fun setCurrentScaleValue(value: Int) {
        currentScaleValue = value.coerceIn(axis.minValue, axis.maxValue)
        scrollToValue()
    }

    private var firstLayout: Boolean = true
    private fun scrollToValue() {
        if (width == 0 || height == 0 || mTouchState != TOUCH_STATE_INIT) {
            return
        }

        var dx = minScrollPosition
        var dy = minScrollPosition
        if (currentScaleValue != axis.minValue) {
            val pts = transformer.generateValueToPixel(currentScaleValue)
            dx = (pts.x + minScrollPosition).roundToInt()
            dy = (pts.y + minScrollPosition).roundToInt()
            pts.recycle()
        }

        if (isHorizontal) {
            dy = 0
        } else {
            dx = 0
        }

        if (firstLayout) {
            firstLayout = false
            scrollTo(dx, dy)
        } else {
            scroller.startScroll(
                scrollX, scrollY,
                dx - scrollX,
                dy - scrollY
            )

            ViewCompat.postInvalidateOnAnimation(this)
        }
    }


    private fun scrollToNearByValue() {
        val canScrollHorizontal =
            overScrollMode != OVER_SCROLL_NEVER && computeHorizontalScrollRange() > computeHorizontalScrollExtent()
        val canScrollVertical =
            overScrollMode != OVER_SCROLL_NEVER && computeVerticalScrollRange() > computeVerticalScrollExtent()


        var dx = minScrollPosition
        var dy = minScrollPosition

        if (currentScaleValue != axis.minValue) {
            val pts = transformer.generateValueToPixel(currentScaleValue)
            dx = pts.x.roundToInt().coerceIn(minScrollPosition, maxScrollPosition) + minScrollPosition
            dy = pts.y.roundToInt().coerceIn(minScrollPosition, maxScrollPosition) + minScrollPosition
            pts.recycle()
        }


        val isDirty: Boolean = when {
            canScrollHorizontal -> {
                dy = 0
                dx != scrollX
            }
            canScrollVertical -> {
                dx = 0
                dy != scrollY
            }
            else -> false
        }

        if (isDirty) {
            scroller.startScroll(
                scrollX,
                scrollY,
                dx - scrollX,
                dy - scrollY
            )
            ViewCompat.postInvalidateOnAnimation(this)

        }
    }


    private fun recycleTouchPoint() {
        mLastTouchPoint?.recycle()
        mLastTouchPoint = null
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, scroller.currY)
            invalidate()
        } else if (mTouchState == TOUCH_STATE_REST || mTouchState == TOUCH_STATE_FLING) {
            mTouchState = TOUCH_STATE_INIT
            scrollToNearByValue()
        }
    }


    private fun abortScrollAnimation() {
        if (!scroller.isFinished) {
            scroller.abortAnimation()
        }
    }


    private fun overScrollByCompat(x: Int, y: Int): Boolean {
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

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)

        val pts = FSize.obtain(l - minScrollPosition, t - minScrollPosition)
        transformer.invertPixelToValue(pts)

        val value = if (isHorizontal) {
            pts.x.roundToInt()
        } else {
            pts.y.roundToInt()
        }

        pts.recycle()

        if (axis.minValue.rangeTo(axis.maxValue).contains(value)) {
            currentScaleValue = value
        }

    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        computeXAxisSize()
    }

    private fun computeXAxisSize() {
        viewPort.setDimens(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            measuredWidth.toFloat() - paddingRight,
            measuredHeight.toFloat() - paddingBottom
        )

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

        if (isHorizontal) {
            minScrollPosition = (minxScrollValuePts.x - width / 2).roundToInt()
            maxScrollPosition = (maxScrollValuePts.x + axis.scaleLineOptions.widthNeeded).roundToInt()
        } else {
            minScrollPosition = minxScrollValuePts.y.roundToInt() - height / 2
            maxScrollPosition = maxScrollValuePts.y.roundToInt() - height / 2
        }

        maxScrollValuePts.recycle()
        minxScrollValuePts.recycle()


        val screenPts = FSize.obtain(width.toFloat(), height.toFloat())
        transformer.invertPixelToValue(screenPts)

        scaleValueRangePerScreen = if (isHorizontal) {
            screenPts.x.roundToInt() - axis.minValue
        } else {
            screenPts.y.roundToInt() - axis.minValue
        }.apply {
            screenPts.recycle()
        }

        // reset value
        setCurrentScaleValue(currentScaleValue)
    }

    override fun computeHorizontalScrollRange(): Int {
        return if (isHorizontal) {
            maxScrollPosition - minScrollPosition
        } else {
            0
        }
    }

    override fun computeVerticalScrollRange(): Int {
        return if (isHorizontal) {
            0
        } else {
            maxScrollPosition - minScrollPosition
        }
    }

    private fun initOrResetVelocityTracker() {
        mVelocityTracker?.run {
            clear()
        } ?: run {
            mVelocityTracker = VelocityTracker.obtain()
        }
    }

    private fun initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
    }

    private fun recycleVelocityTracker() {
        mVelocityTracker?.recycle()
        mVelocityTracker = null
    }

    private fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        if (!disallowIntercept) {
            recycleVelocityTracker()
        }
        parent?.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

} 
