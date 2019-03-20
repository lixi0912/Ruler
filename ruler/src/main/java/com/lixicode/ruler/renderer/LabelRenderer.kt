package com.lixicode.ruler.renderer

import android.graphics.Canvas
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
        val rectF = RectFPool.obtain()

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

        this.rectF = rectF
    }

    fun finishDraw() {
        rectF!!.recycle()
        this.rectF = null
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

        textDrawable.text = view.valueFormatter.formatValue(tick.toFloat())

        val src = rectF!!
        val dest = RectFPool.obtain()
        src.offsetTo(tick.toFloat(), src.top)
        view.transformer.labelMatrix.mapRect(dest, src)
        dest.expand(helper.labelOptions.widthNeeded, 0)
        dest.round(textDrawable.bounds)
        dest.recycle()

        textDrawable.draw(canvas)

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

        textDrawable.text = view.valueFormatter.formatValue(tick.toFloat())

        val src = rectF!!
        val dest = RectFPool.obtain()
        src.offsetTo(src.left, tick.toFloat())
        view.transformer.labelMatrix.mapRect(dest, src)
        dest.expand(0, helper.labelOptions.heightNeeded)
        dest.round(textDrawable.bounds)
        dest.recycle()

        textDrawable.draw(canvas)
    }


}
