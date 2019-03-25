package com.lixicode.rulerdemo.databinding.adapters

import android.text.style.AbsoluteSizeSpan
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.databinding.BindingAdapter

class TextBinding {

    companion object {

        @JvmStatic
        @BindingAdapter("text", "unitText", "unitTextSize")
        fun setTextWithUnit(view: TextView, height: Any, unit: String?, unitTextSize: Int) {
            view.text = buildSpannedString {
                append(height.toString())
                inSpans(
                    AbsoluteSizeSpan(unitTextSize, true)
                ) {
                    unit?.run {
                        append(" ")
                        append(this)
                    }
                }
            }
        }

    }

}
