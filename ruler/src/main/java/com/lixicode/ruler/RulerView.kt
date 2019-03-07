package com.lixicode.ruler

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.widget.OverScroller
import com.lixicode.ruler.data.*
import com.lixicode.ruler.formatter.ValueFormatter
import com.lixicode.ruler.internal.RulerViewHelper
import com.lixicode.ruler.utils.Utils
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


        const val TOUCH_STATE_INIT = 0
        const val TOUCH_STATE_MOVING = 1
        const val TOUCH_STATE_FLING = 2
        const val TOUCH_STATE_REST = 3

    }

    internal val helper by lazy {
        RulerViewHelper(this)
    }


    private val scroller: OverScroller = OverScroller(context)


    var minScrollPosition: Int = 0
    var maxScrollPosition: Int = 0


    var valueFormatter: ValueFormatter = object : ValueFormatter {}


    init {
        Utils.init(context)
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

        helper.onSizeChanged(measuredWidth, measuredHeight)
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

        helper.onDraw(canvas)

        val endTimeMillis = System.currentTimeMillis()
        val usedTimeMillis = endTimeMillis - startTimeMillis

        Log.e(RulerView::class.java.simpleName, "cost $usedTimeMillis milliseconds on draw event")

    }


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
                    if (helper.isHorizontal) {
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
                velocityTracker.computeCurrentVelocity(1000, helper.maximumVelocity.toFloat())
                if (helper.isHorizontal) {
                    val velocity = velocityTracker.getXVelocity(mActivePointerId)
                    if (Math.abs(velocity) > helper.minimumVelocity) {
                        mTouchState = TOUCH_STATE_FLING
                        scroller.fling(
                            scrollX, scrollY,
                            -velocity.roundToInt(), 0,
                            minScrollPosition, maxScrollPosition,
                            0, 0,
                            MAX_OVER_SCROLL_EDGE, MAX_OVER_SCROLL_EDGE
                        )
                        helper.postInvalidateOnAnimation(this)
                    } else if (scroller.springBack(scrollX, scrollY, minScrollPosition, maxScrollPosition, 0, 0)) {
                        mTouchState = TOUCH_STATE_REST
                        helper.postInvalidateOnAnimation(this)
                    } else {
                        mTouchState = TOUCH_STATE_REST
                        scrollToNearByValue()
                    }
                } else {
                    val velocity = velocityTracker.getYVelocity(mActivePointerId)
                    if (Math.abs(velocity) > helper.minimumVelocity) {
                        scroller.fling(
                            scrollX, scrollY,
                            0, -velocity.roundToInt(),
                            0, 0,
                            minScrollPosition, maxScrollPosition,
                            MAX_OVER_SCROLL_EDGE, MAX_OVER_SCROLL_EDGE
                        )
                        mTouchState = TOUCH_STATE_FLING
                    } else if (scroller.springBack(scrollX, scrollY, 0, 0, minScrollPosition, maxScrollPosition)) {
                        helper.postInvalidateOnAnimation(this)
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


    private var currentTickValue: Int = 0

    fun getCurrentScaleValue(): Int {
        return currentTickValue
    }

    fun setTickValue(value: Int) {
        currentTickValue = helper.coerceInTicks(value)
        scrollToValue()
    }

    private var firstLayout: Boolean = true
    private fun scrollToValue() {
        if (width == 0 || height == 0 || mTouchState != TOUCH_STATE_INIT) {
            return
        }

        var dx = minScrollPosition
        var dy = minScrollPosition
        if (currentTickValue != helper.minimumOfTicks) {
            val pts = helper.transformer.generateValueToPixel(currentTickValue)
            dx = (pts.x + minScrollPosition).roundToInt()
            dy = (pts.y + minScrollPosition).roundToInt()
            pts.recycle()
        }

        if (helper.isHorizontal) {
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

            helper.postInvalidateOnAnimation(this)
        }
    }


    private fun scrollToNearByValue() {
        val canScrollHorizontal =
            overScrollMode != OVER_SCROLL_NEVER && computeHorizontalScrollRange() > computeHorizontalScrollExtent()
        val canScrollVertical =
            overScrollMode != OVER_SCROLL_NEVER && computeVerticalScrollRange() > computeVerticalScrollExtent()


        var dx = minScrollPosition
        var dy = minScrollPosition

        if (currentTickValue != helper.minimumOfTicks) {
            val pts = helper.transformer.generateValueToPixel(currentTickValue)
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
            helper.postInvalidateOnAnimation(this)

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
        val value = FSize.obtain(l - minScrollPosition, t - minScrollPosition)
            .also {
                helper.transformer.invertPixelToValue(it)
            }.let {
                try {
                    if (helper.isHorizontal) {
                        it.x.roundToInt()
                    } else {
                        it.y.roundToInt()
                    }
                } finally {
                    it.recycle()
                }
            }


        if (helper.minimumOfTicks.rangeTo(helper.maximumOfTicks).contains(value)) {
            currentTickValue = value
        }

    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        helper.onSizeChanged(w, h)
    }


    override fun computeHorizontalScrollRange(): Int {
        return if (helper.isHorizontal) {
            maxScrollPosition - minScrollPosition
        } else {
            0
        }
    }

    override fun computeVerticalScrollRange(): Int {
        return if (helper.isHorizontal) {
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
