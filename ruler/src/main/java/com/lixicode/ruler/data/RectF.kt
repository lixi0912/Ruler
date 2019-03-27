package com.lixicode.ruler.data

import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.util.Pools

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/27
 */

internal class RectFPool {

    companion object {
        private val sPool = Pools.SimplePool<RectF>(6)

        fun obtain(): RectF {
            val rectF = sPool.acquire()
            return rectF ?: RectF()
        }


        fun recycle(rectF: RectF) {
            rectF.setEmpty()
            sPool.release(rectF)
        }

    }

}

internal fun RectF.concat(matrix: Matrix): RectF {
    matrix.mapRect(this)
    return this
}

internal fun RectF.set(width: Float, height: Float): RectF {
    set(width, height, width, height)
    return this
}

internal fun RectF.set(left: Int, top: Int, right: Int, bottom: Int): RectF {
    val temp = RectPool.obtain()
    temp.set(left, top, right, bottom)
    set(temp)
    temp.recycle()
    return this
}


internal fun RectF.mapToRect(): Rect {
    val out = RectPool.obtain()
    round(out)
    RectFPool.recycle(this)
    return out
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

internal fun RectF.recycle() {
    RectFPool.recycle(this)
}
