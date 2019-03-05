package com.lixicode.ruler.utils

import android.graphics.Matrix
import com.lixicode.ruler.data.FSize

/**
 * <>
 * @author 陈晓辉
 * @date 2019/3/5
 */
class Transformer(private val viewPort: ViewPortHandler) {


    var mMatrixPxToValue = Matrix()
    var mMatrixValueToPx = Matrix()


    fun prepareMatrixValuePx(xMinimum: Float, deltaX: Int, yMinimun: Float, deltaY: Int) {
        val scaleX = (viewPort.contentWidth / deltaX).letFinite()
        val scaleY = (viewPort.contentHeight / deltaY).letFinite()


        val matrix = mMatrixValueToPx
        matrix.reset()
        matrix.postTranslate(-xMinimum, -yMinimun)
        matrix.postScale(scaleX, scaleY)
        matrix.postTranslate(viewPort.offsetLeft, viewPort.offsetTop)


        matrix.invert(mMatrixPxToValue)
    }


    fun pointValuesToPixel(pts: FSize) {
        mMatrixValueToPx.mapPoints(pts)
    }

    fun invertPixelToValue(pts: FSize) {
        mMatrixPxToValue.mapPoints(pts)
    }


}


internal fun Matrix.mapPoints(pts: FSize) {
    mapPoints(pts.array)
}
