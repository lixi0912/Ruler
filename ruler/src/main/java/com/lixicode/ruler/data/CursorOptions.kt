package com.lixicode.ruler.data

import android.graphics.Color
import android.graphics.drawable.Drawable
import com.lixicode.ruler.Constants
import com.lixicode.ruler.utils.Utils

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/6
 */
class CursorOptions(
    var drawable: Drawable?
) : LineOptions(color = Color.RED, widthNeeded = Utils.dpToPx(Constants.LINE_WIDTH * 2))
