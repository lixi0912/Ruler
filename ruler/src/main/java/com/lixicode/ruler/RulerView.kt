package com.lixicode.run.ui.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.OverScroller
import com.lixicode.ruler.Axis
import com.lixicode.ruler.XAxis
import com.lixicode.ruler.data.*
import com.lixicode.ruler.renderer.LabelRenderer
import com.lixicode.ruler.renderer.Renderer
import com.lixicode.ruler.renderer.XAxisRenderer
import com.lixicode.ruler.utils.Transformer
import com.lixicode.ruler.utils.Utils
import com.lixicode.ruler.utils.ViewPortHandler
import kotlin.math.max

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
    }

    var axis: Axis
    var axisRenderer: Renderer
    var origintation = HORIZONTAL

    val viewPort = ViewPortHandler()
    val labelRenderer: Renderer

    val scroller: OverScroller = OverScroller(context)

    private val transformer: Transformer
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

        transformer.prepareMatrixValuePx(
            axis.minValue.toFloat(),
            axis.visibleDividerLineSpacingCount,
            0F, 3
        )
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val startTimeMillis = System.currentTimeMillis()

        canvas?.run {
            // 不显示屏幕之外的内容
            // TODO 临时注释
//            clipRect(0, 0, widthNeeded, height)

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


    var mLastTouchPoint: FSize? = null


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
                        scrollBy(offsetX.toInt(), 0)
                    } else {
                        val offsetY = lastPoint.offsetY(y)
                        scrollBy(0, offsetY.toInt())
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    recycleTouchPoint()

                    abortScrollAnimation()

                    // release parent scroll event
                    parent?.requestDisallowInterceptTouchEvent(false)
                }
            }
            true
        } ?: super.onTouchEvent(event)
    }

    private fun recycleTouchPoint() {
        mLastTouchPoint?.recycle()
        mLastTouchPoint = null
    }

    fun abortScrollAnimation() {
        if (!scroller.isFinished) {
            scroller.abortAnimation()
        }
    }


    /**
     * 滑动到最近的刻度线
     */
    private fun scrollToNearByScaleLine() {


    }


} 
