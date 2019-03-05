package com.lixicode.ruler.renderer

import android.graphics.Canvas
import com.lixicode.ruler.data.FSize
import com.lixicode.ruler.utils.Transformer
import com.lixicode.ruler.RulerView

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
abstract class Renderer(val view: RulerView) {

    abstract fun computeSize(
        widthMeasureSpec: Int,
        minimunWidth: Float,
        heightMeasureSpec: Int,
        minimunHeight: Float
    ): FSize

    abstract fun draw(canvas: Canvas, transformer: Transformer)
}
