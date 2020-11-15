package io.github.withlet11.skyclock.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import io.github.withlet11.skyclock.R
import io.github.withlet11.skyclock.model.AbstractSkyModel
import kotlin.math.*

class SkyPanel(context: Context) : AbstractPanel() {
    private var starGeometryList = listOf<AbstractSkyModel.StarGeometry>()
    private var constellationLineList = listOf<AbstractSkyModel.ConstellationLineGeometry>()
    private var equatorial = listOf<Pair<Int, Float>>()
    private var ecliptic = listOf<Pair<Float, Float>>()

    var siderealAngle = 0f
    private var tenMinuteGridStep = 180f / 72f

    private val paint = Paint().apply { isAntiAlias = true }
    private val path = Path()
    private val equatorColor = context.getColor(R.color.raspberry)
    private val declinationLineColor = context.getColor(R.color.lightGray)
    private val eclipticColor = context.getColor(R.color.lemon)
    private val starColor = context.getColor(R.color.lightGray)
    private val constellationLineColor = context.getColor(R.color.lightGray)
    private val rectAscensionLineColor = context.getColor(R.color.lightGray)
    private val rectAscensionRing = context.getColor(R.color.lightGray)

    fun draw() {
        draw(Canvas(bmp))
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.run {
            rotate(-siderealAngle * sign(tenMinuteGridStep), 0f, 0f)
            drawEquatorial()
            drawEcliptic()
            drawStars()
            drawConstellationLines()
            drawRectAscensionLines()
            drawRectAscensionRing()
        }
    }

    private fun Canvas.drawEquatorial() {
        equatorial.forEach { (index, radius) ->
            if (index == 0) {
                paint.color = equatorColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
            } else {
                paint.color = declinationLineColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.75f
            }
            drawCircle(0f, 0f, radius.toCanvas(), paint)
        }
    }

    private fun Canvas.drawEcliptic() {
        paint.color = eclipticColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        ecliptic.last().let { (x, y) -> path.moveTo(x.toCanvas(), y.toCanvas()) }
        ecliptic.forEach { (x, y) -> path.lineTo(x.toCanvas(), y.toCanvas()) }
        drawPath(path, paint)
        path.reset()
    }

    private fun Canvas.drawStars() {
        paint.color = starColor
        paint.style = Paint.Style.FILL
        starGeometryList.forEach { (x, y, size) ->
            drawCircle(x.toCanvas(), y.toCanvas(), size, paint)
        }
    }

    private fun Canvas.drawConstellationLines() {
        paint.strokeWidth = 1f
        paint.color = constellationLineColor
        constellationLineList.forEach { (x1, y1, x2, y2) ->
            drawLine(x1.toCanvas(), y1.toCanvas(), x2.toCanvas(), y2.toCanvas(), paint)
        }
    }

    private fun Canvas.drawRectAscensionLines() {
        paint.strokeWidth = 0.75f
        paint.color = rectAscensionLineColor
        for (i in 1..6) {
            val angle = i / 6.0 * PI
            val x = cos(angle).toFloat().toCanvas()
            val y = sin(angle).toFloat().toCanvas()
            drawLine(-x, -y, x, y, paint)
        }
    }

    private fun Canvas.drawRectAscensionRing() {
        paint.textSize = 16f
        paint.color = rectAscensionRing
        paint.style = Paint.Style.FILL
        val fontMetrics = paint.fontMetrics

        for (i in 0..143) {
            save()
            // angle + 180 because text is drawn at opposite side
            rotate(i * tenMinuteGridStep + 180f)

            when {
                i % 6 == 0 -> {
                    val text = (i / 6).toString()
                    val textWidth = paint.measureText(text)
                    // positive height means opposite side
                    drawText(
                        text,
                        -textWidth * 0.5f,
                        -fontMetrics.descent + SKY_BACKGROUND_RADIUS,
                        paint
                    )
                }
                else -> drawCircle(0f, 326f, 2f, paint)
            }
            restore()
        }
    }

    fun set(
        starGeometryList: List<AbstractSkyModel.StarGeometry>,
        constellationLineGeometry: List<AbstractSkyModel.ConstellationLineGeometry>,
        equatorial: List<Pair<Int, Float>>,
        ecliptic: List<Pair<Float, Float>>,
        tenMinuteGridStep: Float
    ) {
        this.starGeometryList = starGeometryList
        this.constellationLineList = constellationLineGeometry
        this.equatorial = equatorial
        this.ecliptic = ecliptic
        this.tenMinuteGridStep = tenMinuteGridStep
    }
}