package com.lixicode.ruler.data

import android.graphics.Paint
import androidx.annotation.FloatRange
import com.lixicode.ruler.Constants.Companion.COLOR_DEFAULT
import com.lixicode.ruler.Constants.Companion.LINE_SIZE
import com.lixicode.ruler.Constants.Companion.LINE_WIDTH
import com.lixicode.ruler.Constants.Companion.RATIO_DEFAULT
import com.lixicode.ruler.utils.Utils
import kotlin.math.max

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
open class LineOptions(

    var widthNeeded: Float = Utils.dpToPx(LINE_WIDTH),

    var color: Int = COLOR_DEFAULT,

    var ratioOfParent: Float = RATIO_DEFAULT,

    var size: Float = Utils.dpToPx(LINE_SIZE),

    var cap: Paint.Cap = Paint.Cap.ROUND,

    var enable: Boolean = true
) {

    companion object {
        val NONE = LineOptions(enable = false)
    }

}


fun LineOptions.offset(): Float {
    return if (isRoundCap()) {
        widthNeeded / 2
    } else {
        0F
    }
}

fun LineOptions.isRoundCap(): Boolean {
    return cap == Paint.Cap.ROUND
}

fun LineOptions.createPaint(): Paint {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = color
    // 去除本身线的 1px 的宽度
    paint.strokeWidth = max(0F, widthNeeded - 1)
    paint.strokeCap = cap
    return paint
}
