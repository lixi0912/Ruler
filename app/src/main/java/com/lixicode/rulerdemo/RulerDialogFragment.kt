package com.lixicode.rulerdemo

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.fragment.app.DialogFragment
import com.lixicode.ruler.formatter.ValueFormatter
import com.lixicode.rulerdemo.databinding.DialogRulerBinding
import kotlin.math.roundToInt


class RulerDialogFragment : DialogFragment() {

    val tick: ObservableField<Int> = ObservableField(0)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


        val binding = DataBindingUtil.inflate<DialogRulerBinding>(
            LayoutInflater.from(context),
            R.layout.dialog_ruler,
            null,
            false
        )
        binding.tick = tick
        binding.btnDialogOk.setOnClickListener {
            dismiss()
        }

        binding.ruler.valueFormatter = object : ValueFormatter {
            override fun formatValue(value: Float): String {
                return value.roundToInt().toString()
            }
        }

        return AlertDialog.Builder(context!!)
            .setView(binding.root)
            .create()
    }


}
