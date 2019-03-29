package com.lixicode.rulerdemo

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.fragment.app.DialogFragment
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.RulerView.OnTickChangedListener
import com.lixicode.ruler.formatter.ValueFormatter
import com.lixicode.rulerdemo.databinding.DialogRulerBinding
import com.lixicode.rulerdemo.databinding.DialogRulerWheelBinding
import java.util.*
import kotlin.math.roundToInt


class WheelDialogFragment : DialogFragment() {

    val month: ObservableField<Int> = ObservableField(0)
    val day: ObservableField<Int> = ObservableField(0)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DataBindingUtil.inflate<DialogRulerWheelBinding>(
            LayoutInflater.from(context),
            R.layout.dialog_ruler_wheel,
            null,
            false
        )
        month.set(binding.monthRuler.minimumOfTicks)
        day.set(binding.dateRuler.minimumOfTicks)

        binding.month = month
        binding.day = day

        binding.btnDialogOk.setOnClickListener {
            dismiss()
        }

        binding.monthRuler.valueFormatter = object : ValueFormatter {
            override fun formatItemLabel(position: Int): String {
                return position.plus(binding.monthRuler.minimumOfTicks).toString()
            }
        }

        binding.dateRuler.valueFormatter = object : ValueFormatter {
            override fun formatItemLabel(position: Int): String {
                return position.plus(binding.dateRuler.minimumOfTicks).toString()
            }
        }


        val calendar = Calendar.getInstance()
            .also {
                it.set(Calendar.DAY_OF_MONTH, 1)
            }
        binding.monthRuler.addOnTickChangedListener(object : OnTickChangedListener {

            override fun onTickChanged(oldValue: Int, newValue: Int, label: String) {
                if (oldValue != newValue) {
                    binding.dateRuler.getAdapter()?.run {
                        val newItemCount = calendar.also {
                            it.set(Calendar.MONTH, newValue)
                        }.getActualMaximum(Calendar.DAY_OF_MONTH)

                        if (itemCount != newItemCount) {
                            itemCount = newItemCount
                            notifyDataSetChange()
                        }
                    }
                }
            }

        })

        return AlertDialog.Builder(context!!)
            .setView(binding.root)
            .create()
    }


}
