package com.lixicode.ruler

import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import com.lixicode.ruler.data.PointF
import com.lixicode.ruler.data.offsetX
import com.lixicode.ruler.data.offsetY
import com.lixicode.ruler.internal.RulerViewHelper
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * @author 陈晓辉
 * @description <>
 * @date 2019/3/9
 */
internal class ScrollHelper(
    private val view: RulerView,
    private val helper: RulerViewHelper
) {

    companion object {

        const val SCROLL_STATE_IDLE = 0

        const val SCROLL_STATE_DRAGGING = 1

        const val SCROLL_STATE_SETTLING = 2
    }


    private val scroller = OverScroller(view.context)

    var scrollOffset: Int = 0

    var minScrollPosition: Int = 0
    var maxScrollPosition: Int = 0

    private val touchSlop: Int
    private val minimumVelocity: Float
    private val maximumVelocity: Float

    private var mActivePointerId = RulerView.INVALID_POINTER
    private var mVelocityTracker: VelocityTracker? = null
    private var mScrollState: Int = 0
    private var mLastTouchPoint: PointF? = null
    private var isBeingDragged: Boolean = false
    private var firstLayout: Boolean = false

    init {
        val configuration = ViewConfiguration.get(view.context)
        this.touchSlop = configuration.scaledTouchSlop
        this.minimumVelocity = configuration.scaledMinimumFlingVelocity.toFloat()
        this.maximumVelocity = configuration.scaledMaximumFlingVelocity.toFloat()
    }


    fun onSizeChanged(w: Int, h: Int) {
        firstLayout = true


        scrollOffset = if (helper.isHorizontal) {
            w.div(2)
        } else {
            h.div(2)
        }


        val adapter = view.getAdapter()
        minScrollPosition = if (adapter.minimumOfTicks == Int.MIN_VALUE) {
            Int.MIN_VALUE
        } else {
            generateScrollPx(adapter.minimumOfTicks).minus(scrollOffset)
        }


        maxScrollPosition = if (adapter.maximumOfTicks == Int.MAX_VALUE) {
            Int.MAX_VALUE
        } else {
            generateScrollPx(adapter.maximumOfTicks).minus(scrollOffset)
        }

        // cancel scroller
        if (!scroller.isFinished) {
            scroller.abortAnimation()
        }

        // reset scroll position
        view.scrollTo(0, 0)

        scrollTo(view.tick)
    }

    private fun generateScrollPx(poisition: Int): Int {
        return helper.generateValueToPixel(poisition)
            .let {
                //  允许首项居中
                if (helper.isHorizontal) {
                    it.x.roundToInt()
                } else {
                    it.y.roundToInt()
                }.apply {
                    it.recycle()
                }
            }
    }


    fun scrollTo(tick: Int) {
        if (!scroller.isFinished) {
            return
        }

        val pts = helper.generateValueToPixel(tick)
        val dx = pts.x.minus(scrollOffset).minus(view.scrollX).roundToInt().takeIf { helper.isHorizontal } ?: 0
        val dy = pts.y.minus(scrollOffset).minus(view.scrollY).roundToInt().takeIf { !helper.isHorizontal } ?: 0
        pts.recycle()

        if (dx == 0 && dy == 0) {
            setScrollState(SCROLL_STATE_IDLE)
            return
        }

        if (firstLayout) {
            firstLayout = false
            view.scrollBy(dx, dy)

            view.tickChangeListener?.onTickChanged(tick.toFloat(), view.getAdapter().getItemTitle(tick))
        } else {
            scroller.startScroll(
                view.scrollX, view.scrollY,
                dx, dy
            )
            ViewCompat.postInvalidateOnAnimation(view)
        }
    }

    fun computeScroll() {
        if (scroller.computeScrollOffset()) {

            if (view.scrollX != scroller.currX
                || view.scrollY != scroller.currY
            ) {
                view.scrollTo(scroller.currX, scroller.currY)
            }

            ViewCompat.postInvalidateOnAnimation(view)
        }
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        initVelocityTrackerIfNotExists()
        mVelocityTracker!!.addMovement(event)

        when (event.action.and(MotionEvent.ACTION_MASK)) {
            MotionEvent.ACTION_DOWN -> {
                mLastTouchPoint = PointF.obtain(event.x, event.y)

                abortScrollAnimation()

                mActivePointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(mActivePointerId)
                if (pointerIndex == -1) {
                    if (resetTouch()) {
                        ViewCompat.postInvalidateOnAnimation(view)
                    }
                    return true
                }

                val touchPoint = mLastTouchPoint!!
                if (!isBeingDragged) {
                    val xDiff = abs(event.getX(pointerIndex) - touchPoint.x)
                    val yDiff = abs(event.getY(pointerIndex) - touchPoint.y)

                    if (xDiff > touchSlop || yDiff > touchSlop) {
                        isBeingDragged = true
                        setScrollState(SCROLL_STATE_DRAGGING)

                        // disable parent scroll event
                        requestParentDisallowInterceptTouchEvent(true)
                    }
                }

                if (isBeingDragged) {
                    if (view.isHorizontal) {
                        val deltaX = touchPoint.offsetX(event.getX(pointerIndex)).roundToInt()
                        if (overScrollByCompat(-deltaX, 0)) {
                            recycleVelocityTracker()
                        }
                    } else {
                        val deltaY = touchPoint.offsetY(event.getY(pointerIndex)).roundToInt()
                        if (overScrollByCompat(0, -deltaY)) {
                            recycleVelocityTracker()
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isBeingDragged) {
                    val velocityTracker = mVelocityTracker!!
                    velocityTracker.computeCurrentVelocity(1000, maximumVelocity)

                    val velocityX: Int
                    val velocityY: Int
                    if (helper.isHorizontal) {
                        velocityX = velocityTracker.getXVelocity(mActivePointerId).roundToInt()
                        velocityY = 0
                    } else {
                        velocityX = 0
                        velocityY = velocityTracker.getYVelocity(mActivePointerId).roundToInt()
                    }

                    when {
                        abs(velocityX) > minimumVelocity || abs(velocityY) > minimumVelocity -> {
                            smoothScrollTo(view.scrollX, view.scrollY, -velocityX, -velocityY)
                            updateTickFromScrollPosition(scroller.finalX, scroller.finalY)
                            ViewCompat.postInvalidateOnAnimation(view)
                        }
                        scroller.springBack(
                            view.scrollX, view.scrollY,
                            minScrollPosition, maxScrollPosition,
                            minScrollPosition, maxScrollPosition
                        ) -> {
                            updateTickFromScrollPosition(scroller.finalX, scroller.finalY)
                            ViewCompat.postInvalidateOnAnimation(view)
                        }
                        else -> updateTickFromScrollPosition(view.scrollX, view.scrollY)
                    }

                    // release parent scroll event
                    requestParentDisallowInterceptTouchEvent(false)

                    resetTouch()

                }
            }
            MotionEvent.ACTION_CANCEL -> {
                if (isBeingDragged) {
                    updateTickFromScrollPosition(view.scrollX, view.scrollY)

                    // release parent scroll event
                    requestParentDisallowInterceptTouchEvent(false)

                    resetTouch()
                }
            }
        }

        return true
    }

    private fun smoothScrollTo(scrollX: Int, scrollY: Int, velocityX: Int, velocityY: Int) {
        scroller.fling(
            scrollX, scrollY,
            velocityX, velocityY,
            minScrollPosition, maxScrollPosition,
            minScrollPosition, maxScrollPosition,
            RulerView.MAX_OVER_SCROLL_EDGE, RulerView.MAX_OVER_SCROLL_EDGE
        )

        if (scroller.finalX >= maxScrollPosition || scroller.finalX <= minScrollPosition
            || scroller.finalY >= maxScrollPosition || scroller.finalY <= minScrollPosition
        ) {
            return
        }


        val deltaX: Int
        val deltaY: Int

        helper.invertPixelToValue(
            scroller.finalX + scrollOffset,
            scroller.finalY + scrollOffset
        ).also {
            it.x = round(it.x)
            it.y = round(it.y)
            helper.transformer.pointValuesToPixel(it)
        }.also {
            if (helper.isHorizontal) {
                deltaX = it.x.roundToInt() - scrollOffset - scroller.finalX
                deltaY = 0
            } else {
                deltaX = 0
                deltaY = it.y.roundToInt() - scrollOffset - scroller.finalY
            }
            it.recycle()
        }

        if (deltaX != 0 || deltaY != 0) {
            scroller.abortAnimation()

            scroller.fling(
                scrollX + deltaX, scrollY + deltaY,
                velocityX, velocityY,
                minScrollPosition, maxScrollPosition,
                minScrollPosition, maxScrollPosition,
                RulerView.MAX_OVER_SCROLL_EDGE, RulerView.MAX_OVER_SCROLL_EDGE
            )
        }


    }

    private fun updateTickFromScrollPosition(x: Int, y: Int) {
        val tick: Int

        helper.invertPixelToValue(
            x.coerceIn(minScrollPosition, maxScrollPosition).plus(scrollOffset),
            y.coerceIn(minScrollPosition, maxScrollPosition).plus(scrollOffset)
        ).also {
            tick = if (view.isHorizontal) {
                it.x
            } else {
                it.y
            }.roundToInt()
        }.also {
            it.recycle()
        }

        view.tick = tick

        setScrollState(SCROLL_STATE_IDLE)

        view.tickChangeListener?.onTickChanged(tick.toFloat(), view.getAdapter().getItemTitle(tick))
    }

    private fun setScrollState(state: Int) {
        mScrollState = state
    }

    private fun overScrollByCompat(x: Int, y: Int): Boolean {
        val overScrollMode = view.overScrollMode
        val canScrollHorizontal = overScrollMode != View.OVER_SCROLL_NEVER && view.canScrollHorizontally(x)
        val canScrollVertical = overScrollMode != View.OVER_SCROLL_NEVER && view.canScrollVertically(y)
        val overScrollHorizontal =
            overScrollMode == View.OVER_SCROLL_ALWAYS || overScrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollHorizontal
        val overScrollVertical =
            overScrollMode == View.OVER_SCROLL_ALWAYS || overScrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollVertical


        var newScrollX = view.scrollX + x
        val maxOverScrollX = if (!overScrollHorizontal) {
            0
        } else {
            RulerView.MAX_OVER_SCROLL_EDGE
        }

        var newScrollY = view.scrollY + y
        val maxOverScrollY = if (!overScrollVertical) {
            0
        } else {
            RulerView.MAX_OVER_SCROLL_EDGE
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

        view.scrollTo(newScrollX, newScrollY)

        return clampedX || clampedY
    }


    private fun resetTouch(): Boolean {
        isBeingDragged = false
        mActivePointerId = RulerView.INVALID_POINTER

        mLastTouchPoint?.recycle()
        mLastTouchPoint = null

        recycleVelocityTracker()
        return false
    }

    private fun abortScrollAnimation() {
        if (!scroller.isFinished) {
            scroller.abortAnimation()
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


    fun onAttachedToWindow() {
        firstLayout = true
    }

    fun onDetachedFromWindow() {
        scroller.abortAnimation()
    }

    private fun requestParentDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        if (!disallowIntercept) {
            recycleVelocityTracker()
        }
        view.parent?.requestDisallowInterceptTouchEvent(disallowIntercept)
    }


}
