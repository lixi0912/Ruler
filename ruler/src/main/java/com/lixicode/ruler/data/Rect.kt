package com.lixicode.ruler.data

import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import com.lixicode.ruler.utils.RectFPool
import com.lixicode.ruler.utils.RectPool

/**
 * @author 陈晓辉
 * @description <>
 * @date 2019/3/21
 */


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
