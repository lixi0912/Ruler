package com.lixicode.ruler.data

import android.graphics.drawable.Drawable
import kotlin.math.roundToInt

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


    fun coerceWidthIn(width: Int) {


    }

}

fun Options<*>.setBounds(x: Float, y: Float) {
    setBounds(x.roundToInt(), y.roundToInt())
}

fun Options<*>.setBounds(x: Int, y: Int) {
    setBounds(x, y, x, y)
}


fun Options<*>.setBounds(left: Float, top: Float, right: Float, bottom: Float) {
    setBounds(left.roundToInt(), top.roundToInt(), right.roundToInt(), bottom.roundToInt())
}

fun Options<*>.setBounds(left: Int, top: Int, right: Int, bottom: Int) {
    getDrawable()?.apply {
        setBounds(left, top, right, bottom)
        if (inset) {
            val insetHorizontal: Int = -(intrinsicWidth shr 1)
            val insetVertical: Int = -(intrinsicHeight shr 1)


            bounds.inset(
                insetHorizontal,
                insetVertical
            )
        }
    }
}

inline fun Int.negativeIf(negativeIfNeed: () -> Boolean): Int {
    return if (negativeIfNeed()) {
        -this
    } else {
        this
    }

}
