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
package com.lixicode.ruler.data

import android.graphics.Rect
import android.graphics.drawable.Drawable

/**
 * <>
 * @author lixi
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
    var weight: Float = 0F,

    /**
     * 用于控制间隙，当文字宽度不满足于显示的时候
     */
    var spacing: Int = 0,

    var inset: Boolean = true,

    /**
     * 是否显示
     */
    var enable: Boolean = false,

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