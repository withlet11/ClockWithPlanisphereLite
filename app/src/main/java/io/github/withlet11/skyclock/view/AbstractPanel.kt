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
import android.util.AttributeSet
import android.view.View

abstract class AbstractPanel(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    companion object {
        private const val PREFERRED_SIZE = 800f
        private const val CENTER = PREFERRED_SIZE * 0.5f
        private const val CIRCLE_RADIUS = PREFERRED_SIZE * 0.4f
        const val BEZEL_RADIUS = 400f
        const val DATE_PANEL_RADIUS = 368f
        const val SKY_BACKGROUND_RADIUS = 336f

        fun Float.toCanvasPos(): Float = this * CIRCLE_RADIUS
    }

    var isZoomed = false
    var isLandScape = false
    var narrowSideLength = 0
    var wideSideLength = 0

    final override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
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

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val drawAreaSize = if (isZoomed) wideSideLength else narrowSideLength
        val scale = drawAreaSize.toFloat() / PREFERRED_SIZE

        canvas?.run {
            scale(scale, scale)
            translate(CENTER, CENTER)
        }
    }

}
