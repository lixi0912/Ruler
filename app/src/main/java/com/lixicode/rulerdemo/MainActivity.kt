package com.lixicode.rulerdemo

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.lixicode.ruler.Adapter
import com.lixicode.ruler.RulerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
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
        rulerView.setAdapter(object : Adapter() {

            // item count - 65
//            override val itemCount: Int
//                get() = TEXT.length

            // index -> 0 - 64
//            override fun formatItemLabel(index: Int): String {
//                return TEXT.substring(index, index + 1)
//            }

            // item count -
            override var itemCount: Int = TEXT.length.times(5)

            override fun formatItemLabel(position: Int): String {
                val index = position.div(5 /* step */)
                return TEXT.substring(index, index + 1)
            }


        })


        val valueChip = findViewById<Chip>(R.id.value)

        rulerView.addOnTickChangedListener(object : RulerView.OnTickChangedListener {
            override fun onTickChanged(oldValue: Int, newValue: Int, label: String) {
                val text = "newValue: $newValue, oldValue: $oldValue,label: $label"
                valueChip.text = text
                rulerView.requestLayout()
            }
        })
        val dashBaseLine = findViewById<Chip>(R.id.dash_base_line).also { chip ->
            chip.setOnCloseIconClickListener {
                rulerView.updateBaseLineOptions {
                    it.enable = !it.enable
                    // only invalid
                    false
                }
            }

            chip.setOnClickListener {
                rulerView.updateBaseLineOptions { options ->
                    if (it.isSelected) {
                        rulerView.setLayerType(View.LAYER_TYPE_NONE, null)

                        options.setDrawable(
                            ContextCompat.getDrawable(
                                this@MainActivity,
                                R.drawable.ruler_simple_baseline
                            )
                        )
                        chip.text = getString(R.string.solid_line)
                    } else {
                        if (chip.isChecked) {
                            options.setDrawable(
                                DottedLineDrawable()
                            )
                        } else {
                            if (rulerView.isHorizontal) {
                                options.setDrawable(
                                    ContextCompat.getDrawable(
                                        this@MainActivity,
                                        R.drawable.dotted_baseline
                                    )
                                )
                                chip.text = getString(R.string.dotted_line)
                            } else {
                                options.setDrawable(
                                    ContextCompat.getDrawable(
                                        this@MainActivity,
                                        R.drawable.rotate_dotted_baseline
                                    )
                                )
                                chip.text = getString(R.string.not_support_xml)
                            }


                            rulerView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                        }
                    }
                    it.isSelected = !it.isSelected

                    // request layout
                    true
                }

            }
        }


        findViewById<Chip>(R.id.orientation).also { chip ->
            chip.setOnClickListener {
                if (rulerView.orientation == RulerView.HORIZONTAL) {
                    rulerView.orientation = RulerView.VERTICAL
                    chip.text = getString(R.string.vertical)

                    if (dashBaseLine.isSelected) {
                        dashBaseLine.text = getString(R.string.not_support_xml)


                        rulerView.updateBaseLineOptions { options ->
                            options.setDrawable(
                                ContextCompat.getDrawable(
                                    this@MainActivity,
                                    R.drawable.rotate_dotted_baseline
                                )
                            )
                            true
                        }
                    }
                } else {
                    rulerView.orientation = RulerView.HORIZONTAL
                    chip.text = getString(R.string.horizontal)

                    if (dashBaseLine.isSelected) {
                        dashBaseLine.text = getString(R.string.dotted_line)

                        rulerView.updateBaseLineOptions { options ->
                            options.setDrawable(
                                ContextCompat.getDrawable(
                                    this@MainActivity,
                                    R.drawable.dotted_baseline
                                )
                            )
                            true
                        }
                    }
                }
            }
        }
        val gravityChip = findViewById<Chip>(R.id.gravity).also { chip ->
            chip.setOnClickListener {
                if (rulerView.enableMirrorTick) {
                    chip.text = getText(R.string.gravity_invalid)
                    return@setOnClickListener
                }

                it.isSelected = !it.isSelected
                if (it.isSelected) {
                    rulerView.gravityOfTick = RulerView.GRAVITY_END
                    chip.text = getText(R.string.gravity_end)
                } else {
                    rulerView.gravityOfTick = RulerView.GRAVITY_START
                    chip.text = getText(R.string.gravity_start)
                }

            }
        }

        findViewById<Chip>(R.id.mirror).also { chip ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                rulerView.enableMirrorTick = isChecked
                if (isChecked) {
                    gravityChip.text = getText(R.string.gravity_invalid)
                } else {
                    if (gravityChip.isSelected) {
                        gravityChip.text = getText(R.string.gravity_end)
                    } else {
                        gravityChip.text = getText(R.string.gravity_start)
                    }
                }
            }
        }

        findViewById<Chip>(R.id.infinite_button).also { chip ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                rulerView.infiniteMode = isChecked
                if (isChecked) {
                    chip.text = getText(R.string.infinite_mode)
                } else {
                    chip.text = getText(R.string.finite_mode)
                }
            }
        }
        val lpChip = findViewById<Chip>(R.id.ruler_lp)

        val progressWidth = findViewById<SeekBar>(R.id.progress_width)
        val progressHeight = findViewById<SeekBar>(R.id.progress_height)

        lpChip.text = updateText(progressWidth, progressHeight)

        progressWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lpChip.text = updateText(progressWidth, progressHeight)

                rulerView.layoutParams.width = dpToPx(progress)
                rulerView.requestLayout()
            }
        })


        progressHeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lpChip.text = updateText(progressWidth, progressHeight)

                rulerView.layoutParams.height = dpToPx(progress)
                rulerView.requestLayout()
            }
        })

        findViewById<View>(R.id.dialog_button)
            .setOnClickListener {
                RulerDialogFragment().show(supportFragmentManager, getString(R.string.dialog_demo))
            }

        findViewById<View>(R.id.wheel_button)
            .setOnClickListener {
                com.lixicode.rulerdemo.WheelDialogFragment()
                    .show(supportFragmentManager, getString(R.string.wheel_demo))
            }


        findViewById<View>(R.id.wheel2_button)
            .setOnClickListener {
                com.lixicode.rulerdemo.Wheel2DialogFragment()
                    .show(supportFragmentManager, getString(R.string.wheel_demo_single))
            }

    }

    private fun updateText(progressWidth: SeekBar, progressHeight: SeekBar): String {
        return "w: ${progressWidth.progress} dp, h: ${progressHeight.progress} dp"
    }


    fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).roundToInt()
    }

}
