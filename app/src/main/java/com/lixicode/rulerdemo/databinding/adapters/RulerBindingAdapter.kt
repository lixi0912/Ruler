package com.lixicode.rulerdemo.databinding.adapters

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.databinding.adapters.ListenerUtil
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.RulerView.OnTickChangedListener
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
            return view.getTick() + view.minimumOfTicks
        }

        @JvmStatic
        @BindingAdapter(value = ["tick", "tickAttrChanged"], requireAll = false)
        fun setTick(view: RulerView, value: Int, attrChanged: InverseBindingListener?) {

            attrChanged?.run {
                val oldListener =
                    ListenerUtil.getListener<RulerView.OnTickChangedListener?>(view, R.id.callbackListener)
                if (null == oldListener) {
                    val newListener = object : RulerView.OnTickChangedListener {

                        override fun onTickChanged(oldValue: Int, newValue: Int, label: String) {
                            if (oldValue != newValue) {
                                onChange()
                            }
                        }
                    }
                    ListenerUtil.trackListener(view, newListener, R.id.callbackListener)
                    view.addOnTickChangedListener(newListener)
                }
            }
            val tickValue = if (value >= view.minimumOfTicks) {
                value - view.minimumOfTicks
            } else {
                value
            }

            if (view.getTick() != tickValue) {
                view.setTick(tickValue, true)
            }
        }
    }
}
