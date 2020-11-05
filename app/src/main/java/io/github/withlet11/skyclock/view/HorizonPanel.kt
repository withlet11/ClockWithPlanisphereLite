/*
 * HorizonPanel.kt
 *
 * Copyright 2020 Yasuhiro Yamakawa <withlet11@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.withlet11.skyclock.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import io.github.withlet11.skyclock.R
import kotlin.math.max
import kotlin.math.min


class HorizonPanel(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    companion object {
        private const val PREFERRED_SIZE = 800
        private const val CENTER = PREFERRED_SIZE * 0.5f
        private const val CIRCLE_RADIUS = PREFERRED_SIZE * 0.4f
    }

    private val paint = Paint()
    private val path = Path()
    private val dottedLine = DashPathEffect(floatArrayOf(2f, 2f), 0f)

    var horizon = listOf<Pair<Float, Float>>()
    var altAzimuth = listOf<List<Pair<Float, Float>?>>()
    var directionLetters = listOf<Triple<String, Float, Float>>()
    var isZoomed = false
    private var narrowSideLength = 0
    private var wideSideLength = 0

    private val horizonColor = context?.getColor(R.color.smoke) ?: 0
    private val altAzimuthLineColor = context?.getColor(R.color.skyBlue) ?: 0
    private val directionLetterColor = context?.getColor(R.color.lightGray) ?: 0
    private val siderealTimeIndicatorColor = context?.getColor(R.color.yellow) ?: 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        narrowSideLength = min(widthSize, heightSize)
        wideSideLength = max(widthSize, heightSize)

        setMeasuredDimension(wideSideLength, wideSideLength)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val drawAreaSize = if (isZoomed) wideSideLength else narrowSideLength
        val scale = drawAreaSize.toFloat() / PREFERRED_SIZE

        paint.isAntiAlias = true

        canvas?.scale(scale, scale)
        canvas?.translate(CENTER, CENTER)

        canvas?.drawHorizon()
        canvas?.drawAltitudeAndAzimuthLines()
        canvas?.drawDirectionLetters()
        canvas?.drawSiderealTimeIndicator()
    }

    private fun Canvas.drawHorizon() {
        paint.color = horizonColor
        paint.style = Paint.Style.FILL
        path.moveTo(horizon[0].first * CIRCLE_RADIUS, horizon[0].second * CIRCLE_RADIUS)
        horizon.forEach { path.lineTo(it.first * CIRCLE_RADIUS, it.second * CIRCLE_RADIUS) }
        this.drawPath(path, paint)
        path.reset()
    }

    private fun Canvas.drawAltitudeAndAzimuthLines() {
        paint.color = altAzimuthLineColor
        paint.strokeWidth = 1f
        paint.pathEffect = dottedLine
        paint.style = Paint.Style.STROKE
        altAzimuth.forEach { list ->
            var isPenDown = false
            list.forEach {
                if (isPenDown) {
                    if (it == null) {
                        isPenDown = false
                    } else {
                        path.lineTo(it.first * CIRCLE_RADIUS, it.second * CIRCLE_RADIUS)
                    }
                } else {
                    if (it != null) {
                        isPenDown = true
                        path.moveTo(it.first * CIRCLE_RADIUS, it.second * CIRCLE_RADIUS)
                    }
                }
            }
            this.drawPath(path, paint)
            path.reset()
        }
        paint.pathEffect = null
    }

    private fun Canvas.drawDirectionLetters() {
        paint.color = directionLetterColor
        paint.style = Paint.Style.FILL
        paint.textSize = 18f
        val fontMetrics = paint.fontMetrics
        directionLetters.forEach { triple ->
            val textWidth = paint.measureText(triple.first)
            this.drawText(
                triple.first,
                triple.second * CIRCLE_RADIUS - textWidth * 0.5f,
                triple.third * CIRCLE_RADIUS + textWidth * 0.5f,
                paint
            )
        }
    }

    private fun Canvas.drawSiderealTimeIndicator() {
        paint.color = siderealTimeIndicatorColor
        paint.style = Paint.Style.FILL
        /*
        this.drawCircle(
            0f,
            -PREFERRED_SIZE * 0.406f,
            4f,
            paint
        )
         */
        path.moveTo(0f, -PREFERRED_SIZE * 0.409f)
        path.lineTo(5f, -PREFERRED_SIZE * 0.397f)
        path.lineTo(-5f, -PREFERRED_SIZE * 0.397f)
        path.lineTo(0f, -PREFERRED_SIZE * 0.409f)
        this.drawPath(path, paint)
        path.reset()
    }
}
