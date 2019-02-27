package com.lixicode.run.ui.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.lixicode.ruler.Axis
import com.lixicode.ruler.Constants.Companion.COLOR_DEFAULT
import com.lixicode.ruler.Constants.Companion.LABEL_TEXT_SIZE
import com.lixicode.ruler.XAxis
import com.lixicode.ruler.data.LineOptions
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

    init {
        Utils.init(context)
        val xAxis = XAxis()

        this.labelRenderer = LabelRenderer(xAxis, viewPort, COLOR_DEFAULT, Utils.spToPx(LABEL_TEXT_SIZE))

        xAxis.baselineOptions = LineOptions()
        xAxis.scaleLineOptions = LineOptions()
        xAxis.dividerOptions = LineOptions()

        this.axisRenderer = XAxisRenderer(xAxis, viewPort)
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


        viewPort.setDimens(measuredWidth.toFloat(), measuredHeight.toFloat())
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.run {
            axisRenderer.draw(this)
            if (axis.repeat) {
                val saveId = save()
                rotate(180F)
                translate(-width.toFloat(), -height.toFloat())
                axisRenderer.draw(this)
                restoreToCount(saveId)
            }

            labelRenderer.draw(this)
        }


    }

} 
