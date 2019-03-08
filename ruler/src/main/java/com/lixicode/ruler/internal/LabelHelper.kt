package com.lixicode.ruler.internal

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import com.lixicode.ruler.R
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.data.FSize
import com.lixicode.ruler.data.Options
import com.lixicode.ruler.data.setBounds
import kotlin.math.roundToInt

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/7
 */
internal class LabelHelper(val view: RulerView) {

    companion object {
        private const val SANS = 1
        private const val SERIF = 2
        private const val MONOSPACE = 3
    }

    inner class Attributes(
        var textSize: Float = 0F,
        var textColor: ColorStateList? = null,
        var textAlign: Paint.Align = Paint.Align.LEFT,
        var typeface: Typeface = Typeface.DEFAULT,
        var sameLengthOfLabel: Boolean = false,
        var longestLabel: String = ""
    ) {


        fun readAttributes(a: TypedArray) {

            if (a.hasValue(R.styleable.RulerView_android_textColor)) {
                textColor = a.getColorStateList(R.styleable.RulerView_android_textColor)
            }

            if (a.hasValue(R.styleable.RulerView_android_textSize)) {
                textSize = a.getDimensionPixelSize(R.styleable.RulerView_android_textSize, -1).toFloat()
            }

            if (a.hasValue(R.styleable.RulerView_textAlign)) {
                textAlign = Paint.Align.values()[a.getInt(R.styleable.RulerView_textAlign, textAlign.ordinal)]
            }

            if (a.hasValue(R.styleable.RulerView_sameLengthOfLabel)) {
                sameLengthOfLabel = a.getBoolean(R.styleable.RulerView_sameLengthOfLabel, sameLengthOfLabel)
            }

            if (a.hasValue(R.styleable.RulerView_longestLabel)) {
                longestLabel = a.getString(R.styleable.RulerView_longestLabel) ?: longestLabel
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

    private var longestLabel: String = ""
    val labelOptions = Options(TextDrawable {
        longestLabel
    })

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
        val attributes = Attributes(textSize = defaultTextSize)

        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.RulerView, defStyleAttr, defStyleRes
        )
        attributes.readAttributes(a)

        OptionsHelper.applyAttributes(
            context,
            a.getResourceId(R.styleable.RulerView_labelOptions, -1), labelOptions
        ) {

            val typeAttributes = context.obtainStyledAttributes(it, R.styleable.RulerView)
            attributes.readAttributes(typeAttributes)
            typeAttributes.recycle()

        }
        a.recycle()


        setLongestLabel(attributes.longestLabel, attributes.sameLengthOfLabel, false)

        // do not inset label drawable
        labelOptions.inset = false

        labelOptions.getDrawable()
            ?.updatePaintInfo {
                var updated = false
                if (it.typeface != attributes.typeface) {
                    it.typeface = attributes.typeface
                    updated = true
                }

                if (it.textSize != attributes.textSize) {
                    it.textSize = attributes.textSize
                    updated = true
                }

                it.textAlign = attributes.textAlign
                it.color = attributes.textColor?.defaultColor ?: it.color
                updated
            }
    }


    fun setLongestLabel(label: String?, sameLengthOfLabel: Boolean, notifyChange: Boolean = true) {

        val helper = view.helper
        if (!TextUtils.isEmpty(label)) {
            this.longestLabel = label!!
        } else if (sameLengthOfLabel) {
            this.longestLabel = helper.valueFormatter.formatValue(helper.maximumOfTicks.toFloat())
        } else {
            var tempString = longestLabel
            var tempLength = tempString.length
            for (index in helper.minimumOfTicks..helper.maximumOfTicks step helper.stepOfTicks) {
                val formatted = helper.valueFormatter.formatValue(index.toFloat())
                if (formatted.length > tempLength) {
                    tempString = formatted
                    tempLength = formatted.length
                }
            }
            this.longestLabel = tempString
        }

        if (notifyChange) {
            labelOptions.getDrawable()?.updatePaintInfo { true }
        }
    }


    fun visibleWidthNeeded(visibleCountOfTick: Int, stepOfTicks: Int): Int {

        // 此处计算的 spacing 会被 Transform 均分
        val visiableSpacing = labelOptions.spacing.times(visibleCountOfTick.times(stepOfTicks))

        return labelOptions.widthNeeded.times(visibleCountOfTick)
            .plus(visiableSpacing)
    }

    fun visibleHeightNeeded(): Int {
        return labelOptions.heightNeeded
    }

    fun onDraw(canvas: Canvas) {
        val helper = view.helper
        if (helper.isHorizontal) {
            drawHorizontalLabel(helper, canvas)
        } else {
            drawVerticalLabel(helper, canvas)
        }
    }

    private fun drawVerticalLabel(helper: RulerViewHelper, canvas: Canvas) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun drawHorizontalLabel(helper: RulerViewHelper, canvas: Canvas) {
        if (!labelOptions.enable) {
            return
        }

        for (x in helper.rangeOfTickWithScrollOffset()) {
            if (helper.remOfTick(x)) {
                // 说明当前为起始刻度

                val textDrawable = labelOptions.getDrawable()
                textDrawable?.text = view.valueFormatter.formatValue(x.toFloat())

                // 计算标题居中显示所需 y 坐标起点
                val y = (helper.weightOfTick + helper.weightOfLabel).div(2)

                FSize.obtain(x.toFloat(), y).also {
                    helper.transformer.pointValuesToPixel(it)
                }.also {
                    labelOptions.setBounds(it.x, it.y + labelOptions.heightNeeded)
                }.also {
                    it.recycle()
                }
                textDrawable?.draw(canvas)
            }
        }
    }


    internal class TextDrawable(private val getLongestLabel: () -> String) : Drawable() {


        private val textRect by lazy {
            Rect()
        }


        private val paint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
        private var textWidthNeeded = -1
        private var textHeightNeeded = -1

        var text: String = ""

        fun updatePaintInfo(onPaintUpdate: (paint: Paint) -> Boolean) {
            if (onPaintUpdate(paint)) {
                val longestText = getLongestLabel()

                this.textWidthNeeded = paint.measureText(longestText).roundToInt()
                this.textHeightNeeded = calcTextHeight(longestText)
            }
        }

        /**
         * @return 文本实际高度
         */
        private fun calcTextHeight(measuredText: String): Int {
            val r = textRect
            r.set(0, 0, 0, 0)
            paint.getTextBounds(measuredText, 0, measuredText.length, r)
            return r.height()
        }

        override fun draw(canvas: Canvas) {
            if (TextUtils.isEmpty(text)) {
                return
            }
            canvas.drawText(text, bounds.centerX().toFloat(), bounds.centerY().toFloat(), paint)
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


    }


}
