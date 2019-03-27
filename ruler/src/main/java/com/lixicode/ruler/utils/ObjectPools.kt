package com.lixicode.ruler.utils

import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF


internal object RectPool : SingletonPool<Rect>(
    onCreateInstance = { Rect() },
    onPreRelease = { it.setEmpty() }
)

internal object RectFPool : SingletonPool<RectF>(6,
    onCreateInstance = { RectF() },
    onPreRelease = { it.setEmpty() }
)

internal object MatrixPool : SingletonPool<Matrix>(
    onCreateInstance = { Matrix() },
    onPreRelease = { it.reset() }
)
