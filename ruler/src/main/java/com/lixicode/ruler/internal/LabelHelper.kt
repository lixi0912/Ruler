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
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import androidx.core.graphics.drawable.TintAwareDrawable
import com.lixicode.ruler.R
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.data.Options
import com.lixicode.ruler.utils.ViewPortHandler
import kotlin.math.roundToInt

/**
 * <>
 * @author lixi
 * @date 2019/3/7
 */
internal class LabelHelper(val view: RulerView) {

    companion object {
        private const val SANS = 1
        private const val SERIF = 2
        private const val MONOSPACE = 3


        const val NEVER = 0
        const val LONGEST_TEXT = 1
        const val ALWAYS = 2


        val stateHovered = intArrayOf(android.R.attr.state_hovered)
        val stateNone = intArrayOf()
    }

    inner class Attributes(
        var textSize: Float = 0F,
        var textColor: ColorStateList = ColorStateList.valueOf(Color.LTGRAY),
        var textAlign: Paint.Align = Paint.Align.CENTER,
        var typeface: Typeface = Typeface.DEFAULT,
        var sameLengthOfLabel: Boolean = false,
        var longestLabel: String? = null,
        var autoSizeMode: Int = NEVER,
        var autoSizeMinTextSize: Float,
        var autoSizeMaxTextSize: Float,
        var autoSizeStepGranularity: Int = 10
    ) {


        fun readAttributes(a: TypedArray) {

            if (a.hasValue(R.styleable.RulerView_android_textColor)) {
                textColor = a.getColorStateList(R.styleable.RulerView_android_textColor) ?: textColor
            }

            if (a.hasValue(R.styleable.RulerView_android_textSize)) {
                textSize = a.getDimensionPixelSize(R.styleable.RulerView_android_textSize, -1).toFloat()
            }

            if (a.hasValue(R.styleable.RulerView_ruler_textAlign)) {
                textAlign = Paint.Align.values()[a.getInt(R.styleable.RulerView_ruler_textAlign, textAlign.ordinal)]
            }

            if (a.hasValue(R.styleable.RulerView_ruler_sameLengthOfLabel)) {
                sameLengthOfLabel = a.getBoolean(R.styleable.RulerView_ruler_sameLengthOfLabel, sameLengthOfLabel)
            }

            if (a.hasValue(R.styleable.RulerView_ruler_longestLabel)) {
                longestLabel = a.getString(R.styleable.RulerView_ruler_longestLabel) ?: longestLabel
            }

            if (a.hasValue(R.styleable.RulerView_ruler_autoSize)) {
                autoSizeMode = a.getInt(R.styleable.RulerView_ruler_autoSize, autoSizeMode)
            }

            if (a.hasValue(R.styleable.RulerView_ruler_autoSizeMinTextSize)) {
                autoSizeMinTextSize =
                    a.getDimensionPixelSize(
                        R.styleable.RulerView_ruler_autoSizeMinTextSize,
                        autoSizeMinTextSize.roundToInt()
                    ).toFloat()
            }
            if (a.hasValue(R.styleable.RulerView_ruler_autoSizeMaxTextSize)) {
                autoSizeMaxTextSize =
                    a.getDimensionPixelSize(R.styleable.RulerView_ruler_autoSizeMaxTextSize, textSize.roundToInt())
                        .toFloat()
            }
            if (a.hasValue(R.styleable.RulerView_ruler_autoSizeStepGranularity)) {
                autoSizeStepGranularity =
                    a.getDimensionPixelSize(
                        R.styleable.RulerView_ruler_autoSizeStepGranularity,
                        autoSizeStepGranularity
                    )
            }

            readTypefaceAndStyle(a)
        }

        private fun readTypefaceAndStyle(a: TypedArray) {
            val textStyle = a.getInt(R.styleable.RulerView_android_textStyle, Typeface.NORMAL)
            if (a.hasValue(R.styleable.RulerView_android_fontFamily)) {
                // Try with String. This is done by TextView JB+, but fails in ICS
                val fontFamilyName = a.getString(R.styleable.RulerView_android_fontFamily)
                if (fontFamilyName != null) {
                    this.typeface = Typeface.create(fontFamilyName, textStyle)
                    return
                }
            }
            if (a.hasValue(R.styleable.RulerView_android_typeface)) {
                val typeface = when (a.getInt(
                    R.styleable.RulerView_android_typeface,
                    SANS
                )) {
                    SANS -> Typeface.SANS_SERIF
                    SERIF -> Typeface.SERIF
                    MONOSPACE -> Typeface.MONOSPACE
                    else -> Typeface.DEFAULT
                }
                this.typeface = Typeface.create(typeface, textStyle)
            } else if (textStyle != Typeface.NORMAL) {
                this.typeface = Typeface.defaultFromStyle(textStyle)
            }
        }

    }

