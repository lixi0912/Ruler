package com.lixicode.rulerdemo.databinding.adapters

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.databinding.adapters.ListenerUtil
import com.lixicode.ruler.RulerView
import com.lixicode.rulerdemo.R

/**
 * <>
 * @author lixi
 * @date 2019/3/4
 */
class RulerBindingAdapter {
    companion object {


        @JvmStatic
        @InverseBindingAdapter(attribute = "tick")
        fun getTick(view: RulerView): Int {
            return view.getTick()
        }

        @JvmStatic
        @BindingAdapter(value = ["tick", "tickAttrChanged"], requireAll = false)
        fun setTick(view: RulerView, value: Int, attrChanged: InverseBindingListener?) {
            if (view.getTick() == value) {
                return
            }
            view.setTick(value)
            attrChanged?.run {
                val oldListener =
                    ListenerUtil.getListener<RulerView.OnTickChangedListener?>(view, R.id.callbackListener)
                if (null == oldListener) {
                    val newListener = object : RulerView.OnTickChangedListener {
                        override fun onTickChanged(value: Float, label: String) {
                            onChange()
                        }
                    }
                    ListenerUtil.trackListener(view, newListener, R.id.callbackListener)
                    view.tickChangeListener = newListener
                }
            }
        }
    }
}
