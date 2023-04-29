/*
 * AbstractPanel.kt
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

import android.graphics.Bitmap
import android.graphics.Canvas

abstract class AbstractPanel {
    companion object {
        private const val PREFERRED_SIZE = 800f
        const val CENTER = PREFERRED_SIZE * 0.5f
        private const val CIRCLE_RADIUS = PREFERRED_SIZE * 0.4f
        const val MOON_RADIUS = 10f
        const val BEZEL_RADIUS = 400f
        const val DATE_PANEL_RADIUS = 368f
        const val SKY_BACKGROUND_RADIUS = 336f

        /**
         * Converts a length on the canvas.
         * @receiver a length (the radius of the draw area = 1)
         * @return the length on the canvas
         */
        fun Float.toCanvas(): Float = this * CIRCLE_RADIUS
    }

    val bmp: Bitmap =
        Bitmap.createBitmap(PREFERRED_SIZE.toInt(), PREFERRED_SIZE.toInt(), Bitmap.Config.ARGB_8888)

    open fun draw(canvas: Canvas) {
        canvas.translate(CENTER, CENTER)
    }
}