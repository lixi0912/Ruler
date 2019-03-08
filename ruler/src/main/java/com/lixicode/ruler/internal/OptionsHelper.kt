package com.lixicode.ruler.internal

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import com.lixicode.ruler.R
import com.lixicode.ruler.data.Options

/**
 * @author 陈晓辉
 * @description <>
 * @date 2019/3/7
 */

internal class OptionsHelper {
    companion object {


        inline fun <reified T : Drawable> applyAttributes(
            context: Context,
            resourceId: Int,
            options: Options<T>,
            bindAttributes: (resourceId: Int) -> Unit = { }
        ) {
            if (resourceId == -1) {
                return
            }

            val a = context.obtainStyledAttributes(resourceId, R.styleable.Options)
            val enable = a.getBoolean(R.styleable.Options_enable, options.enable)
            val inset = a.getBoolean(R.styleable.Options_inset, options.inset)
            val spacing = a.getDimensionPixelSize(R.styleable.Options_spacing, options.spacing)
            val weight = a.getFloat(R.styleable.Options_weight, options.weight)
            val drawable = a.getDrawable(R.styleable.Options_drawable)
            a.recycle()


            bindAttributes(resourceId)



            options.weight = weight
            options.enable = enable
            options.spacing = spacing
            options.inset = inset

            (drawable as? T)?.run {
                options.setDrawable(this)
            }

        }

    }


}
