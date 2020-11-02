/*
 * ClockBasePanel.kt
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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import io.github.withlet11.skyclock.model.DateObject
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

class ClockBasePanel(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    companion object {
        private const val PREFERRED_SIZE = 800
        private const val CENTER = PREFERRED_SIZE * 0.5f
    }

    private val paint = Paint()

    var offset = 0f
    var direction = false
    var dateList = listOf<DateObject>()
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

        canvas?.drawBackPanel()
        canvas?.drawGrid()
        canvas?.drawDate()
    }

    private fun Canvas.drawBackPanel() {
        listOf(
            0.5f to Color.rgb(4, 8, 32),
            0.46f to Color.LTGRAY,
            0.42f to Color.rgb(8, 16, 64)
        ).forEach { (size, color) ->
            val r = PREFERRED_SIZE * size
            paint.color = color
            this.drawCircle(0f, 0f, r, paint)
        }
    }

    private fun Canvas.drawGrid() {
        val rectangleSize = PREFERRED_SIZE * 0.028f
        val dot1radius = PREFERRED_SIZE * 0.010f
        val dot2radius = PREFERRED_SIZE * 0.006f
        val intervalOfDoubleRect = PREFERRED_SIZE * 0.01f
        val doubleRect1OffsetX = -rectangleSize - intervalOfDoubleRect * 0.5f
        val doubleRect2OffsetX = intervalOfDoubleRect * 0.5f
        val singleRectOffsetX = rectangleSize * 0.5f
        val offsetY = -PREFERRED_SIZE * 0.46f - rectangleSize
        val dot1OffsetY = offsetY + rectangleSize * 0.5f
        val dot2OffsetY = offsetY + rectangleSize * 0.5f

        paint.color = Color.GRAY
        paint.style = Paint.Style.FILL
        for (i in 0..59) {
            this.save()
            this.rotate(i * 6f) // 6 = 360 / 60
            when {
                i == 0 -> {
                    this.drawRect(
                        doubleRect1OffsetX,
                        offsetY,
                        doubleRect1OffsetX + rectangleSize,
                        offsetY + rectangleSize,
                        paint
                    )
                    this.drawRect(
                        doubleRect2OffsetX,
                        offsetY,
                        doubleRect2OffsetX + rectangleSize,
                        offsetY + rectangleSize,
                        paint
                    )
                }
                i % 15 == 0 ->
                    this.drawRect(
                        -singleRectOffsetX,
                        offsetY,
                        singleRectOffsetX,
                        offsetY + rectangleSize,
                        paint
                    )
                i % 5 == 0 ->
                    this.drawCircle(0f, dot1OffsetY, dot1radius, paint)
                else ->
                    this.drawCircle(0f, dot2OffsetY, dot2radius, paint)
            }
            this.restore()
        }
    }

    private fun Canvas.drawDate() {
        val circleY = PREFERRED_SIZE * 0.427f
        dateList.forEach { date ->
            this.save()
            // angle + 180 because text is drawn at opposite side
            this.rotate((-360f / dateList.size * date.dayOfYear + offset + 180f) * if (direction) -1f else 1f)

            when {
                date.isToday -> Color.RED to 4f
                date.dayOfMonth % 10 == 0 -> Color.BLACK to 3f
                date.dayOfMonth % 5 == 0 -> Color.BLACK to 2f
                else -> Color.BLACK to 1f
            }.let { (color, r) ->
                paint.style = Paint.Style.FILL
                paint.color = color
                this.drawCircle(0f, circleY, r, paint)
            }

            this.drawMonth(date)
            this.restore()
        }
    }

    private fun Canvas.drawMonth(date: DateObject) {
        paint.textSize = 24f
        when (date.dayOfMonth) {
            1 -> {
                paint.color = Color.DKGRAY
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                val startY = PREFERRED_SIZE * 0.42f
                val stopY = PREFERRED_SIZE * 0.46f
                val offsetRate = PI.toFloat() / if (direction) 365f else -365f
                this.drawLine(
                    startY * offsetRate,
                    startY,
                    stopY * offsetRate,
                    stopY,
                    paint
                )
            }
            15 -> {
                paint.color = Color.BLACK
                paint.style = Paint.Style.FILL
                val text = date.monthString
                val fontMetrics = paint.fontMetrics
                val r = PREFERRED_SIZE * 0.445f - (fontMetrics.ascent + fontMetrics.descent) * 0.5f
                val ratio = 180f / PI.toFloat() / r
                val start = ratio * paint.measureText(text) * 0.5f
                this.save()
                this.rotate(start)
                text.forEach {
                    this.drawText(it.toString(), 0f, r, paint)
                    val step = -ratio * paint.measureText(it.toString())
                    this.rotate(step)
                }
                this.restore()
            }
        }
    }
}
