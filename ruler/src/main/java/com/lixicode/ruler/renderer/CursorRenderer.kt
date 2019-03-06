package com.lixicode.ruler.renderer

import android.graphics.Canvas
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.data.CursorOptions
import com.lixicode.ruler.data.FSize
import com.lixicode.ruler.data.createPaint
import com.lixicode.ruler.utils.Transformer

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/6
 */
class CursorRenderer(
    view: RulerView,
    private val cursorOptions: CursorOptions
) : Renderer(view) {

    val paint by lazy {
        cursorOptions.createPaint()
    }

    override fun computeSize(
        widthMeasureSpec: Int,
        minimunWidth: Float,
        heightMeasureSpec: Int,
        minimunHeight: Float
    ): FSize {
        return FSize.obtain(0F, 0F)
    }

    override fun draw(canvas: Canvas, transformer: Transformer) {
        if (!cursorOptions.enable) {
            return
        }

        val x = view.scrollX + view.width / 2

        val pts = FSize.obtain(view.getCurrentScaleValue(), 1)
        transformer.pointValuesToPixel(pts)
        pts.x = x.toFloat()

        cursorOptions.drawable?.run {
            // TODO
        } ?: kotlin.run {
            canvas.drawLine(
                pts.x,
                view.viewPort.contentTop,
                pts.x,
                pts.y,
                paint
            )
        }
        pts.recycle()

    }


}
