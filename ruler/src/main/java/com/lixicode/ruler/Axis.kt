package com.lixicode.ruler

import com.lixicode.ruler.Constants.Companion.DIVIDER_COUNT
import com.lixicode.ruler.Constants.Companion.DIVIDER_SPACING
import com.lixicode.ruler.Constants.Companion.REPEAT
import com.lixicode.ruler.Constants.Companion.VISIBLE_RANGE_MAXIMUN
import com.lixicode.ruler.data.LabelOptions
import com.lixicode.ruler.data.LineOptions
import com.lixicode.ruler.utils.Utils
import com.lixicode.run.ui.view.RulerView

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
abstract class Axis {


    lateinit var labelOptions: LabelOptions


    var minValue: Int = 100
    var maxValue: Int = 200
    val range: Int
        get() = maxValue - minValue


    var scaleLineStep: Int = 10

    /**
     * 最小可见刻度的个数
     */
    var visibleRangeMinimum: Int = VISIBLE_RANGE_MAXIMUN


    /**
     * 绘制 baseline 的参数
     */
    var baselineOptions = LineOptions.NONE


    /**
     * 刻度分隔线
     */
    var dividerLineOptions = LineOptions.NONE

    var dividerLineCount = DIVIDER_COUNT

    var dividerLineSpacing: Float = Utils.dpToPx(DIVIDER_SPACING)

    val visibleDividerLineCount
        get() = dividerLineCount * visibleRangeMinimum


    val visibleDividerLineSpacingCount
        get() = (dividerLineCount + 1) * visibleRangeMinimum


    /**
     * 大刻度的间隔
     */
    val scaleLineSpacing: Float
        get() {
            return (dividerLineCount + 1) * dividerLineSpacing +
                    dividerLineCount * if (dividerLineOptions.enable) {
                dividerLineOptions.widthNeeded
            } else {
                0F
            }
        }

    /**
     * 刻度线的参数
     */
    lateinit var scaleLineOptions: LineOptions

    /**
     * 是否重复绘制
     */
    var repeat = REPEAT


}