    internal var autoSizeMode: Int = NEVER
    private var autoSizeMinTextSize: Float = Float.MAX_VALUE
    private var autoSizeMaxTextSize: Float = Float.MIN_VALUE
    private var autoSizeStepGranularity: Int = 1
    private var sameLengthOfLabel: Boolean = false
    private var longestLabel: String? = null


    val labelOptions = Options(TextDrawable(), updatable = false)

    fun loadFromAttributes(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) {

        val defaultTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            14F, context.resources.displayMetrics
        )
        val attributes = Attributes(
            textSize = defaultTextSize,
            autoSizeMinTextSize = defaultTextSize,
            autoSizeMaxTextSize = defaultTextSize
        )

        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.RulerView, defStyleAttr, defStyleRes
        )
        OptionsHelper.applyAttributes(
            context,
            a.getResourceId(R.styleable.RulerView_ruler_labelOptions, -1), labelOptions
        ) {

            val typeAttributes = context.obtainStyledAttributes(it, R.styleable.RulerView)
            attributes.readAttributes(typeAttributes)
            typeAttributes.recycle()

        }

        // override attributes by root
        attributes.readAttributes(a)

        a.recycle()

        this.sameLengthOfLabel = attributes.sameLengthOfLabel
        this.autoSizeMode = attributes.autoSizeMode
        this.autoSizeMinTextSize = attributes.autoSizeMinTextSize
        this.autoSizeMaxTextSize = attributes.autoSizeMaxTextSize
        this.autoSizeStepGranularity = attributes.autoSizeStepGranularity
        this.longestLabel = attributes.longestLabel

        // do not inset label drawable
        labelOptions.inset = false
        labelOptions.getDrawable()
            ?.apply {
                init(attributes.textColor) {
                    longestLabel
                }
                updatePaintInfo {
                    var updated = false
                    if (it.typeface != attributes.typeface) {
                        it.typeface = attributes.typeface
                        updated = true
                    }

                    if (it.textSize != attributes.textSize) {
                        it.textSize = attributes.textSize.coerceAtMost(autoSizeMaxTextSize)
                        updated = true
                    }

                    it.textAlign = attributes.textAlign
                    updated
                }
            }
    }


    fun resetLongestLabel(label: String?, notifyChange: Boolean = true) {
        val measuredText: String? = label ?: view.getAdapter()?.run {
            if (itemCount > 0) {
                if (sameLengthOfLabel) {
                    return@run getItemTitle(0)
                } else {
                    var tempText: String? = label ?: longestLabel
                    for (index in 0..itemCount) {
                        tempText = getItemTitle(index)
                            .takeIf {
                                it.length > tempText?.length ?: 0
                            } ?: tempText
                    }
                    return@run tempText
                }
            }
            return@run longestLabel
        }

        this.longestLabel = measuredText
        if (notifyChange) {
            labelOptions.getDrawable()?.updatePaintInfo { true }
        }
    }


    fun visibleWidthNeeded(visibleCountOfTick: Int, stepOfTicks: Int): Int {
        // 此处计算的 spacing 会被 Transform 均分
        val visibleSpacing = labelOptions.spacing.times(visibleCountOfTick.plus(1)).times(stepOfTicks)

        return labelOptions.widthNeeded.times(visibleCountOfTick)
            .plus(visibleSpacing)
    }

    fun visibleHeightNeeded(visibleCountOfTick: Int = 1, stepOfTicks: Int = 1): Int {
        // 此处计算的 spacing 会被 Transform 均分
        val visibleSpacing = labelOptions.spacing.times(visibleCountOfTick.plus(1)).times(stepOfTicks)

        return labelOptions.heightNeeded.times(visibleCountOfTick).plus(visibleSpacing)
    }


    fun resetTextSize() {
        labelOptions.getDrawable()
            ?.updatePaintInfo {
                val updated = it.textSize != autoSizeMaxTextSize
                if (updated) {
                    it.textSize = autoSizeMaxTextSize
                }
                updated
            }
    }

    fun autoTextSize(viewPort: ViewPortHandler? = null, measuredText: String? = longestLabel) {
        if (autoSizeMode != NEVER) {
            labelOptions.getDrawable()?.run {
                paint.textSize = autoSizeMaxTextSize
                measureTextSize(measuredText)

                while (shouldAutoTextSize(viewPort)) {
                    val updateValue = paint.textSize - autoSizeStepGranularity
                    if (updateValue >= autoSizeMinTextSize) {
                        paint.textSize = updateValue
                        measureTextSize(measuredText)
                    } else {
                        return
                    }
                }
            }
        }
    }

    fun shouldAutoTextSize(viewPort: ViewPortHandler?): Boolean {
        return viewPort != null && (labelOptions.widthNeeded == 0
                || labelOptions.heightNeeded == 0
                || labelOptions.widthNeeded > viewPort.contentWidth
                || labelOptions.heightNeeded > viewPort.contentHeight)
    }


    internal class TextDrawable : Drawable() {

        private lateinit var textColors: ColorStateList
        private lateinit var getLongestLabel: () -> String?
        val paint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)

        var textWidthNeeded = -1
        var textHeightNeeded = -1

        var text: String = ""

        fun init(textColors: ColorStateList, getLongestLabel: () -> String?) {
            this.textColors = textColors
            this.getLongestLabel = getLongestLabel
        }

        fun updatePaintInfo(onPaintUpdate: (paint: Paint) -> Boolean): Boolean {
            return if (onPaintUpdate(paint)) {
                measureTextSize(getLongestLabel())
            } else false
        }

        fun measureTextSize(longestText: String?): Boolean {
            val measuredText = longestText ?: text
            val textWidthNeeded = paint.measureText(measuredText).roundToInt()
            val textHeightNeeded = calcTextHeight(measuredText)
            return if (this.textWidthNeeded != textWidthNeeded || this.textHeightNeeded != textHeightNeeded) {
                this.textWidthNeeded = textWidthNeeded
                this.textHeightNeeded = textHeightNeeded
                true
            } else false
        }

        override fun isStateful(): Boolean {
            return textColors.isStateful
        }

        /**
         * @return 文本实际高度
         */
        private fun calcTextHeight(measuredText: String): Int {
            bounds.setEmpty()
            paint.getTextBounds(measuredText, 0, measuredText.length, bounds)
            return bounds.height()
        }

        override fun draw(canvas: Canvas) {
            if (TextUtils.isEmpty(text)) {
                return
            }

            paint.color = textColors.getColorForState(state, textColors.defaultColor)

            canvas.drawText(
                text,
                bounds.exactCenterX(),
                bounds.exactCenterY() + textHeightNeeded.minus(fontDescent()).div(2F),
                paint
            )
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        override fun getIntrinsicWidth(): Int {
            return textWidthNeeded
        }

        override fun getIntrinsicHeight(): Int {
            return textHeightNeeded
        }

        private fun fontDescent(): Float {
            return paint.fontMetrics.descent
        }

    }


}
