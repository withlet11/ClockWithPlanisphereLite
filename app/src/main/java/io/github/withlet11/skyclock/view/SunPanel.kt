/*
 * SunPanel.kt
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
import kotlin.math.*


class SunPanel(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    companion object {
        private const val PREFERRED_SIZE = 800
        private const val CENTER = PREFERRED_SIZE * 0.5f
        private const val CIRCLE_RADIUS = PREFERRED_SIZE * 0.4f
    }

    private val paint = Paint()
    private val path = Path()

    var analemma = listOf<Pair<Float, Float>>()
    var monthlyPositionList = listOf<Pair<Float, Float>>()
    var currentPosition = 0f to 0f

    var siderealAngle = 0f
    var tenMinuteGridStep = 180f / 72f
    var isZoomed = false
    private var narrowSideLength = 0
    private var wideSideLength = 0


    private val eclipticColor = context?.getColor(R.color.lemon) ?: 0
    private val sunColor = context?.getColor(R.color.orange) ?: 0

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

        canvas?.drawAnalemma()
        canvas?.drawMonthlyPosition()
        canvas?.drawCurrentPosition()
    }


    private fun Canvas.drawAnalemma() {
        paint.color = eclipticColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        path.moveTo(analemma.last().first * CIRCLE_RADIUS, analemma.last().second * CIRCLE_RADIUS)
        analemma.forEach { (x, y) -> path.lineTo(x * CIRCLE_RADIUS, y * CIRCLE_RADIUS) }
        this.drawPath(path, paint)
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
            when {
                pos.first < 0f -> drawText(
                    label,
                    pos.first * CIRCLE_RADIUS - 5f - offset,
                    pos.second * CIRCLE_RADIUS + 5f,
                    paint
                )
                else -> drawText(
                    label,
                    pos.first * CIRCLE_RADIUS + 5f,
                    pos.second * CIRCLE_RADIUS + 5f,
                    paint
                )
            }
            drawCircle(pos.first * CIRCLE_RADIUS, pos.second * CIRCLE_RADIUS, 3f, paint)
        }
    }

    private fun Canvas.drawCurrentPosition() {
        paint.color = sunColor
        paint.style = Paint.Style.FILL
        drawCircle(
            currentPosition.first * CIRCLE_RADIUS,
            currentPosition.second * CIRCLE_RADIUS,
            5f,
            paint
        )
    }
}
