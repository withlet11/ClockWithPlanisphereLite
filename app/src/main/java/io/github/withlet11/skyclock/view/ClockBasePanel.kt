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
import android.graphics.Paint
import android.util.AttributeSet
import io.github.withlet11.skyclock.R
import io.github.withlet11.skyclock.model.DateObject
import kotlin.math.*

class ClockBasePanel(context: Context?, attrs: AttributeSet?) : AbstractPanel(context, attrs) {
    private var offset = 0f
    private var direction = false
    var dateList = listOf<DateObject>()
    private var dayOfYear = 1

    private val paint = Paint().apply { isAntiAlias = true }
    private val bezelColor = context?.getColor(R.color.darkBlue) ?: 0
    private val minuteGridColor = context?.getColor(R.color.gray) ?: 0
    private val datePanelColor = context?.getColor(R.color.lightGray) ?: 0
    private val todayGridColor = context?.getColor(R.color.red) ?: 0
    private val dayGridColor = context?.getColor(R.color.black) ?: 0
    private val monthBorderColor = context?.getColor(R.color.darkGray) ?: 0
    private val monthNameColor = context?.getColor(R.color.black) ?: 0
    private val skyBackGroundColor = context?.getColor(R.color.midnightBlue) ?: 0

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.run {
            drawBackPanel()
            drawGrid()
            drawDate()
        }
    }

    private fun Canvas.drawBackPanel() {
        listOf(
            BEZEL_RADIUS to bezelColor,
            DATE_PANEL_RADIUS to datePanelColor,
            SKY_BACKGROUND_RADIUS to skyBackGroundColor
        ).forEach { (r, color) ->
            paint.color = color
            drawCircle(0f, 0f, r, paint)
        }
    }

    private fun Canvas.drawGrid() {
        val rectangleSize = 22f
        val dot1radius = 8f
        val dot2radius = 4f
        val intervalOfDoubleRect = 8f
        val doubleRect1OffsetX = -rectangleSize - intervalOfDoubleRect * 0.5f
        val doubleRect2OffsetX = intervalOfDoubleRect * 0.5f
        val singleRectOffsetX = rectangleSize * 0.5f
        val offsetY = -DATE_PANEL_RADIUS - rectangleSize
        val dot1OffsetY = offsetY + rectangleSize * 0.5f
        val dot2OffsetY = offsetY + rectangleSize * 0.5f

        paint.color = minuteGridColor
        paint.style = Paint.Style.FILL
        for (i in 0..59) {
            save()
            rotate(i * 6f) // 6 = 360 / 60
            when {
                i == 0 -> {
                    drawRect(
                        doubleRect1OffsetX,
                        offsetY,
                        doubleRect1OffsetX + rectangleSize,
                        offsetY + rectangleSize,
                        paint
                    )
                    drawRect(
                        doubleRect2OffsetX,
                        offsetY,
                        doubleRect2OffsetX + rectangleSize,
                        offsetY + rectangleSize,
                        paint
                    )
                }
                i % 15 == 0 ->
                    drawRect(
                        -singleRectOffsetX,
                        offsetY,
                        singleRectOffsetX,
                        offsetY + rectangleSize,
                        paint
                    )
                i % 5 == 0 ->
                    drawCircle(0f, dot1OffsetY, dot1radius, paint)
                else ->
                    drawCircle(0f, dot2OffsetY, dot2radius, paint)
            }
            restore()
        }
    }

    private fun Canvas.drawDate() {
        val circleY = 341f
        dateList.forEachIndexed { i, date ->
            save()
            // angle + 180 because text is drawn at opposite side
            rotate((-360f / dateList.size * date.dayOfYear + offset + 180f) * if (direction) -1f else 1f)

            val (color, r) = when {
                date.isToday -> {
                    dayOfYear = i + 1
                    todayGridColor to 4f
                }
                date.dayOfMonth % 10 == 0 -> dayGridColor to 3f
                date.dayOfMonth % 5 == 0 -> dayGridColor to 2f
                else -> dayGridColor to 1f
            }

            paint.style = Paint.Style.FILL
            paint.color = color
            drawCircle(0f, circleY, r, paint)

            drawMonth(date)
            restore()
        }
    }

    private fun Canvas.drawMonth(date: DateObject) {
        paint.textSize = 24f
        when (date.dayOfMonth) {
            1 -> {
                paint.color = monthBorderColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                val startY = SKY_BACKGROUND_RADIUS
                val stopY = DATE_PANEL_RADIUS
                val offsetRate = PI.toFloat() / if (direction) 365f else -365f
                drawLine(
                    startY * offsetRate,
                    startY,
                    stopY * offsetRate,
                    stopY,
                    paint
                )
            }
            15 -> {
                paint.color = monthNameColor
                paint.style = Paint.Style.FILL
                val monthName = date.monthString
                val fontMetrics = paint.fontMetrics
                val r = 356f - (fontMetrics.ascent + fontMetrics.descent) * 0.5f
                val ratio = 180f / PI.toFloat() / r
                val start = ratio * paint.measureText(monthName) * 0.5f
                save()
                rotate(start)
                monthName.forEach { name ->
                    drawText(name.toString(), 0f, r, paint)
                    val step = -ratio * paint.measureText(name.toString())
                    rotate(step)
                }
                restore()
            }
        }
    }

    fun set(offset: Float, direction: Boolean, dateList: List<DateObject>) {
        this.offset = offset
        this.direction = direction
        this.dateList = dateList
    }

    /**
     * Checks if a position is on the edge of sky background
     * @return true if a position is on the edge
     */
    fun isOnSkyBackgroundEdge(posOnFragment: Pair<Float, Float>): Boolean {
        val (center, radius) = SKY_BACKGROUND_RADIUS.toAbsoluteXY()
        return posOnFragment.toCanvasXY().isNear(center, radius)
    }

    /**
     * Checks if a position is on today grid.
     * @param posOnFragment a position on the fragment
     * @return true if a position is on today grid
     */
    fun isOnTodayGrid(posOnFragment: Pair<Float, Float>): Boolean {
        val rotate =
            ((-360.0 / dateList.size * dayOfYear + offset) * if (direction) -1.0 else 1.0) / 180.0 * PI
        val position =
            SKY_BACKGROUND_RADIUS * sin(rotate).toFloat() to -SKY_BACKGROUND_RADIUS * cos(rotate).toFloat()
        return posOnFragment.toCanvasXY().isNear(position.toAbsoluteXY())
    }

    /**
     * Gets the rotate angle of a position from January 1
     */
    fun getAngleFromJan1(x: Float, y: Float): Float =
        getAngle(x, y) + if (direction) offset else -offset
}
