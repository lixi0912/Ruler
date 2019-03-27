package com.lixicode.ruler.data

import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.util.Pools

/**
 * @author 陈晓辉
 * @description <>
 * @date 2019/3/21
 */

internal class RectPool {

    companion object {
        private val sPool = Pools.SimplePool<Rect>(2)

        fun obtain(): Rect {
            val rectF = sPool.acquire()
            return rectF ?: Rect()
        }


        fun recycle(rectF: Rect) {
            rectF.setEmpty()
            sPool.release(rectF)
        }
    }
}


internal fun Rect.concat(matrix: Matrix): Rect {
    val rectF = mapToRectF()
    matrix.mapRect(rectF)
    return rectF.mapToRect()
}

internal fun Rect.mapToRectF(): RectF {
    val out = RectFPool.obtain()
    out.set(this)
    RectPool.recycle(this)
    return out
}

internal fun Rect.recycle() {
    RectPool.recycle(this)
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


internal fun Rect.coerceIn(other: Rect): Rect {
    left = left.coerceAtLeast(other.left)
    top = top.coerceAtLeast(other.top)
    right = right.coerceIn(other.left, other.right)
    bottom = bottom.coerceIn(other.top, other.bottom)
    return this
}


internal fun Rect.set(width: Int, height: Int): Rect {
    set(width, height, width, height)
    return this
}
