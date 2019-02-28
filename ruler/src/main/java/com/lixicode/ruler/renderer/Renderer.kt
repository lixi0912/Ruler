package com.lixicode.ruler.renderer

import android.graphics.Canvas
import com.lixicode.ruler.data.RulerBuffer
import com.lixicode.ruler.utils.ViewPortHandler

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
abstract class Renderer(val viewPort: ViewPortHandler) {
    abstract fun computeMinimumWidth(widthMeasureSpec: Int, minimunWidth: Float): Float
    abstract fun computeMinimumHeight(heightMeasureSpec: Int, minimunHeight: Float): Float

    abstract fun draw(canvas: Canvas, buffer: RulerBuffer)
}
