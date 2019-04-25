package com.lixicode.rulerdemo

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.fragment.app.DialogFragment
import com.lixicode.ruler.formatter.ValueFormatter
import com.lixicode.rulerdemo.databinding.DialogRulerWheel2Binding


class Wheel2DialogFragment : DialogFragment() {

    val day: ObservableField<Int> = ObservableField(0)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DataBindingUtil.inflate<DialogRulerWheel2Binding>(
            LayoutInflater.from(context),
            R.layout.dialog_ruler_wheel2,
            null,
            false
        )
        day.set(binding.dateRuler.minimumOfTicks)

        binding.day = day

        binding.btnDialogOk.setOnClickListener {
            dismiss()
        }

        binding.dateRuler.getAdapter()!!.formatter = object : ValueFormatter {
            override fun formatItemLabel(position: Int): String {
                return position.plus(binding.dateRuler.minimumOfTicks).toString()
            }
        }

        return AlertDialog.Builder(context!!)
            .setView(binding.root)
            .create()
    }


}
