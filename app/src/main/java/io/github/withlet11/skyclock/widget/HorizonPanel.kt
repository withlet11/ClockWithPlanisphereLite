package io.github.withlet11.skyclock.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import io.github.withlet11.skyclock.R

class HorizonPanel(context: Context) : AbstractPanel() {
    private var horizon = listOf<Pair<Float, Float>>()
    private var altAzimuth = listOf<List<Pair<Float, Float>?>>()
    private var directionLetters = listOf<Triple<String, Float, Float>>()

    private val paint = Paint().apply { isAntiAlias = true }
    private val path = Path()
    private val dottedLine = DashPathEffect(floatArrayOf(2f, 2f), 0f)
    private val horizonColor = context.getColor(R.color.smoke)
    private val altAzimuthLineColor = context.getColor(R.color.skyBlue)
    private val directionLetterColor = context.getColor(R.color.lightGray)
    private val siderealTimeIndicatorColor = context.getColor(R.color.yellow)

    fun draw() {
        draw(Canvas(bmp))
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.run {
            drawHorizon()
            drawAltitudeAndAzimuthLines()
            drawDirectionLetters()
            drawSiderealTimeIndicator()
        }
    }

    private fun Canvas.drawHorizon() {
        paint.color = horizonColor
        paint.style = Paint.Style.FILL
        horizon.first().let { (x, y) -> path.moveTo(x.toCanvas(), y.toCanvas()) }
        horizon.forEach { (x, y) -> path.lineTo(x.toCanvas(), y.toCanvas()) }
        drawPath(path, paint)
        path.reset()
    }

    private fun Canvas.drawAltitudeAndAzimuthLines() {
        paint.color = altAzimuthLineColor
        paint.strokeWidth = 1f
        paint.pathEffect = dottedLine
        paint.style = Paint.Style.STROKE
        altAzimuth.forEach { list ->
            var isPenDown = false
            list.forEach { pos ->
                if (isPenDown) {
                    if (pos == null) {
                        isPenDown = false
                    } else {
                        val (x, y) = pos
                        path.lineTo(x.toCanvas(), y.toCanvas())
                    }
                } else {
                    pos?.let { (x, y) ->
                        isPenDown = true
                        path.moveTo(x.toCanvas(), y.toCanvas())
                    }
                }
            }
            drawPath(path, paint)
            path.reset()
        }
        paint.pathEffect = null
    }

    private fun Canvas.drawDirectionLetters() {
        paint.color = directionLetterColor
        paint.style = Paint.Style.FILL
        paint.textSize = 18f
        directionLetters.forEach { (letter, x, y) ->
            val textWidth = paint.measureText(letter)
            drawText(
                letter,
                x.toCanvas() - textWidth * 0.5f,
                y.toCanvas() + textWidth * 0.5f,
                paint
            )
        }
    }

    /**
     * Draws a triangle as an indicator of sidereal time.
     */
    private fun Canvas.drawSiderealTimeIndicator() {
        paint.color = siderealTimeIndicatorColor
        paint.style = Paint.Style.FILL
        path.moveTo(0f, -327f)
        path.lineTo(5f, -318f)
        path.lineTo(-5f, -318f)
        path.lineTo(0f, -327f)
        drawPath(path, paint)
        path.reset()
    }

    fun set(
        horizon: List<Pair<Float, Float>>,
        altAzimuth: List<List<Pair<Float, Float>?>>,
        directionLetters: List<Triple<String, Float, Float>>
    ) {
        this.horizon = horizon
        this.altAzimuth = altAzimuth
        this.directionLetters = directionLetters
    }
}