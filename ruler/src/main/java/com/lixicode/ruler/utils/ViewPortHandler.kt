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

    var width: Float = 0F
    var height: Float = 0F

    val contentLeft
        get() = contentRect.left
    val contentTop
        get() = contentRect.top
    val contentRight
        get() = contentRect.right
    val contentBottom
        get() = contentRect.bottom
    val contentWidth
        get() = contentRect.width()
    val contentHeight
        get() = contentRect.height()


    val offsetLeft
        get() = contentRect.left
    val offsetTop
        get() = contentRect.top
    val offsetRight
        get() = width - contentRect.right
    val offsetBottom
        get() = contentRect.bottom


    fun setDimens(width: Float, height: Float) {
        val offsetLeft = offsetLeft
        val offsetTop = offsetTop
        val offsetRight = offsetRight
        val offsetBottom = offsetBottom

        this.width = width
        this.height = height

        restrainViewPort(offsetLeft, offsetTop, offsetRight, offsetBottom)
    }

    fun restrainViewPort(
        offsetLeft: Float, offsetTop: Float,
        offsetRight: Float, offsetBottom: Float
    ) {
        contentRect.set(offsetLeft, offsetTop, width - offsetRight, height - offsetBottom)
    }
}
