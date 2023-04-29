/*
 * SkyPanel.kt
 *
 * Copyright 2020-2023 Yasuhiro Yamakawa <withlet11@gmail.com>
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

package io.github.withlet11.clockwithplanispherelite.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import io.github.withlet11.clockwithplanispherelite.R
import io.github.withlet11.clockwithplanispherelite.model.AbstractSkyModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin

class SkyPanel(context: Context) : AbstractPanel() {
    private var starGeometryList = listOf<AbstractSkyModel.StarGeometry>()
    private var constellationLineList = listOf<AbstractSkyModel.ConstellationLineGeometry>()
    private var milkyWayDotList = listOf<AbstractSkyModel.MilkyWayDot>()
    private var milkyWayDotSize = 0f
    private var equatorial = listOf<Pair<Int, Float>>()
    private var ecliptic = listOf<Pair<Float, Float>>()

    var siderealAngle = 0f
    private var tenMinuteGridStep = 180f / 72f

    private val paint = Paint().apply { isAntiAlias = true }
    private val path = Path()
    private val equatorColor = context.getColor(R.color.sprintRed)
    private val declinationLineColor = context.getColor(R.color.silver)
    private val eclipticColor = context.getColor(R.color.dandelion)
    private val starColor = context.getColor(R.color.silver)
    private val constellationLineColor = context.getColor(R.color.silver)
    private val rightAscensionLineColor = context.getColor(R.color.silver)
    private val rightAscensionRing = context.getColor(R.color.silver)

    fun draw() {
        draw(Canvas(bmp))
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.run {
            rotate(-siderealAngle * sign(tenMinuteGridStep), 0f, 0f)
            drawEquatorial()
            drawEcliptic()
            drawStars()
            drawMilkyWay()
            drawConstellationLines()
            drawRightAscensionLines()
            drawRightAscensionRing()
        }
    }

    private fun Canvas.drawEquatorial() {
        equatorial.forEach { (index, radius) ->
            if (index == 0) {
                paint.color = equatorColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
            } else {
                paint.color = declinationLineColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.75f
            }
            drawCircle(0f, 0f, radius.toCanvas(), paint)
        }
    }

    private fun Canvas.drawEcliptic() {
        paint.color = eclipticColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        ecliptic.last().let { (x, y) -> path.moveTo(x.toCanvas(), y.toCanvas()) }
        ecliptic.forEach { (x, y) -> path.lineTo(x.toCanvas(), y.toCanvas()) }
        drawPath(path, paint)
        path.reset()
    }

    private fun Canvas.drawStars() {
        paint.color = starColor
        paint.style = Paint.Style.FILL
        starGeometryList.forEach { (x, y, radius) ->
            drawCircle(x.toCanvas(), y.toCanvas(), radius, paint)
        }
    }

    private fun Canvas.drawMilkyWay() {
        paint.style = Paint.Style.FILL
        milkyWayDotList.forEach { (x, y, color) ->
            paint.color = color
            drawRect(
                x.toCanvas(),
                y.toCanvas(),
                (x + milkyWayDotSize).toCanvas(),
                (y + milkyWayDotSize).toCanvas(),
                paint
            )
        }
    }

    private fun Canvas.drawConstellationLines() {
        paint.strokeWidth = 1f
        paint.color = constellationLineColor
        constellationLineList.forEach { (x1, y1, x2, y2) ->
            drawLine(x1.toCanvas(), y1.toCanvas(), x2.toCanvas(), y2.toCanvas(), paint)
        }
    }

    private fun Canvas.drawRightAscensionLines() {
        paint.strokeWidth = 0.75f
        paint.color = rightAscensionLineColor
        for (i in 1..6) {
            val angle = i / 6.0 * PI
            val x = cos(angle).toFloat().toCanvas()
            val y = sin(angle).toFloat().toCanvas()
            drawLine(-x, -y, x, y, paint)
        }
    }

    private fun Canvas.drawRightAscensionRing() {
        paint.textSize = 16f
        paint.color = rightAscensionRing
        paint.style = Paint.Style.FILL
        val fontMetrics = paint.fontMetrics

        for (i in 0..143) {
            save()
            // angle + 180 because text is drawn at opposite side
            rotate(i * tenMinuteGridStep + 180f)

            when {
                i % 6 == 0 -> {
                    val text = (i / 6).toString()
                    val textWidth = paint.measureText(text)
                    // positive height means opposite side
                    drawText(
                        text,
                        -textWidth * 0.5f,
                        -fontMetrics.descent + SKY_BACKGROUND_RADIUS,
                        paint
                    )
                }
                else -> drawCircle(0f, 326f, 2f, paint)
            }
            restore()
        }
    }

    fun set(
        starGeometryList: List<AbstractSkyModel.StarGeometry>,
        constellationLineGeometry: List<AbstractSkyModel.ConstellationLineGeometry>,
        milkyWayDotList: List<AbstractSkyModel.MilkyWayDot>,
        milkyWayDotSize: Float,
        equatorial: List<Pair<Int, Float>>,
        ecliptic: List<Pair<Float, Float>>,
        tenMinuteGridStep: Float
    ) {
        this.starGeometryList = starGeometryList
        this.constellationLineList = constellationLineGeometry
        this.milkyWayDotList = milkyWayDotList
        this.milkyWayDotSize = milkyWayDotSize
        this.equatorial = equatorial
        this.ecliptic = ecliptic
        this.tenMinuteGridStep = tenMinuteGridStep
    }
}