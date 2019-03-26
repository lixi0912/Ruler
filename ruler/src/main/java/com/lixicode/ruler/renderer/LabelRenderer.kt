package com.lixicode.ruler.renderer

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.data.*
import com.lixicode.ruler.internal.LabelHelper

/**
 * @author 陈晓辉
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
            rectF?.recycle()
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
                    src.recycle()
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
                    src.recycle()
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
        dest.recycle()
    }


}
