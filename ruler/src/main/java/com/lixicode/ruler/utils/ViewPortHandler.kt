package com.lixicode.ruler.utils

import android.graphics.Rect
import android.graphics.RectF

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
class ViewPortHandler {

    private val contentRect by lazy {
        RectF()
    }

    private val offsetRect by lazy {
        RectF()
    }

    val contentLeft
        get() = contentRect.left + offsetRect.left
    val contentTop
        get() = contentRect.top + offsetRect.top
    val contentRight
        get() = contentRect.right - offsetRect.right
    val contentBottom
        get() = contentRect.bottom - offsetRect.bottom


    val viewLeft
        get() = contentRect.left
    val viewTop
        get() = contentRect.top
    val viewRight
        get() = contentRect.right
    val viewBottom
        get() = contentRect.bottom


    val contentWidth
        get() = contentRight - contentLeft
    val contentHeight
        get() = contentBottom - contentTop


    val width: Float
        get() = contentRect.width()

    val height: Float
        get() = contentRect.height()


    fun setDimens(rect: Rect) {
        contentRect.set(rect)
    }

    fun setOffset(rect: Rect) {
        offsetRect.set(rect)
    }

}
