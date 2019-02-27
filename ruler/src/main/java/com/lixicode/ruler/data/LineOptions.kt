package com.lixicode.ruler.data

import com.lixicode.ruler.Constants.Companion.COLOR_DEFAULT
import com.lixicode.ruler.Constants.Companion.LINE_SIZE
import com.lixicode.ruler.Constants.Companion.LINE_WIDTH
import com.lixicode.ruler.Constants.Companion.RATIO_DEFAULT
import com.lixicode.ruler.utils.Utils

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
class LineOptions(

    var width: Float = Utils.dpToPx(LINE_WIDTH),

    var color: Int = COLOR_DEFAULT,

    var ratioOfParent: Float = RATIO_DEFAULT,

    var size: Float = Utils.dpToPx(LINE_SIZE),

    var enable: Boolean = true
)
