package com.lixicode.ruler.data

import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import com.lixicode.ruler.utils.RectFPool
import com.lixicode.ruler.utils.RectPool

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/27
 */


internal fun RectF.concat(matrix: Matrix): RectF {
    matrix.mapRect(this)
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
