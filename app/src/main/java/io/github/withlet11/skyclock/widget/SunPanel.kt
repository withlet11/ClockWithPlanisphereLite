package io.github.withlet11.skyclock.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import io.github.withlet11.skyclock.R
import kotlin.math.sign

class SunPanel(context: Context) : AbstractPanel() {
    private var analemma = listOf<Pair<Float, Float>>()
    private var monthlyPositionList = listOf<Pair<Float, Float>>()

    private var sunPosition = 0f to 0f
    private val rotateAngle: Float get() = -solarAngle * sign(tenMinuteGridStep)
    var solarAngle = 0f
    private var tenMinuteGridStep = 180f / 72f

    private val paint = Paint().apply { isAntiAlias = true }
    private val path = Path()
    private val eclipticColor = context.getColor(R.color.lemon)
    private val sunColor = context.getColor(R.color.orange)

    fun draw() {
        draw(Canvas(bmp))
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.run {
            rotate(rotateAngle, 0f, 0f)
            drawAnalemma()
            drawMonthlyPosition()
            drawCurrentPosition()
        }
    }

    private fun Canvas.drawAnalemma() {
        paint.color = eclipticColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        analemma.last().let { (x, y) -> path.moveTo(x.toCanvas(), y.toCanvas()) }
        analemma.forEach { (x, y) -> path.lineTo(x.toCanvas(), y.toCanvas()) }
        drawPath(path, paint)
        path.reset()
    }

    private fun Canvas.drawMonthlyPosition() {
        paint.color = eclipticColor
        paint.style = Paint.Style.FILL
        val monthTextList =
            listOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII")
        monthlyPositionList.forEachIndexed { month, pos ->
            val label = monthTextList[month]
            val offset = paint.measureText(label)
            pos.let { (x, y) ->
                when {
                    x < 0f -> drawText(
                        label,
                        x.toCanvas() - 5f - offset,
                        y.toCanvas() + 5f,
                        paint
                    )
                    else -> drawText(label, x.toCanvas() + 5f, y.toCanvas() + 5f, paint)
                }
                drawCircle(x.toCanvas(), y.toCanvas(), 3f, paint)
            }
        }
    }

    private fun Canvas.drawCurrentPosition() {
        paint.color = sunColor
        paint.style = Paint.Style.FILL
        sunPosition.let { (x, y) ->
            drawCircle(x.toCanvas(), y.toCanvas(), 5f, paint)
        }
    }

    fun set(
        analemma: List<Pair<Float, Float>>,
        monthlyPositionList: List<Pair<Float, Float>>,
        currentPosition: Pair<Float, Float>,
        tenMinuteGridStep: Float
    ) {
        this.analemma = analemma
        this.monthlyPositionList = monthlyPositionList
        this.sunPosition = currentPosition
        this.tenMinuteGridStep = tenMinuteGridStep
    }
}