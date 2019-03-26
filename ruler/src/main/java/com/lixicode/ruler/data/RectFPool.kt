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


class RectFPool {

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

class RectPool {

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


fun Rect.concat(matrix: Matrix): Rect {
    val rectF = mapToRectF()
    matrix.mapRect(rectF)
    return rectF.mapToRect()
}

fun Rect.mapToRectF(): RectF {
    val out = RectFPool.obtain()
    out.set(this)
    RectPool.recycle(this)
    return out
}

fun Rect.recycle() {
    RectPool.recycle(this)
}

fun RectF.concat(matrix: Matrix): RectF {
    matrix.mapRect(this)
    return this
}

fun RectF.mapToRect(): Rect {
    val out = RectPool.obtain()
    round(out)
    RectFPool.recycle(this)
    return out
}

fun Rect.set(width: Int, height: Int): Rect {
    set(width, height, width, height)
    return this
}

fun RectF.set(width: Float, height: Float): RectF {
    set(width, height, width, height)
    return this
}

fun RectF.set(left: Int, top: Int, right: Int, bottom: Int): RectF {
    val temp = RectPool.obtain()
    temp.set(left, top, right, bottom)
    set(temp)
    temp.recycle()
    return this
}

fun Rect.rangeHorizontal(): IntRange {
    return left..right
}

fun Rect.rangeVertical(): IntRange {
    return top..bottom
}

fun Rect.expand(w: Int, h: Int) {
    val dx = w.div(-2)
    val dy = h.div(-2)
    inset(dx, dy)
}

fun RectF.expand(w: Int, h: Int) {
    inset(w.toFloat(), h.toFloat())
}

fun RectF.expand(w: Float, h: Float) {
    val dx = w.div(-2F)
    val dy = h.div(-2F)
    inset(dx, dy)
}


fun RectF.recycle() {
    RectFPool.recycle(this)
}
