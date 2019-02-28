package com.lixicode.ruler.utils

import android.graphics.RectF

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
class ViewPortHandler {

    val contentRect by lazy {
        RectF()
    }

    val offsetRect by lazy {
        RectF()
    }


    val offsetLeft
        get() = offsetRect.left
    val offsetTop
        get() = offsetRect.top
    val offsetRight
        get() = offsetRect.right
    val offsetBottom
        get() = offsetRect.bottom

    val contentLeft
        get() = contentRect.left + offsetLeft
    val contentTop
        get() = contentRect.top + offsetTop
    val contentRight
        get() = contentRect.right - offsetRight
    val contentBottom
        get() = contentRect.bottom - offsetBottom
    val contentWidth
        get() = contentRight - contentLeft
    val contentHeight
        get() = contentBottom - contentTop


    val width: Float
        get() = contentRect.width()

    val height: Float
        get() = contentRect.height()


    fun setDimens(left: Float, top: Float, right: Float, bottom: Float) {
        contentRect.set(left, top, right, bottom)
    }

    fun setOffset(left: Float, top: Float, right: Float, bottom: Float) {
        offsetRect.set(left, top, right, bottom)
    }

}
