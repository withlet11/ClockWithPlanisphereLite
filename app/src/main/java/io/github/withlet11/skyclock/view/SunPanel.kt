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
import io.github.withlet11.skyclock.R
import kotlin.math.*

/**
 * This class is a view that show the Sun and analemma.
 */
class SunPanel(context: Context?, attrs: AttributeSet?) : AbstractPanel(context, attrs) {
    private var analemma = listOf<Pair<Float, Float>>()
    private var monthlyPositionList = listOf<Pair<Float, Float>>()
    private var currentPosition = 0f to 0f
    var hourAngle = 0f
    private val rotateAngle: Float get() = -hourAngle * sign(tenMinuteGridStep)
    private var tenMinuteGridStep = 180f / 72f

    private val paint = Paint().apply { isAntiAlias = true }
    private val path = Path()
    private val eclipticColor = context?.getColor(R.color.lemon) ?: 0
    private val sunColor = context?.getColor(R.color.orange) ?: 0

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.run {
            rotate(rotateAngle, 0f, 0f)
            drawAnalemma()
            drawMonthlyPosition()
            drawCurrentPosition()
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
        currentPosition.let { (x, y) ->
            drawCircle(x.toCanvas(), y.toCanvas(), 5f, paint)
        }
    }

    /**
     * Checks if a position is on analemma.
     * @param posOnFragment a position on the fragment
     * @return true if a position is on analemma
     */
    fun isOnAnalemma(posOnFragment: Pair<Float, Float>): Boolean {
        val analemmaPosition = currentPosition.toAbsoluteXY(-hourAngle * sign(tenMinuteGridStep))
        return posOnFragment.toCanvasXY().isNear(analemmaPosition)
    }

    fun set(
        analemma: List<Pair<Float, Float>>,
        monthlyPositionList: List<Pair<Float, Float>>,
        currentPosition: Pair<Float, Float>,
        tenMinuteGridStep: Float

    ) {
        this.analemma = analemma
        this.monthlyPositionList = monthlyPositionList
        this.currentPosition = currentPosition
        this.tenMinuteGridStep = tenMinuteGridStep
    }

    fun setHourAngleAndCurrentPosition(
        hourAngle: Float,
        currentPosition: Pair<Float, Float>
    ) {
        this.hourAngle = hourAngle
        this.currentPosition = currentPosition
    }
}
