package com.lixicode.ruler.data

import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import com.lixicode.ruler.Constants
import com.lixicode.ruler.utils.Utils

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/28
 */
class LabelOptions(

    var color: Int = Constants.COLOR_DEFAULT,

    var size: Float = Utils.spToPx(Constants.LABEL_TEXT_SIZE),

    var textMode: Int = RIGHT,
    val measuredText: String = "10",

    val textSizeFiducial: Float = Utils.spToPx(1)

) {
    companion object {
        const val LEFT = -1
        const val CENTER = 0
        const val RIGHT = 1
    }

    val paint by lazy {
        createPaint()
    }

}


private val LabelOptions.textRect by lazy {
    Rect()
}

fun LabelOptions.createPaint(): Paint {
    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    paint.color = color
    // 去除本身线的 1px 的宽度
    paint.textSize = size
    return paint
}

fun LabelOptions.calcTextHeight(): Int {
    val r = textRect
    r.set(0, 0, 0, 0)
    paint.getTextBounds(measuredText, 0, measuredText.length, r)
    return r.height()
}

fun LabelOptions.resizeTextSize(specWidth: Float) {
    val specTextWidth = paint.measureText(measuredText)
    if (specWidth < specTextWidth) {
        paint.textSize -= textSizeFiducial
        resizeTextSize( specWidth)
    }
}
