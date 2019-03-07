package com.lixicode.ruler.internal

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import com.lixicode.ruler.R
import com.lixicode.ruler.RulerView
import com.lixicode.ruler.data.FSize
import com.lixicode.ruler.data.Options
import kotlin.math.roundToInt

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/7
 */
internal class LabelHelper(val view: RulerView, getLongestLabel: () -> String) {

    companion object {
        private const val SANS = 1
        private const val SERIF = 2
        private const val MONOSPACE = 3
    }

    val labelOptions = Options(drawable = TextDrawable(getLongestLabel))

    fun loadFromAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.RulerView, defStyleAttr, 0
        )
        val weightOfLabel = a.getFloat(R.styleable.RulerView_weightOfLabel, 1F)
        val textColor = a.getColorStateList(R.styleable.RulerView_android_textColor)
        val textSize = a.getDimensionPixelSize(R.styleable.RulerView_android_textSize, 0).toFloat()
        val textAlign = Paint.Align.values()[a.getInt(
            R.styleable.RulerView_textAlign,
            Paint.Align.LEFT.ordinal
        )]

        val typeface = getTypefaceAndStyle(a)
        a.recycle()

        labelOptions.weight = weightOfLabel
        labelOptions.getDrawable()
            ?.updatePaintInfo {
                var updated = false
                if (it.typeface != typeface) {
                    it.typeface = typeface
                    updated = true
                }

                if (it.textSize != textSize) {
                    it.textSize = textSize
                    updated = true
                }

                it.textAlign = textAlign

                it.color = textColor?.defaultColor ?: it.color
                updated
            }
    }

    fun notifyLongestTextChange() {
        labelOptions.getDrawable()
            ?.updatePaintInfo { true }
    }


    private fun getTypefaceAndStyle(a: TypedArray): Typeface {
        val textStyle = a.getInt(R.styleable.RulerView_android_textStyle, Typeface.NORMAL)
        if (a.hasValue(R.styleable.RulerView_android_fontFamily)) {
            // Try with String. This is done by TextView JB+, but fails in ICS
            val fontFamilyName = a.getString(R.styleable.RulerView_android_fontFamily)
            if (fontFamilyName != null) {
                return Typeface.create(fontFamilyName, textStyle)
            }
        }
        return when (a.getInt(
            R.styleable.RulerView_android_typeface,
            SANS
        )) {
            SANS -> Typeface.SANS_SERIF
            SERIF -> Typeface.SERIF
            MONOSPACE -> Typeface.MONOSPACE
            else -> Typeface.defaultFromStyle(textStyle)
        }
    }

    fun visibleWidthNeeded(visibleCountOfTick: Int): Int {
        return labelOptions.widthNeeded.times(visibleCountOfTick)
    }

    fun visibleHeightNeeded(): Int {
        return labelOptions.heightNeeded
    }

    fun onDraw(canvas: Canvas) {
        labelOptions.getDrawable()?.run {
            val helper = view.helper
            for (x in helper.rangeOfTickWithScrollOffset()) {
                if (helper.remOfTick(x)) {
                    // 说明当前为起始刻度

                    text = view.valueFormatter.formatValue(x.toFloat())

                    FSize.obtain(x.toFloat(), 2F).also {
                        helper.transformer.pointValuesToPixel(it)
                    }.also {
                        val intX = it.x.roundToInt()
                        val intY = it.y.roundToInt()
                        setBounds(intX, intY, intX, intY)
                    }.also {
                        it.recycle()
                    }

                    draw(canvas)
                }
            }
        }

        // TODO draw event on vertical mode
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


        private fun calcTextHeight(measuredText: String): Int {
            val r = textRect
            r.set(0, 0, 0, 0)
            paint.getTextBounds(measuredText, 0, measuredText.length, r)
            return r.height()
        }


    }


}
