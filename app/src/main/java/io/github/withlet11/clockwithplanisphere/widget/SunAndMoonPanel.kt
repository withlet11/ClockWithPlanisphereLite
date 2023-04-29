/*
 * SunAndMoonPanel.kt
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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sign

class SunAndMoonPanel(context: Context) : AbstractPanel() {
    private var analemma = listOf<Pair<Float, Float>>()
    private var monthlyPositionList = listOf<Pair<Float, Float>>()

    private var sunPosition = 0f to 0f
    private var moonPosition = 0f to 0f
    private var longitudeOfSun = 0.0
    private var longitudeOfMoon = 0.0
    private var differenceOfLongitude = 0.0
    private val rotateAngleOfSun: Float get() = -solarAngle * sign(tenMinuteGridStep)
    private val rotateAngleOfMoon: Float get() = -siderealAngle * sign(tenMinuteGridStep)
    var solarAngle = 0f
    var siderealAngle = 0f
    private var tenMinuteGridStep = 180f / 72f

    private val paint = Paint().apply { isAntiAlias = true }
    private val path = Path()
    private val eclipticColor = context.getColor(R.color.dandelion)
    private val sunColor = context.getColor(R.color.ripeMango)
    private val moonColor = context.getColor(R.color.pastelYellow)
    private val moonDarkSideColor = context.getColor(R.color.darkBlue)

    fun draw() {
        draw(Canvas(bmp))
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.run {
            save()
            rotate(rotateAngleOfSun, 0f, 0f)
            drawAnalemma()
            drawMonthlyPosition()
            drawCurrentPosition()
            restore()
            drawMoon()
        }
    }

    private fun Canvas.drawAnalemma() {
        paint.color = eclipticColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        analemma.last().let { (x, y) -> path.moveTo(x.toCanvas(), y.toCanvas()) }
        analemma.forEach { (x, y) -> path.lineTo(x.toCanvas(), y.toCanvas()) }
        drawPath(path, paint)
        path.reset()
    }

    private fun Canvas.drawMonthlyPosition() {
        paint.color = eclipticColor
        paint.style = Paint.Style.FILL
        val monthTextList =
            listOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII")
        monthlyPositionList.forEachIndexed { month, pos ->
            val label = monthTextList[month]
            val offset = paint.measureText(label)
            pos.let { (x, y) ->
                when {
                    x < 0f -> drawText(
                        label,
                        x.toCanvas() - 5f - offset,
                        y.toCanvas() + 5f,
                        paint
                    )
                    else -> drawText(label, x.toCanvas() + 5f, y.toCanvas() + 5f, paint)
                }
                drawCircle(x.toCanvas(), y.toCanvas(), 3f, paint)
            }
        }
    }

    private fun Canvas.drawCurrentPosition() {
        paint.color = sunColor
        paint.style = Paint.Style.FILL
        sunPosition.let { (x, y) ->
            drawCircle(x.toCanvas(), y.toCanvas(), 5f, paint)
        }
    }

    private fun Canvas.drawMoon() {
        save()
        rotate(rotateAngleOfMoon, 0f, 0f)
        translate(moonPosition.first.toCanvas(), moonPosition.second.toCanvas())
        rotate(
            rotateAngleOfSun - rotateAngleOfMoon -
                    if (tenMinuteGridStep > 0.0) (180 - differenceOfLongitude.toFloat())
                    else differenceOfLongitude.toFloat()
        )
        paint.style = Paint.Style.FILL
        val phase = abs(cos(Math.toRadians(differenceOfLongitude)).toFloat() * MOON_RADIUS)
        val (isFirstHalf, color) = when {
            differenceOfLongitude < 90 -> true to moonDarkSideColor
            differenceOfLongitude < 180 -> true to moonColor
            differenceOfLongitude < 270 -> false to moonColor
            else -> false to moonDarkSideColor
        }
        drawHalfMoon(isFirstHalf)
        paint.color = color
        drawOval(-phase, -MOON_RADIUS, phase, MOON_RADIUS, paint)
        restore()
    }

    private fun Canvas.drawHalfMoon(isFirstHalf: Boolean) {
        paint.color = moonColor
        drawCircle(0f, 0f, MOON_RADIUS, paint)
        paint.color = moonDarkSideColor
        drawArc(
            -MOON_RADIUS,
            -MOON_RADIUS,
            MOON_RADIUS,
            MOON_RADIUS,
            if (isFirstHalf) 90f else -90f,
            180f,
            false,
            paint
        )
    }

    fun set(
        analemma: List<Pair<Float, Float>>,
        monthlyPositionList: List<Pair<Float, Float>>,
        currentSunPosition: Pair<Pair<Float, Float>, Double>,
        currentMoonPosition: Pair<Pair<Float, Float>, Double>,
        tenMinuteGridStep: Float
    ) {
        this.analemma = analemma
        this.monthlyPositionList = monthlyPositionList
        this.sunPosition = currentSunPosition.first
        this.moonPosition = currentMoonPosition.first
        this.longitudeOfSun = currentSunPosition.second
        this.longitudeOfMoon = currentMoonPosition.second
        this.differenceOfLongitude =
            ((currentMoonPosition.second - longitudeOfSun) % 360.0 + 360.0) % 360.0
        this.tenMinuteGridStep = tenMinuteGridStep
    }
}