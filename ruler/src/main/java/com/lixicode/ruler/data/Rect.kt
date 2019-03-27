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
 * @author lixi
 * @description <>
 * @date 2019/3/21
 */


fun Rect.inset(rect: Rect): Rect {
    this.left += rect.left
    this.top += rect.top
    this.right -= rect.right
    this.bottom -= rect.bottom
    return this
}


internal fun Rect.clone(): Rect {
    return RectPool.obtain().also {
        it.set(this)
    }
}


internal fun Rect.concat(matrix: Matrix): Rect {
    return mapToRectF().concat(matrix).mapToRect()
}


internal fun Rect.mapToRectF(): RectF {
    return RectFPool.obtain()
        .also {
            it.set(this)
        }.also {
            RectPool.release(this)
        }
}

internal fun Rect.release() {
    RectPool.release(this)
}


internal fun Rect.rangeHorizontal(): IntRange {
    return left..right
}

internal fun Rect.rangeVertical(): IntRange {
    return top..bottom
}

internal fun Rect.expand(dx: Int, dy: Int): Rect {
    inset(-dx, -dy)
    return this
}
