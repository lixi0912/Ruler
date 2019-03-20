package com.lixicode.ruler.data

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

fun RectF.expand(w: Int, h: Int) {
    val dx: Float = w.div(-2F)
    val dy: Float = h.div(-2F)
    inset(dx, dy)
}

fun RectF.recycle() {
    RectFPool.recycle(this)
}
