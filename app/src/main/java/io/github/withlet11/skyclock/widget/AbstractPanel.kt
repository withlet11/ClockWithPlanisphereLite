package io.github.withlet11.skyclock.widget

import android.graphics.Bitmap
import android.graphics.Canvas

abstract class AbstractPanel {
    companion object {
        private const val PREFERRED_SIZE = 800f
        const val CENTER = PREFERRED_SIZE * 0.5f
        private const val CIRCLE_RADIUS = PREFERRED_SIZE * 0.4f
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

    val bmp: Bitmap = Bitmap.createBitmap(PREFERRED_SIZE.toInt(), PREFERRED_SIZE.toInt(), Bitmap.Config.ARGB_8888)

    open fun draw(canvas: Canvas) {
        canvas.translate(CENTER, CENTER)
    }
}