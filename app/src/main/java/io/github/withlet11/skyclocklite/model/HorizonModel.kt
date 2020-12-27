/*
 * HorizonModel.kt
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

package io.github.withlet11.skyclocklite.model

import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import kotlin.math.*

class HorizonModel(private val skyModel: AbstractSkyModel) {
    private var maxAngle = 0.0

    var latitude = 0.0
        set(value) {
            field = value
            maxAngle = min(skyModel.toAngle(value) + 10.0, AbstractSkyModel.ANGLE_LIMIT)
            updateHorizon()
        }

    var horizon = listOf<Pair<Float, Float>>()
    var altAzimuth = listOf<List<Pair<Float, Float>?>>()
    var directionLetters = listOf<Triple<String, Float, Float>>()

    private fun updateHorizon() {
        horizon = when {
            latitude == 0.0 -> {
                List(181) { // 0 to 180
                    val altitude = toRadians(it - 90.0)
                    sin(altitude).toFloat() to cos(altitude).toFloat()
                }.plus(listOf(-1f to 0f))
            }
            skyModel.toAngle(latitude) < 180.0 -> {
                List(361) { // 0 to 360 deg
                    val hourAngle = it / 15.0
                    val declination = calculateHorizon(hourAngle)
                    skyModel.convertToXYPosition(declination, hourAngle)
                }.plus(List(361) { // 0 to 360 deg
                    skyModel.getXYPositionOnOuterCircle(it.toDouble())
                })
            }
            else -> {
                List(361) { // 0 to 360 deg
                    val hourAngle = it / 15.0
                    val declination = calculateHorizon(hourAngle)
                    skyModel.convertToXYPosition(declination, hourAngle)
                }
            }
        }

        altAzimuth = listOf(
            createHorizonLine(),
            createAltitudeLine(-18.0), // astronomical twilight
            createAltitudeLine(30.0),
            createAltitudeLine(60.0),
            createMeridian(true), // upper meridian
            createMeridian(false), // lower meridian
            createAzimuthLine(45.0),
            createAzimuthLine(90.0),
            createAzimuthLine(135.0),
            createAzimuthLine(225.0),
            createAzimuthLine(270.0),
            createAzimuthLine(315.0)
        )

        directionLetters = listOf("N", "E", "S", "W").mapIndexed { index, text ->
            val azimuth = index * 90.0
            val visibility =
                convertToEquatorialFromHorizontal(azimuth, 0.0).let { (dec, ha) ->
                    skyModel.convertToXYPositionWithNull(dec, ha)
                }

            visibility?.run {
                convertToEquatorialFromHorizontal(azimuth, -5.0).let { (dec, ha) ->
                    skyModel.convertToXYPosition(dec, ha).let { (x, y) ->
                        Triple(text, x, y)
                    }
                }
            }
        }.filterNotNull()
    }

    private fun createAltitudeLine(altitude: Double): List<Pair<Float, Float>?> =
        List(361) { // 0 to 360 deg
            val azimuth = it.toDouble()
            convertToEquatorialFromHorizontal(azimuth, altitude).let { (declination, hourAngle) ->
                skyModel.convertToXYPositionWithNull(declination, hourAngle)
            }
        }

    private fun createHorizonLine(): List<Pair<Float, Float>?> =
        if (latitude == 0.0) {
            listOf(1f to 0f, -1f to 0f)
        } else {
            List(361) { // 0 to 360 deg
                val hourAngle = it / 15.0
                val declination = calculateHorizon(hourAngle)
                skyModel.convertToXYPositionWithNull(declination, hourAngle)
            }
        }

    private fun calculateHorizon(hourAngle: Double): Double =
        toDegrees(atan(-cos(hourAngle / 12.0 * PI) / tan(toRadians(latitude))))

    private fun createAzimuthLine(azimuth: Double): List<Pair<Float, Float>?> =
        List(91) { // 0 to 90 deg
            val altitude = it.toDouble()
            convertToEquatorialFromHorizontal(azimuth, altitude).let { (dec, ha) ->
                skyModel.convertToXYPositionWithNull(dec, ha)
            }
        }


    private fun createMeridian(isUpper: Boolean): List<Pair<Float, Float>> {
        val hourAngle = if (isUpper) 0.0 else 12.0
        val poleNearZenith = skyModel.toDeclinationFromPole(0).toDouble()
        val poleNearHorizon: Double
        val horizon00h: Double

        if (latitude > 0.0) {
            poleNearHorizon = if (poleNearZenith > 0.0) poleNearZenith else -90.0 + maxAngle
            horizon00h = if (isUpper) -90 + latitude else 90.0 - latitude
        } else {
            poleNearHorizon = if (poleNearZenith < 0.0) poleNearZenith else 90.0 - maxAngle
            horizon00h = if (isUpper) 90.0 + latitude else -90.0 - latitude
        }

        return listOf(
            skyModel.convertToXYPosition(poleNearHorizon, hourAngle),
            skyModel.convertToXYPosition(horizon00h, hourAngle)
        )
    }

    private fun convertToEquatorialFromHorizontal(
        azimuth: Double,
        altitude: Double
    ): Pair<Double, Double> {
        val lat = toRadians(latitude)
        val (cosLat, sinLat) = cos(lat) to sin(lat)
        val alt = toRadians(altitude)
        val (cosAlt, sinAlt) = cos(alt) to sin(alt)
        val az = toRadians(azimuth)
        val sinAz = sin(az)
        val dec = asin(sinLat * sinAlt + cosLat * cosAlt * cos(az))
        val (cosDec, sinDec) = cos(dec) to sin(dec)

        val ha = if (dec != 0.0) {
            (-asin(cosAlt * sinAz / cosDec)).let {
                if ((sinAlt - sinDec * sinLat) / cosDec / cosLat > 0.0) it else PI - it
            }
        } else {
            0.0
        }

        return toDegrees(dec) to ha / PI * 12.0
    }
}
