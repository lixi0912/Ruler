package com.lixicode.ruler.data

import android.graphics.Rect
import android.graphics.drawable.Drawable

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/7
 */
open class Options<T : Drawable>(

    /**
     * 用于绘制刻度的 Drawable
     */
    private var drawable: T? = null,

    /**
     * 占据屏幕高度的权重, 刻度线权重 + label 权重 = 控件高度
     */
    var weight: Float = 1F,

    /**
     * 用于控制间隙，当文字宽度不满足于显示的时候
     */
    var spacing: Int = 0,

    var inset: Boolean = true,

    /**
     * 是否显示
     */
    var enable: Boolean = true,

    val updatable: Boolean = true
) {


    val widthNeeded: Int
        get() {
            if (!enable || null == drawable) {
                return 0
            }
            return drawable!!.intrinsicWidth
        }

    val heightNeeded: Int
        get() {
            if (!enable || null == drawable) {
                return 0
            }
            return drawable!!.intrinsicHeight
        }

    val visible: Boolean
        get() {
            return enable && null != drawable
        }

    fun setDrawable(drawable: T?) {
        if (updatable) {
            this.drawable = drawable
        }
    }

    fun getDrawable(): T? {
        return if (enable) {
            drawable
        } else {
            null
        }
    }

}

fun <T : Drawable> Options<T>.setBounds(
    rect: Rect,
    onExpanded: (Rect) -> Rect = { it },
    onInset: (Rect) -> Rect = { it }
): Options<T> {
    val bounds = if (inset) {
        val dx = widthNeeded shr 1
        val dy = heightNeeded shr 1
        if (rect.isEmpty) {
            rect.expand(dx, dy)
            onExpanded(rect)
        } else {
            rect.inset(dx, dy)
            onInset(rect)
        }
    } else {
        rect
    }

    getDrawable()?.bounds = bounds
    bounds.release()
    return this
}