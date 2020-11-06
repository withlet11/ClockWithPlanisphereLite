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

package io.github.withlet11.skyclock.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import io.github.withlet11.skyclock.R

class ClockHandsPanel(context: Context?, attrs: AttributeSet?) : AbstractPanel(context, attrs) {
    var hour = 0
    var minute = 0
    var second = 0

    private val paint = Paint().apply { isAntiAlias = true }
    private val hourHandsColor = context?.getColor(R.color.transparentBlue2) ?: 0
    private val minuteHandsColor = context?.getColor(R.color.transparentBlue3) ?: 0
    private val secondHandsColor = context?.getColor(R.color.transparentBlue1) ?: 0

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.run {
            drawHourHand()
            drawMinuteHand()
            drawSecondHand()
        }
    }

    private fun Canvas.drawHourHand() {
        save()
        rotate(180f / 6f * (hour + (minute + second / 60f) / 60f + 6f))
        paint.color = hourHandsColor
        paint.style = Paint.Style.FILL
        drawRect(-5f, -40f, 5f, 240f, paint)
        restore()
    }

    private fun Canvas.drawMinuteHand() {
        save()
        rotate(180f / 30f * (minute + second / 60f + 30f))
        paint.color = minuteHandsColor
        paint.style = Paint.Style.FILL
        drawRect(-4f, -40f, 4f, 384f, paint)
        restore()
    }

    private fun Canvas.drawSecondHand() {
        save()
        rotate(180f / 30f * (second + 30f))
        paint.color = secondHandsColor
        paint.style = Paint.Style.FILL
        drawRect(-1.5f, -40f, 1.5f, 384f, paint)
        restore()
    }
}
