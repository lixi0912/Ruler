package com.lixicode.rulerdemo

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import com.lixicode.ruler.RulerView

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val rulerView = findViewById<RulerView>(R.id.ruler)

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
