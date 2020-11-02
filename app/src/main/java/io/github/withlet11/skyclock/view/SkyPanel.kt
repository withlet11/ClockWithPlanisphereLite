/*
 * SkyPanel.kt
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
import io.github.withlet11.skyclock.model.ConstellationLineGeometry
import io.github.withlet11.skyclock.model.StarGeometry
import kotlin.math.*


class SkyPanel(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    companion object {
        private const val PREFERRED_SIZE = 800
        private const val CENTER = PREFERRED_SIZE * 0.5f
        private const val CIRCLE_RADIUS = PREFERRED_SIZE * 0.4f
    }

    private val paint = Paint()
    private val path = Path()

    var starGeometryList = listOf<StarGeometry>()
    var constellationLineList = listOf<ConstellationLineGeometry>()
    var equatorial = listOf<Pair<Int, Float>>()
    var ecliptic = listOf<Pair<Float, Float>>()
    var siderealAngle  = 0f
    var tenMinuteGridStep = 180f / 72f
    var isZoomed = false
    private var narrowSideLength = 0
    private var wideSideLength = 0


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
        canvas?.rotate(-siderealAngle * sign(tenMinuteGridStep), 0f, 0f)

        canvas?.drawEquatorial()
        canvas?.drawEcliptic()
        canvas?.drawStars()
        canvas?.drawConstellationLines()
        canvas?.drawRectAscensionLines()
        canvas?.drawRectAscensionRing()
    }

    private fun Canvas.drawEquatorial() {
        equatorial.forEach {
            if (it.first == 0) {
                paint.color = Color.rgb(192, 32, 32)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
            } else {
                paint.color = Color.LTGRAY
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.75f
            }
            this.drawOval(
                -it.second * CIRCLE_RADIUS, -it.second * CIRCLE_RADIUS,
                it.second * CIRCLE_RADIUS, it.second * CIRCLE_RADIUS,
                paint
            )
        }
    }

    private fun Canvas.drawEcliptic() {
        paint.color = Color.rgb(192, 192, 32)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        path.moveTo(ecliptic[0].first * CIRCLE_RADIUS, ecliptic[0].second * CIRCLE_RADIUS)
        ecliptic.forEach { path.lineTo(it.first * CIRCLE_RADIUS, it.second * CIRCLE_RADIUS) }
        this.drawPath(path, paint)
        path.reset()
    }

    private fun Canvas.drawStars() {
        paint.color = Color.LTGRAY
        paint.style = Paint.Style.FILL
        starGeometryList.forEach {
            this.drawCircle(it.x * CIRCLE_RADIUS,
                it.y * CIRCLE_RADIUS,
                it.r,
                paint)
        }
    }

    private fun Canvas.drawConstellationLines() {
        paint.strokeWidth = 1f
        paint.color = Color.LTGRAY
        constellationLineList.forEach {
            this.drawLine(
                it.x1 * CIRCLE_RADIUS,
                it.y1 * CIRCLE_RADIUS,
                it.x2 * CIRCLE_RADIUS,
                it.y2 * CIRCLE_RADIUS,
                paint
            )
        }
    }

    private fun Canvas.drawRectAscensionLines() {
        paint.strokeWidth = 0.75f
        paint.color = Color.LTGRAY
        for (i in 1..6) {
            val angle = i / 6.0 * PI
            val x = cos(angle).toFloat() * CIRCLE_RADIUS
            val y = sin(angle).toFloat() * CIRCLE_RADIUS
            this.drawLine(-x, -y, x, y, paint)
        }
    }

    private fun Canvas.drawRectAscensionRing() {
        paint.textSize = 16f
        paint.color = Color.LTGRAY
        paint.style = Paint.Style.FILL
        val fontMetrics = paint.fontMetrics

        for (i in 0..143) {
            this.save()
            // angle + 180 because text is drawn at opposite side
            this.rotate(i * tenMinuteGridStep + 180f)

            when {
                i % 6 == 0 -> {
                    val text = (i / 6).toString()
                    val textWidth = paint.measureText(text)
                    // positive height means opposite side
                    this.drawText(
                        text,
                        -textWidth * 0.5f,
                        -fontMetrics.descent + PREFERRED_SIZE * 0.42f,
                        paint
                    )
                }
                else -> {
                    this.drawCircle(
                        0f,
                        PREFERRED_SIZE * 0.408f,
                        2f,
                        paint
                    )
                }
            }
            this.restore()
        }
    }
}
