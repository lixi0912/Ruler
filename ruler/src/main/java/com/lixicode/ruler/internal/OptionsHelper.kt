/**
 * MIT License
 *
 * Copyright (c) 2019 lixi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.lixicode.ruler.internal

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import com.lixicode.ruler.R
import com.lixicode.ruler.data.Options

/**
 * @author lixi
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

            val a = context.obtainStyledAttributes(resourceId, R.styleable.RulerView_Options)
            val enable = a.getBoolean(R.styleable.RulerView_Options_ruler_enable, options.enable)
            val inset = a.getBoolean(R.styleable.RulerView_Options_ruler_inset, options.inset)
            val spacing = a.getDimensionPixelSize(R.styleable.RulerView_Options_ruler_spacing, options.spacing)
            val weight = a.getFloat(R.styleable.RulerView_Options_ruler_weight, options.weight)
            val drawable = a.getDrawable(R.styleable.RulerView_Options_ruler_drawable)
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
