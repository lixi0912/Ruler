package com.lixicode.ruler.formatter

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/6
 */
interface ValueFormatter {


    fun formatValue(value: Float): String {
        return value.toString()
    }


    companion object {
        val DEFAULT = object : ValueFormatter {}
    }

}
