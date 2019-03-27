package com.lixicode.ruler.utils

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */

/**
 * 确保数据是有限的
 */
fun Float.letFinite(): Float =
    if (isFinite()) {
        this
    } else {
        0F
    }
