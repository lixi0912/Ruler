package com.lixicode.ruler.utils

import androidx.core.util.Pools

open class SingletonPool<T>(
    maxPoolSize: Int = 3,
    val onCreateInstance: () -> T,
    val onPreRelease: (T) -> Unit,
    threadSafe: Boolean = false
) {
    private val sPool = if (threadSafe) {
        Pools.SynchronizedPool<T>(maxPoolSize)
    } else {
        Pools.SimplePool<T>(maxPoolSize)
    }

    fun obtain(): T {
        return sPool.acquire() ?: onCreateInstance()
    }

    fun release(value: T) {
        sPool.release(value)
    }

}
