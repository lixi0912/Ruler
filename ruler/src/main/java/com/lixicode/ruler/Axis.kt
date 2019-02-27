package com.lixicode.ruler

import com.lixicode.ruler.Constants.Companion.DIVIDER_COUNT
import com.lixicode.ruler.Constants.Companion.DIVIDER_SPACING
import com.lixicode.ruler.Constants.Companion.REPEAT
import com.lixicode.ruler.Constants.Companion.VISIBLE_RANGE_MAXIMUN
import com.lixicode.ruler.data.LineOptions
import com.lixicode.ruler.utils.Utils

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
abstract class Axis {


    /**
     * 最大可见刻度的个数
     */
    var visibleRangeMaximun: Int = VISIBLE_RANGE_MAXIMUN


    /**
     * 绘制 baseline 的参数
     */
    var baselineOptions: LineOptions? = null


    /**
     * 刻度分隔线
     */
    var dividerOptions: LineOptions? = null
    var dividerCount = DIVIDER_COUNT

    var dividerSpacing: Float = DIVIDER_SPACING.toFloat()
        get() = Utils.dpToPx(field.toInt())


    val visibleDividerCount
        get() = dividerCount * visibleRangeMaximun


    val visibleDividerSpacingCount
        get() = (dividerCount + 1) * visibleRangeMaximun


    /**
     * 刻度线的参数
     */
    lateinit var scaleLineOptions: LineOptions

    /**
     * 是否重复绘制
     */
    var repeat = REPEAT


}
