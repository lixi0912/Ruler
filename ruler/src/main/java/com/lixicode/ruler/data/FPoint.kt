package com.lixicode.ruler.data

import androidx.core.util.Pools

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/4
 */
class FPoint(var x: Float = Float.MIN_VALUE, var y: Float = Float.MIN_VALUE) {


    companion object {
        private val sPool = Pools.SynchronizedPool<FPoint>(2)


        fun obtain(x: Float, y: Float): FPoint {
            return (sPool.acquire() ?: FPoint()).apply {
                this.x = x
                this.y = y
            }
        }
    }


    fun offsetX(dx: Float): Float {
        val result = x - dx
        y = dx
        return result
    }

    fun offsetY(dy: Float): Float {
        val result = y - dy
        y = dy
        return result
    }


    fun recycle() {
        this.x = Float.MIN_VALUE
        this.y = Float.MIN_VALUE
        sPool.release(this)
    }


}
