package com.lixicode.ruler.formatter

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/6
 */
interface ValueFormatter {


    @Deprecated("use [formatItemLabel] instead",replaceWith = ReplaceWith("formatItemLabel"))
    fun formatValue(value: Float): String {
        return value.toString()
    }


    /**
     * 格式化 item 的 label
     */
    fun formatItemLabel(position: Int): String {
        return formatValue(position.toFloat())
    }

    companion object {
        val DEFAULT = object : ValueFormatter {}
    }

}
