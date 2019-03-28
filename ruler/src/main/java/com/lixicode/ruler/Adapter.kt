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
package com.lixicode.ruler

import com.lixicode.ruler.formatter.ValueFormatter
import java.util.*
import kotlin.math.abs

/**
 * <数据适配器>
 * @author lixi
 * @date 2019/3/26
 *
 * @since 1.0-rc1
 */
open class Adapter {

    var formatter: ValueFormatter? = null

    /**
     * item 的个数
     */
    open var itemCount: Int = 0

    private val observable = object : Observable() {
        override fun notifyObservers(arg: Any?) {
            setChanged()
            super.notifyObservers(arg)
        }
    }

    /**
     * 无限模式下，传入的 position 为 [Int.MIN_VALUE]-[Int.MIN_VALUE]，所以需要格式为有效位置
     */
    internal fun getItemTitle(position: Int): String {
        val itemPosition = abs(position).rem(itemCount)
        return formatter?.formatItemLabel(itemPosition) ?: formatItemLabel(itemPosition)
    }


    /**
     * 格式化 itemLabel
     */
    open fun formatItemLabel(position: Int): String {
        return position.toString()
    }

    /**
     * add an [observer] to [observable]
     *
     * @param observer to be added
     * @since 1.0-rc2
     */
    fun registerObserver(observer: Observer?) {
        observable.addObserver(observer)
    }

    /**
     * remove [observer] from [observable]
     *
     * @param observer to be removed
     * @since 1.0-rc2
     */
    fun unRegisterObserver(observer: Observer?) {
        observable.deleteObserver(observer)
    }

    /**
     * notify all of its [Observer] update
     *
     * @since 1.0-rc2
     */
    fun notifyDataSetChange(any: Any? = null) {
        observable.notifyObservers(any)
    }

}
