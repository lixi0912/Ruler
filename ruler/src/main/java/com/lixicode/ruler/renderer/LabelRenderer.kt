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
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
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

    private var activeTextBounds: Rect? = null

    fun onPreDraw(view: RulerView) {
        if (helper.autoSizeMode != LabelHelper.ALWAYS) {
            val rectF = RectFPool.obtain()
            initTextBounds(view, rectF)
            this.rectF = rectF
        }

        if (helper.labelOptions.getDrawable()?.isStateful == true) {
            this.activeTextBounds = RectFPool.obtain()
                .also {
                    initTextBounds(view, it)
                }
                .concat(view.transformer.labelMatrix)
                .mapToRect()
                .also {
                    if (view.isHorizontal) {
                        it.left = view.scrollX + view.width.div(2)
                        it.right = it.left
                    } else {
                        it.bottom = view.scrollY + view.height.div(2)
                        it.top = it.bottom
                    }
                }.also {
                    findOptions(view)?.run {
                        it.expand(widthNeeded, heightNeeded)
                    } ?: kotlin.run {
                        it.expand(helper.labelOptions.widthNeeded, helper.labelOptions.heightNeeded)
                    }
                }
        }
    }

    private fun findOptions(view: RulerView): Options<Drawable>? {
        val helper = view.helper.tickHelper
        return helper.cursorOptions.takeIf { it.enable }
            ?: helper.tickOptions.takeIf { it.enable }
            ?: helper.dividerTickOptions.takeIf { it.enable }
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

        activeTextBounds?.release()
        activeTextBounds = null
    }


    fun onDrawHorizontal(view: RulerView, canvas: Canvas, tick: Int, remainderOfTick: Int) {
        drawText(
            view, canvas, tick, remainderOfTick,
            expandWidth = helper.labelOptions.widthNeeded
        ) {
            it.apply {
                offsetTo(tick.toFloat(), it.top)
            }
        }

    }

    fun onDrawVertical(view: RulerView, canvas: Canvas, tick: Int, remainderOfTick: Int) {
        drawText(
            view, canvas, tick, remainderOfTick,
            expandHeight = helper.labelOptions.heightNeeded
        ) {
            it.apply {
                offsetTo(it.left, tick.toFloat())
            }
        }
    }

    private fun drawText(
        view: RulerView, canvas: Canvas, tick: Int,
        remainderOfTick: Int,
        expandWidth: Int = 0,
        expandHeight: Int = 0,
        onApplyOffset: (RectF) -> RectF
    ) {
        if (!helper.labelOptions.enable || remainderOfTick != 0) {
            return
        }

        val textDrawable = helper.labelOptions.getDrawable()!!
        textDrawable.text = view.getAdapter().getItemTitle(tick)

        val textBounds: Rect = when {
            helper.autoSizeMode == LabelHelper.ALWAYS -> {
                helper.autoTextSize(view.viewPort, textDrawable.text)
                if (helper.shouldAutoTextSize(view.viewPort)) {
                    return
                }
                RectFPool.obtain()
                    .let { tempSrc ->
                        initTextBounds(view, tempSrc)
                        RectFPool.obtain()
                            .concat(view.transformer.labelMatrix, onApplyOffset(tempSrc))
                            .mapToRect()
                            .also {
                                tempSrc.release()
                            }
                    }

            }
            helper.shouldAutoTextSize(view.viewPort) -> return
            else -> RectFPool.obtain()
                .concat(view.transformer.labelMatrix, onApplyOffset(rectF!!))
                .mapToRect()
        }

        activeTextBounds?.run {
            textDrawable.state = if (Rect.intersects(textBounds, activeTextBounds)) {
                LabelHelper.stateHovered
            } else {
                LabelHelper.stateNone
            }
        }

        textBounds.expand(expandWidth, expandHeight)
            .also {
                textDrawable.bounds = it
            }.release()

        textDrawable.draw(canvas)
    }

}
