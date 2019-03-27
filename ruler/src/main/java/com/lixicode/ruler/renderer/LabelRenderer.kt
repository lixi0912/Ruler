/**
 * MIT License
 *
 * Copyright (c) 2019 lixi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.lixicode.ruler.renderer

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.data.*
import com.lixicode.ruler.internal.LabelHelper
import com.lixicode.ruler.utils.RectFPool

/**
 * @author lixi
 * @description <>
 * @date 2019/3/20
 */
internal class LabelRenderer(private val helper: LabelHelper) {

    private var rectF: RectF? = null

    fun onPreDraw(view: RulerView) {
        if (helper.autoSizeMode != LabelHelper.ALWAYS) {
            val rectF = RectFPool.obtain()
            initTextBounds(view, rectF)
            this.rectF = rectF
        }
    }

    private fun initTextBounds(view: RulerView, rectF: RectF) {
        // 计算标题居中显示所需坐标起点
        val start = if (view.helper.gravityOfTick == RulerView.GRAVITY_START || view.helper.enableMirrorTick) {
            view.helper.weightOfTick
        } else {
            0F
        }

        if (view.isHorizontal) {
            rectF.set(0F, start, 0F, start + view.helper.weightOfLabel)
        } else {
            rectF.set(start, 0F, start + view.helper.weightOfLabel, 0F)
        }
    }


    fun finishDraw(view: RulerView) {
        if (helper.autoSizeMode == LabelHelper.ALWAYS) {
            helper.autoTextSize()
        } else {
            rectF?.release()
            this.rectF = null
        }
    }


    fun onDrawHorizontal(
        view: RulerView,
        canvas: Canvas,
        tick: Int,
        remainderOfTick: Int
    ) {
        if (!helper.labelOptions.enable || remainderOfTick != 0) {
            return
        }

        val textDrawable = helper.labelOptions.getDrawable()!!


        textDrawable.text = view.getAdapter().getItemTitle(tick)
        if (helper.autoSizeMode == LabelHelper.ALWAYS) {
            helper.autoTextSize(view.viewPort, textDrawable.text)
            if (helper.shouldAutoTextSize(view.viewPort)) {
                return
            }
            RectFPool.obtain()
                .also { src ->
                    initTextBounds(view, src)
                    mapBoundsToDrawable(
                        view.transformer.labelMatrix,
                        textDrawable,
                        src,
                        tick.toFloat(), src.top,
                        helper.labelOptions.widthNeeded
                    )
                    src.release()
                }

            textDrawable.draw(canvas)
        } else if (!helper.shouldAutoTextSize(view.viewPort)) {
            mapBoundsToDrawable(
                view.transformer.labelMatrix,
                textDrawable,
                rectF!!,
                tick.toFloat(), rectF!!.top,
                helper.labelOptions.widthNeeded
            )
            textDrawable.draw(canvas)
        }

    }

    fun onDrawVertical(
        view: RulerView,
        canvas: Canvas,
        tick: Int,
        remainderOfTick: Int
    ) {
        if (!helper.labelOptions.enable || remainderOfTick != 0) {
            return
        }

        val textDrawable = helper.labelOptions.getDrawable()!!
        textDrawable.text = view.getAdapter().getItemTitle(tick)

        if (helper.autoSizeMode == LabelHelper.ALWAYS) {
            helper.autoTextSize(view.viewPort, textDrawable.text)
            if (helper.shouldAutoTextSize(view.viewPort)) {
                return
            }
            RectFPool.obtain()
                .also { src ->
                    initTextBounds(view, src)
                    mapBoundsToDrawable(
                        view.transformer.labelMatrix, textDrawable, src,
                        src.left, tick.toFloat(),
                        expandHeight = helper.labelOptions.heightNeeded
                    )
                    src.release()
                }

            textDrawable.draw(canvas)
        } else if (!helper.shouldAutoTextSize(view.viewPort)) {
            mapBoundsToDrawable(
                view.transformer.labelMatrix, textDrawable, rectF!!,
                rectF!!.left, tick.toFloat(),
                expandHeight = helper.labelOptions.heightNeeded
            )
            textDrawable.draw(canvas)
        }
    }


    private fun mapBoundsToDrawable(
        matrix: Matrix,
        textDrawable: LabelHelper.TextDrawable,
        src: RectF,
        newLeft: Float,
        newTop: Float,
        expandWidth: Int = 0,
        expandHeight: Int = 0
    ) {
        val dest = RectFPool.obtain()
        src.offsetTo(newLeft, newTop)
        matrix.mapRect(dest, src)
        dest.expand(expandWidth, expandHeight)
        dest.round(textDrawable.bounds)
        dest.release()
    }


}
