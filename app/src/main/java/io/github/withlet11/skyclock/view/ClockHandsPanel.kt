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

class ClockHandsPanel(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    companion object {
        private const val PREFERRED_SIZE = 800
        private const val CENTER = PREFERRED_SIZE * 0.5f
    }

    private val paint = Paint()

    var hour = 0
    var minute = 0
    var second = 0
    var isZoomed = false
    var isLandScape = false
    var narrowSideLength = 0
    var wideSideLength = 0

    private val hourHandsColor = context?.getColor(R.color.transparentBlue2) ?: 0
    private val minuteHandsColor = context?.getColor(R.color.transparentBlue3) ?: 0
    private val secondHandsColor = context?.getColor(R.color.transparentBlue1) ?: 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        isLandScape = (widthSize > heightSize).also {
            if (it) {
                narrowSideLength = heightSize
                wideSideLength = widthSize
            } else {
                narrowSideLength = widthSize
                wideSideLength = heightSize
            }
        }

        setMeasuredDimension(wideSideLength, wideSideLength)
    }


    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val drawAreaSize = if (isZoomed) wideSideLength else narrowSideLength
        val scale = drawAreaSize.toFloat() / PREFERRED_SIZE

        paint.isAntiAlias = true

        canvas?.save()
        canvas?.scale(scale, scale)
        canvas?.translate(CENTER, CENTER)

        canvas?.let {
            drawHourHand(canvas)
            drawMinuteHand(canvas)
            drawSecondHand(canvas)
        }

        canvas?.restore()
    }

    private fun drawHourHand(canvas: Canvas) {
        canvas.save()
        canvas.rotate(180f / 6f * (hour + (minute + second / 60f) / 60f + 6f))
        paint.color = hourHandsColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(
            -PREFERRED_SIZE * 0.006f,
            -PREFERRED_SIZE * 0.05f,
            PREFERRED_SIZE * 0.006f,
            PREFERRED_SIZE * 0.30f,
            paint
        )
        canvas.restore()
    }

    private fun drawMinuteHand(canvas: Canvas) {
        canvas.save()
        canvas.rotate(180f / 30f * (minute + second / 60f + 30f))
        paint.color = minuteHandsColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(
            -PREFERRED_SIZE * 0.005f,
            -PREFERRED_SIZE * 0.05f,
            PREFERRED_SIZE * 0.005f,
            PREFERRED_SIZE * 0.48f,
            paint
        )
        canvas.restore()
    }

    private fun drawSecondHand(canvas: Canvas) {
        canvas.save()
        canvas.rotate(180f / 30f * (second + 30f))
        paint.color = secondHandsColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(
            -PREFERRED_SIZE * 0.002f,
            -PREFERRED_SIZE * 0.05f,
            PREFERRED_SIZE * 0.002f,
            PREFERRED_SIZE * 0.48f,
            paint
        )
        canvas.restore()
    }
}
