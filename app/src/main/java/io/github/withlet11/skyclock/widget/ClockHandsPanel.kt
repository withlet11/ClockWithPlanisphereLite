package io.github.withlet11.skyclock.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import io.github.withlet11.skyclock.R
import java.time.LocalTime

class ClockHandsPanel(context: Context) : AbstractPanel() {
    var localTime: LocalTime = LocalTime.MIDNIGHT
    private val paint = Paint().apply { isAntiAlias = true }
    private val hourHandsColor = context.getColor(R.color.transparentBlue2)
    private val minuteHandsColor = context.getColor(R.color.transparentBlue3)
    private val secondHandsColor = context.getColor(R.color.transparentBlue1)

    fun draw() {
        draw(Canvas(bmp))
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.run {
            drawHourHand()
            drawMinuteHand()
            // drawSecondHand()
        }
    }

    private fun Canvas.drawHourHand() {
        save()
        rotate(180f / 6f * (localTime.toSecondOfDay() / 3600f + 6f))
        paint.color = hourHandsColor
        paint.style = Paint.Style.FILL
        drawRect(-5f, -40f, 5f, 240f, paint)
        restore()
    }

    private fun Canvas.drawMinuteHand() {
        save()
        rotate(180f / 30f * (localTime.minute + localTime.second / 60f + 30f))
        paint.color = minuteHandsColor
        paint.style = Paint.Style.FILL
        drawRect(-4f, -40f, 4f, 384f, paint)
        restore()
    }

    private fun Canvas.drawSecondHand() {
        save()
        rotate(180f / 30f * (localTime.second + 30f))
        paint.color = secondHandsColor
        paint.style = Paint.Style.FILL
        drawRect(-1.5f, -40f, 1.5f, 384f, paint)
        restore()
    }
}