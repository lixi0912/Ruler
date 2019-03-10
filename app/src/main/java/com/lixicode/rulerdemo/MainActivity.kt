package com.lixicode.rulerdemo

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.formatter.ValueFormatter

import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    companion object {
        const val TEXT = "投我以木瓜，报之以琼琚。匪报也，永以为好也" +
                "投我以木桃，报之以琼瑶。匪报也，永以为好也！" +
                "投我以木李，报之以琼玖。匪报也，永以为好也！"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val rulerView = findViewById<RulerView>(R.id.ruler)

        rulerView.valueFormatter = object : ValueFormatter {
            override fun formatValue(value: Float): String {
                val intValue = value.roundToInt()
                val index = rulerView.tickIndex(intValue).div(5 /* step */)
                if (index < TEXT.length) {
                    return TEXT.substring(index, index + 1)
                }
                return intValue.toString()
            }
        }
        fab.setOnClickListener { view ->

            Snackbar.make(
                view,
                "current scale value: ${rulerView.tick}  scrollX: ${rulerView.scrollX}, scrollY: ${rulerView.scrollY}",
                Snackbar.LENGTH_LONG
            ).setAction("Action") {
                rulerView.tick++
            }.show()
        }
    }

}
