package com.lixicode.ruler.data

import androidx.core.util.Pools

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/4
 */
class PointF {

    val array: FloatArray = FloatArray(2) {
        Float.MIN_VALUE
    }


    companion object {
        private val sPool = Pools.SimplePool<PointF>(5)


        fun obtain(x: Int, y: Int): PointF {
            return (sPool.acquire() ?: PointF()).apply {
                this.x = x.toFloat()
                this.y = y.toFloat()
            }
        }

        fun obtain(x: Float, y: Float): PointF {
            return (sPool.acquire() ?: PointF()).apply {
                this.x = x
                this.y = y
            }
        }
    }


    var x: Float
        get() = array[0]
        set(value) {
            array[0] = value
        }


    var y: Float
        get() = array[1]
        set(value) {
            array[1] = value
        }


    fun recycle() {
        array.fill(Float.MIN_VALUE)
        sPool.release(this)
    }


}

fun PointF.offsetX(newValue: Float): Float {
    return (newValue - x)
        .apply {
            x = newValue
        }
}

fun PointF.offsetY(newValue: Float): Float {
    return (newValue - y)
        .apply {
            y = newValue
        }
}