package com.lixicode.ruler

import android.graphics.RectF
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import com.lixicode.ruler.data.*
import com.lixicode.ruler.internal.RulerViewHelper
import com.lixicode.ruler.utils.RectFPool
import com.lixicode.ruler.utils.RectPool
import kotlin.math.abs
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
    private var mLastTouchPoint: RectF? = null
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
            w.div(2).also {
                view.transformer.prepareScrollOffset(it.toFloat(), 0F)
            }
        } else {
            h.div(2).also {
                view.transformer.prepareScrollOffset(0f, it.toFloat())
            }
        }


        val adapter = view.getAdapter()
        minScrollPosition = generateScrollPx(adapter.minimumOfTicks)
        maxScrollPosition = generateScrollPx(adapter.maximumOfTicks)


        // cancel scroller
        if (!scroller.isFinished) {
            scroller.abortAnimation()
        }

        // reset scroll position
        view.scrollTo(0, 0)

        scrollTo(view.tick)
    }

    private fun generateScrollPx(poisition: Int): Int {
        return if (poisition == Int.MIN_VALUE || poisition == Int.MAX_VALUE) {
            poisition
        } else {
            RectPool.obtain()
                .also {
                    it.left = poisition
                    it.top = poisition
                    it.right = it.left
                    it.bottom = it.top
                }
                .concat(helper.transformer.mMatrixValueToPx)
                .let {
                    //  允许首项居中
                    if (helper.isHorizontal) {
                        it.left
                    } else {
                        it.top
                    }.apply {
                        it.release()
                    }.minus(scrollOffset)
                }
        }
    }


    fun scrollTo(tick: Int) {
        if (!scroller.isFinished) {
            return
        }

        val dx: Int
        val dy: Int

        RectPool.obtain()
            .also {
                it.set(tick, tick, tick, tick)
            }
            .concat(helper.transformer.mMatrixValueToPx)
            .also {
                it.inset(-view.scrollX, -view.scrollY)
                it.offset(-scrollOffset, -scrollOffset)
            }
            .also {
                dx = it.left.takeIf { helper.isHorizontal } ?: 0
                dy = it.top.takeIf { !helper.isHorizontal } ?: 0
                it.release()
            }

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
                mLastTouchPoint = RectFPool.obtain()
                    .also {
                        it.left = event.x
                        it.top = event.y
                    }

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
                    val xDiff = abs(event.getX(pointerIndex) - touchPoint.left)
                    val yDiff = abs(event.getY(pointerIndex) - touchPoint.top)

                    if (xDiff > touchSlop || yDiff > touchSlop) {
                        isBeingDragged = true
                        setScrollState(SCROLL_STATE_DRAGGING)

                        // disable parent scroll event
                        requestParentDisallowInterceptTouchEvent(true)
                    }
                }

                if (isBeingDragged) {
                    if (view.isHorizontal) {
                        val deltaX = event.getX(pointerIndex) - touchPoint.left
                        touchPoint.left = event.getX(pointerIndex)
                        if (overScrollByCompat(-deltaX.roundToInt(), 0)) {
                            recycleVelocityTracker()
                        }
                    } else {
                        val deltaY = event.getY(pointerIndex) - touchPoint.top
                        touchPoint.top = event.getY(pointerIndex)
                        if (overScrollByCompat(0, -deltaY.roundToInt())) {
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

        RectPool
            .obtain()
            .also {
                it.left = scroller.finalX
                it.top = scroller.finalY
                it.right = it.left
                it.bottom = it.top
            }
            .concat(helper.transformer.mMatrixScrollOffset)
            .concat(helper.transformer.mMatrixPxToValue)
            .concat(helper.transformer.mMatrixValueToPx)
            .also {
                it.offset(-scrollOffset, -scrollOffset)
                it.offset(-scroller.finalX, -scroller.finalY)
                if (helper.isHorizontal) {
                    deltaX = it.left
                    deltaY = 0
                } else {
                    deltaX = 0
                    deltaY = it.top
                }
                it.release()
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

        RectPool
            .obtain()
            .also {
                it.left = x.coerceIn(minScrollPosition, maxScrollPosition)
                it.top = y.coerceIn(minScrollPosition, maxScrollPosition)
                it.right = it.left
                it.bottom = it.top
            }
            .concat(helper.transformer.mMatrixScrollOffset)
            .concat(helper.transformer.mMatrixPxToValue)
            .also {
                tick = if (view.isHorizontal) {
                    it.left
                } else {
                    it.top
                }
                it.release()
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

        mLastTouchPoint?.release()
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
