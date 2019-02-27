package com.lixicode.ruler.utils

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue

/**
 * <>
 * @author 陈晓辉
 * @date 2019/2/27
 */
class Utils {


    companion object {
        private lateinit var metrics: DisplayMetrics


        public fun init(context: Context) {
            metrics = context.resources.displayMetrics
        }

        fun dpToPx(dip: Int): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip.toFloat(), metrics
            )
        }

        fun spToPx(sp: Int): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp.toFloat(), metrics
            )
        }

    }

}
