/*
 * ClockHandsPanel.kt
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

package io.github.withlet11.clockwithplanispherelite.widget

import android.content.Context
import android.graphics.*
import io.github.withlet11.clockwithplanispherelite.R
import java.time.LocalTime

class ClockHandsPanel(context: Context) : AbstractPanel() {
    var localTime: LocalTime = LocalTime.MIDNIGHT
    private val paint = Paint().apply { isAntiAlias = true }
    private val path = Path()
    private val hourHandsColor = context.getColor(R.color.transparentBlue2)
    private val minuteHandsColor = context.getColor(R.color.transparentBlue1)
    private val secondHandsColor = context.getColor(R.color.transparentWhite)
    private val shadow = context.getColor(R.color.smoke)

    private val hourHandGeometries = listOf(
        0.0f to -20.0f,
        -5.2f to -19.3f,
        -10.0f to -17.3f,
        -14.1f to -14.1f,
        -17.3f to -10.0f,
        -19.3f to -5.2f,
        -20.0f to 0.0f,
        -19.3f to 5.2f,
        -17.3f to 10.0f,
        -14.1f to 14.1f,
        -10.0f to 17.3f,
        -10.0f to 236.0f,
        -9.5f to 238.0f,
        -8.0f to 239.5f,
        -6.0f to 240.0f,
        6.0f to 240.0f,
        8.0f to 239.5f,
        9.5f to 238.0f,
        10.0f to 236.0f,
        10.0f to 17.3f,
        14.1f to 14.1f,
        17.3f to 10.0f,
        19.3f to 5.2f,
        20.0f to 0.0f,
        19.3f to -5.2f,
        17.3f to -10.0f,
        14.1f to -14.1f,
        10.0f to -17.3f,
        5.2f to -19.3f
    )

    private val minuteHandGeometries = listOf(
        0.0f to -16.0f,
        -4.1f to -15.5f,
        -8.0f to -13.9f,
        -11.3f to -11.3f,
        -13.9f to -8.0f,
        -15.5f to -4.1f,
        -16.0f to 0.0f,
        -15.5f to 4.1f,
        -13.9f to 8.0f,
        -11.3f to 11.3f,
        -8.0f to 13.9f,
        -8.0f to 350.0f,
        -7.5f to 352.0f,
        -6.0f to 353.5f,
        -4.0f to 354.0f,
        4.0f to 354.0f,
        6.0f to 353.5f,
        7.5f to 352.0f,
        8.0f to 350.0f,
        8.0f to 13.9f,
        11.3f to 11.3f,
        13.9f to 8.0f,
        15.5f to 4.1f,
        16.0f to 0.0f,
        15.5f to -4.1f,
        13.9f to -8.0f,
        11.3f to -11.3f,
        8.0f to -13.9f,
        4.1f to -15.5f
    )

    /*
    private val secondHandGeometries = listOf(
        0.0f to -12.0f,
        -3.1f to -11.6f,
        -6.0f to -10.4f,
        -8.5f to -8.5f,
        -10.4f to -6.0f,
        -11.6f to -3.1f,
        -12.0f to 0.0f,
        -11.6f to 3.1f,
        -10.4f to 6.0f,
        -8.5f to 8.5f,
        -6.0f to 10.4f,
        -3.1f to 11.6f,
        -2.0f to 382.5f,
        -1.8f to 383.3f,
        -1.3f to 383.8f,
        -0.5f to 384.0f,
        0.5f to 384.0f,
        1.3f to 383.8f,
        1.8f to 383.3f,
        2.0f to 382.5f,
        3.1f to 11.6f,
        6.0f to 10.4f,
        8.5f to 8.5f,
        10.4f to 6.0f,
        11.6f to 3.1f,
        12.0f to 0.0f,
        11.6f to -3.1f,
        10.4f to -6.0f,
        8.5f to -8.5f,
        6.0f to -10.4f,
        3.1f to -11.6f
    )
     */

    fun draw() {
        draw(Canvas(bmp))
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.run {
            drawHourHand()
            drawMinuteHand()
            drawSecondHand()
        }
    }

    private fun Canvas.drawHourHand() {
        save()
        translate(5f, 5f)
        rotate(180f / 6f * (localTime.toSecondOfDay() / 3600f + 6f))
        paint.maskFilter = BlurMaskFilter(2f, BlurMaskFilter.Blur.NORMAL)
        paint.color = shadow
        paint.style = Paint.Style.FILL
        hourHandGeometries.last().let { (x, y) -> path.moveTo(x, y) }
        hourHandGeometries.forEach { (x, y) -> path.lineTo(x, y) }
        drawPath(path, paint)
        path.reset()
        restore()
        save()
        rotate(180f / 6f * (localTime.toSecondOfDay() / 3600f + 6f))
        paint.maskFilter = null
        paint.color = hourHandsColor
        paint.style = Paint.Style.FILL
        hourHandGeometries.last().let { (x, y) -> path.moveTo(x, y) }
        hourHandGeometries.forEach { (x, y) -> path.lineTo(x, y) }
        drawPath(path, paint)
        path.reset()
        restore()
    }

    private fun Canvas.drawMinuteHand() {
        save()
        translate(5f, 5f)
        rotate(180f / 30f * (localTime.minute + localTime.second / 60f + 30f))
        paint.maskFilter = BlurMaskFilter(2f, BlurMaskFilter.Blur.NORMAL)
        paint.color = shadow
        paint.style = Paint.Style.FILL
        minuteHandGeometries.last().let { (x, y) -> path.moveTo(x, y) }
        minuteHandGeometries.forEach { (x, y) -> path.lineTo(x, y) }
        drawPath(path, paint)
        path.reset()
        restore()
        save()
        rotate(180f / 30f * (localTime.minute + localTime.second / 60f + 30f))
        paint.maskFilter = null
        paint.color = minuteHandsColor
        paint.style = Paint.Style.FILL
        minuteHandGeometries.last().let { (x, y) -> path.moveTo(x, y) }
        minuteHandGeometries.forEach { (x, y) -> path.lineTo(x, y) }
        drawPath(path, paint)
        path.reset()
        restore()
    }

    private fun Canvas.drawSecondHand() {
        save()
        translate(5f, 5f)
        paint.color = shadow
        paint.style = Paint.Style.FILL
        drawCircle(0f, 0f, 12f, paint)
        restore()
        save()
        paint.color = secondHandsColor
        paint.style = Paint.Style.FILL
        drawCircle(0f, 0f, 12f, paint)
        restore()
    }
}