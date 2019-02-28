package com.lixicode.run.ui.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.lixicode.ruler.Axis
import com.lixicode.ruler.BuildConfig
import com.lixicode.ruler.Constants.Companion.COLOR_DEFAULT
import com.lixicode.ruler.Constants.Companion.LABEL_TEXT_SIZE
import com.lixicode.ruler.XAxis
import com.lixicode.ruler.data.LabelOptions
import com.lixicode.ruler.data.LineOptions
import com.lixicode.ruler.data.RulerBuffer
import com.lixicode.ruler.renderer.LabelRenderer
import com.lixicode.ruler.renderer.Renderer
import com.lixicode.ruler.renderer.XAxisRenderer
import com.lixicode.ruler.utils.Utils
import com.lixicode.ruler.utils.ViewPortHandler
import kotlin.math.max

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
open class RulerView @JvmOverloads constructor(
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

    var buffer: RulerBuffer

    init {
        Utils.init(context)

        val xAxis = XAxis()
        xAxis.labelOptions = LabelOptions()
        xAxis.baselineOptions = LineOptions()
        xAxis.scaleLineOptions = LineOptions()
        xAxis.dividerLineOptions = LineOptions()


        this.labelRenderer = LabelRenderer(viewPort, xAxis)
        this.axisRenderer = XAxisRenderer(viewPort, xAxis)
        this.buffer = RulerBuffer(xAxis.visibleRangeMaximun)
        this.axis = xAxis
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {


        var computeWidth = labelRenderer.computeMinimumWidth(widthMeasureSpec, 0F)
        computeWidth = axisRenderer.computeMinimumWidth(widthMeasureSpec, computeWidth)


        var computeHeight = labelRenderer.computeMinimumHeight(widthMeasureSpec, 0F)
        computeHeight = axisRenderer.computeMinimumHeight(heightMeasureSpec, computeHeight)


        setMeasuredDimension(
            resolveSize(
                max(suggestedMinimumWidth, computeWidth.toInt())
                , widthMeasureSpec
            ), resolveSize(
                max(suggestedMinimumHeight, computeHeight.toInt())
                , heightMeasureSpec
            )
        )

        viewPort.setDimens(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            measuredWidth.toFloat() - paddingRight,
            measuredHeight.toFloat() - paddingBottom
        )

        buffer.feed(axis, viewPort)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.run {
            // 不显示屏幕之外的内容
            // TODO 临时注释
//            clipRect(0, 0, width, height)

            axisRenderer.draw(this, buffer)
            if (axis.repeat) {
                val saveId = save()
                scale(1F, -1F)
                translate(0F, -height.toFloat())
                axisRenderer.draw(this, buffer)
                restoreToCount(saveId)
            }

            labelRenderer.draw(this, buffer)
        }


    }

} 
