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
package com.lixicode.ruler.utils

import android.graphics.Rect
import android.graphics.RectF

/**
 * <>
 * @author lixi
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
