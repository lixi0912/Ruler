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

import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import com.lixicode.ruler.utils.RectFPool
import com.lixicode.ruler.utils.RectPool

/**
 * <>
 * @author lixi
 * @date 2019/3/27
 */

fun RectF.inset(rect: RectF): RectF {
    this.left += rect.left
    this.top += rect.top
    this.right -= rect.right
    this.bottom -= rect.bottom
    return this
}


internal fun RectF.clone(): RectF {
    return RectFPool.obtain().also {
        it.set(this)
    }
}

internal fun RectF.concat(matrix: Matrix, src: RectF = this): RectF {
    matrix.mapRect(this, src)
    return this
}

internal fun RectF.set(left: Int, top: Int, right: Int, bottom: Int): RectF {
    RectPool.obtain().also {
        it.set(left, top, right, bottom)
    }.also {
        set(it)
    }.release()
    return this
}


internal fun RectF.mapToRect(): Rect {
    return RectPool.obtain().also {
        round(it)
    }.also {
        release()
    }
}

internal fun RectF.expand(dx: Int, dy: Int): RectF {
    return expand(dx.toFloat(), dy.toFloat())
}

internal fun RectF.expand(dx: Float, dy: Float): RectF {
    inset(-dx, -dy)
    return this
}


internal fun RectF.coerceIn(other: RectF): RectF {
    left = left.coerceAtLeast(other.left)
    top = top.coerceAtLeast(other.top)
    right = right.coerceIn(other.left, other.right)
    bottom = bottom.coerceIn(other.top, other.bottom)
    return this
}

internal fun RectF.release() {
    RectFPool.release(this)
}
