/*
 * AbstractSkyModel.kt
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

package io.github.withlet11.skyclock.model

import android.content.Context
import android.content.res.AssetManager
import java.io.*
import java.util.ArrayList
import kotlin.math.*

abstract class AbstractSkyModel {
    data class StarGeometry(val x: Float, val y: Float, val r: Float)
    data class ConstellationLineGeometry(val x1: Float, val y1: Float, val x2: Float, val y2: Float)

    companion object {
        const val ANGLE_LIMIT = 155.0
    }

    private val hipLiteMajorFile = "hip_lite_major.csv"
    private val constellationLineFile = "hip_constellation_line.csv"

    abstract fun toAngle(declination: Double): Double
    abstract fun toDeclinationFromPole(angle: Int): Int
    protected abstract fun toRadius(declination: Double): Double
    protected abstract fun toRadiansFromHours(hour: Double): Double
    abstract fun toRadiansFromDegrees(degree: Double): Double

    var latitude = 0.0
        set(value) {
            field = value
            maxAngle = min(toAngle(value) + 10.0, ANGLE_LIMIT)
            equatorial = List(5) {
                val angle = (it + 1) * 30
                val dec = toDeclinationFromPole(angle)
                if (angle < toAngle(value)) dec to (angle / maxAngle).toFloat() else null
            }.filterNotNull()
            ecliptic = List(360) {
                val (dec, ra) = convertToEquatorialFromEcliptic(it.toDouble())
                val radius = toRadius(dec)
                val angle = toRadiansFromDegrees(-ra) // need hour angle
                (-radius * sin(angle)).toFloat() to (-radius * cos(angle)).toFloat()
            }
        }

    protected var maxAngle = 0.0
    abstract val tenMinuteGridStep: Float

    var equatorial = listOf<Pair<Int, Float>>()
    var ecliptic = listOf<Pair<Float, Float>>()

    private val starCatalog = ArrayList<StarParameters?>()
    private val constellationLine = ArrayList<Pair<Int, Int>>()

    var starGeometryList = listOf<StarGeometry>()
        protected set

    var constellationLineList = listOf<ConstellationLineGeometry>()
        protected set

    fun updatePositionList() {
        starGeometryList =
            starCatalog.mapNotNull { it?.run { calculateStarPosition(dec, ra, magnitude) } }

        constellationLineList = constellationLine.mapNotNull {
            val xy1 = starCatalog[it.first]?.run { convertToXYPositionWithNull(dec, -ra) }
            val xy2 = starCatalog[it.second]?.run { convertToXYPositionWithNull(dec, -ra) }
            if (xy1 != null && xy2 != null) ConstellationLineGeometry(
                xy1.first,
                xy1.second,
                xy2.first,
                xy2.second
            ) else null
        }
    }

    private fun calculateStarPosition(dec: Double, ra: Double, magnitude: Double): StarGeometry? {
        return when {
            magnitude < 6.0 -> {
                convertToXYPositionWithNull(dec, -ra)?.run {
                    val r = min(4.5, 6.0 * 0.65.pow(magnitude)).toFloat()
                    StarGeometry(first, second, r)
                }
            }
            else -> null
        }
    }

    fun convertToXYPositionWithNull(dec: Double, ha: Double): Pair<Float, Float>? {
        val radius = toRadius(dec)
        return when {
            radius < 1.0 -> {
                val angle = toRadiansFromHours(ha)
                (-radius * sin(angle)).toFloat() to (-radius * cos(angle)).toFloat()
            }
            else -> null
        }
    }

    fun convertToXYPosition(dec: Double, ha: Double): Pair<Float, Float> {
        val radius = min(toRadius(dec), 1.0)
        val angle = toRadiansFromHours(ha)
        return (-radius * sin(angle)).toFloat() to (-radius * cos(angle)).toFloat()
    }

    fun getXYPositionOnOuterCircle(degree: Double): Pair<Float, Float> {
        val radian = toRadiansFromDegrees(degree)
        return cos(radian).toFloat() to sin(radian.toFloat())
    }

    private fun convertToEquatorialFromEcliptic(eclipticLongitude: Double): Pair<Double, Double> {
        val obliquity = 23.44
        val eLong = Math.toRadians(eclipticLongitude)
        val dec = asin(sin(Math.toRadians(obliquity)) * sin(eLong))
        val ra = acos(cos(eLong) / cos(dec)).let { if (eLong > PI) 2.0 * PI - it else it }
        return Math.toDegrees(dec) to Math.toDegrees(ra)
    }

    fun loadStarCatalog(context: Context) {
        if (starCatalog.any { it != null }) return

        val assetManager: AssetManager = context.resources.assets
        var fileReader: BufferedReader? = null
        var count = 0

        try {
            var line: String?

            val inputStream: InputStream = assetManager.open(hipLiteMajorFile)
            val inputStreamReader = InputStreamReader(inputStream)
            fileReader = BufferedReader(inputStreamReader)

            // Read CSV header
            fileReader.readLine()

            // add position-0 data
            starCatalog.add(null)

            // Read the file line by line starting from the second line
            line = fileReader.readLine()
            while (line != null) {
                val tokens = line.split(",").map { it.trim() }
                if (tokens.isNotEmpty()) {
                    val star = StarParameters(
                        tokens[0],
                        tokens[1],
                        tokens[2],
                        tokens[3],
                        tokens[4],
                        tokens[5],
                        tokens[6],
                        tokens[7],
                        tokens[8]
                    )

                    for (i in starCatalog.size until star.hipNumber + 1) {
                        starCatalog.add(null)
                    }
                    starCatalog[star.hipNumber] = star
                }

                count++
                line = fileReader.readLine()
            }
        } catch (e: Exception) {
            println("Reading CSV Error!")
            e.printStackTrace()
        } finally {
            try {
                fileReader!!.close()
            } catch (e: IOException) {
                println("Closing fileReader Error!")
                e.printStackTrace()
            }
        }
    }

    fun loadConstellationLine(context: Context) {
        if (constellationLine.isNotEmpty()) return

        val assetManager: AssetManager = context.resources.assets
        var fileReader: BufferedReader? = null

        try {
            var line: String?

            val inputStream: InputStream = assetManager.open(constellationLineFile)
            val inputStreamReader = InputStreamReader(inputStream)
            fileReader = BufferedReader(inputStreamReader)

            // add position-0 data
            starCatalog.add(null)

            // Read the file line by line starting from the second line
            line = fileReader.readLine()
            while (line != null) {
                val tokens = line.split(",").map { it.trim() }
                if (tokens.isNotEmpty()) {
                    val star1 = tokens[1].toInt()
                    val star2 = tokens[2].toInt()
                    constellationLine.add(star1 to star2)
                }

                line = fileReader.readLine()
            }
        } catch (e: IOException) {
            println("Reading CSV Error!")
            e.printStackTrace()
        } finally {
            try {
                fileReader!!.close()
            } catch (e: IOException) {
                println("Closing fileReader Error!")
                e.printStackTrace()
            }
        }
    }
}