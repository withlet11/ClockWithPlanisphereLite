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
import java.lang.Math.toDegrees
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

abstract class AbstractPanel(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    companion object {
        private const val PREFERRED_SIZE = 800f
        const val CENTER = PREFERRED_SIZE * 0.5f
        private const val CIRCLE_RADIUS = PREFERRED_SIZE * 0.4f
        const val BEZEL_RADIUS = 400f
        const val DATE_PANEL_RADIUS = 368f
        const val SKY_BACKGROUND_RADIUS = 336f

        /**
         * Convert a length on the canvas
         * @receiver a length (the radius of the draw area = 1)
         * @return the length on the canvas
         */
        fun Float.toCanvas(): Float = this * CIRCLE_RADIUS

        /**
         * Check if a position and [other] is near on the canvas
         * @receiver a position on the canvas
         * @return true if 2 position is near
         */
        fun Pair<Float, Float>.isNear(other: Pair<Float, Float>): Boolean =
            abs(first - other.first) < 50 && abs(second - other.second) < 50
    }

    var isZoomed = false
    var isLandScape = false
    var narrowSideLength = 0
    var wideSideLength = 0

    protected val centerPosition
        get() = (if (isZoomed) wideSideLength else narrowSideLength).let { it * 0.5f to it * 0.5f }

    private val scale: Float
        get() {
            val drawAreaSize = if (isZoomed) wideSideLength else narrowSideLength
            return drawAreaSize.toFloat() / PREFERRED_SIZE
        }

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

        canvas?.run {
            scale(scale, scale)
            translate(CENTER, CENTER)
        }
    }

    /**
     * Convert a position on the fragment to the absolute position on the canvas
     * @receiver a position on the fragment
     * @return the absolute position on the canvas
     */
    protected fun Pair<Float, Float>.toCanvasXY(): Pair<Float, Float> =
        first + scrollX to second + scrollY

    /**
     * Convert a relative position to the absolute position on the canvas with a rotate angle
     * @receiver a relative position (the radius of the draw area = 1)
     * @param rotate a rotate angle (degrees)
     * @return the absolute position on the canvas
     */
    protected fun Pair<Float, Float>.toAbsoluteXY(rotate: Float): Pair<Float, Float> {
        val x = first.toCanvas()
        val y = second.toCanvas()
        val rad = Math.toRadians(rotate.toDouble())
        val absoluteX = (x * cos(rad) - y * sin(rad)).toFloat() * scale + centerPosition.first
        val absoluteY = (x * sin(rad) + y * cos(rad)).toFloat() * scale + centerPosition.second
        return absoluteX to absoluteY
    }

    /**
     * Calculate the rotate angle of a position on the fragment with the absolute position of the
     * center on the canvas
     * @param x x of the absolute position on the fragment
     * @param y y of the absolute position on the fragment
     * @return the rotate angle (degrees)
     */
    fun getAngle(x: Float, y: Float): Float =
        (x to y).toCanvasXY().let { (absX, absY) ->
            toDegrees(
                atan2(
                    (absX - centerPosition.first).toDouble(),
                    -(absY - centerPosition.second).toDouble()
                )
            ).toFloat() // Note the order of the arguments as 0 deg is not on X axis but on Y axis
        }
}
