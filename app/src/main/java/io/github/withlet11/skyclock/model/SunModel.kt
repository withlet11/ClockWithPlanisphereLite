package io.github.withlet11.skyclock.model

import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import kotlin.math.*

/** This class calculates the position of the Sun, and provide geometries of analemma. */
class SunModel(private val skyModel: AbstractSkyModel) {
    private var maxAngle = 0.0

    var latitude = 0.0
        set(value) {
            field = value
            maxAngle = min(skyModel.toAngle(value) + 10.0, AbstractSkyModel.ANGLE_LIMIT)
            updateAnalemma()
        }

    var analemmaGeometryList = listOf<Pair<Float, Float>>()
    var monthlyPositionList = listOf<Pair<Float, Float>>()

    /**
     * Calculates the sun position.
     * @param jc Julian centuries
     */
    fun getSunPosition(jc: Double): Pair<Float, Float> {
        // orbital parameters
        val epoch = SolarAndSiderealTime.getJc(2000, 1, 1, 12 * 60 * 60) // J2000.0
        val eccentricity = 0.0167086 // e
        val inclination = toRadians(23.43658) // axial tilt
        val longitudeOfPerihelion = toRadians(102.9 + 180.0) // ϖ
        val longitudeOfEpoch = toRadians(280.46645683) // λ
        val period = 365.256363004 // P

        // prograde or retrograde
        val direction = if (cos(inclination) < 0.0) -1.0 else 1.0

        val differenceOfAnomaly =
            (jc - epoch) * 36525.0 / period % 1.0 * 2.0 * PI + longitudeOfEpoch
        val meanAnomaly = differenceOfAnomaly - longitudeOfPerihelion
        val eccentricAnomaly = ma2ea(eccentricity, meanAnomaly)
        val trueAnomaly = ea2ta(eccentricity, eccentricAnomaly)
        val argumentOfLatitude = trueAnomaly + longitudeOfPerihelion
        val rightAscension = redLat2GeoLat(cos(inclination), argumentOfLatitude)
        val declination = asin(sin(inclination) * sin(argumentOfLatitude))
        val equationOfTime = differenceOfAnomaly * direction - rightAscension
        return skyModel.convertToXYPosition(
            toDegrees(declination),
            toDegrees(equationOfTime) / 15.0
        )
    }

    /** creates analemma geometry list [analemmaGeometryList] */
    private fun getAnalemma(): List<Pair<Float, Float>> {
        return List(25) { it * 15 }.map { dayOfYear ->
            val jc = SolarAndSiderealTime.getJc(2020, 1, 1, 0) + dayOfYear / 36525.0
            getSunPosition(jc)
        }
    }

    private fun createMonthlyPositionList(): List<Pair<Float, Float>> {
        return listOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334).map { dayOfYear ->
            val jc = SolarAndSiderealTime.getJc(2020, 1, 1, 0) + dayOfYear / 36525.0
            getSunPosition(jc)
        }
    }

    private fun updateAnalemma() {
        analemmaGeometryList = getAnalemma()
        monthlyPositionList = createMonthlyPositionList()
    }

    companion object {
        /**
         * Reduced latitude --> geocentric latitude
         * @return geocentric latitude
         */
        private fun redLat2GeoLat(ratio: Double, theta: Double): Double {
            val (absRatio, absTheta) = if (ratio > 0) ratio to theta else -ratio to -theta
            val (cosTheta, sinTheta) = cos(absTheta) to sin(absTheta)
            return absTheta + atan2(
                (absRatio - 1.0) * sinTheta * cosTheta,
                cosTheta * cosTheta + absRatio * sinTheta * sinTheta
            )
        }

        /**
         * Inverse Kepler equation
         * * fixed-point iteration
         * @param e eccentricity e << 1
         * @param ma mean anomaly
         * @return eccentric anomaly
         */
        private fun ma2ea(e: Double, ma: Double): Double =
            (1..3).fold(ma) { ea, _ -> ma + e * sin(ea) }

        /**
         * Eccentric anomaly --> true anomaly
         * @param e eccentricity
         * @param ea eccentric anomaly
         * @return true anomaly
         */
        private fun ea2ta(e: Double, ea: Double): Double =
            redLat2GeoLat(sqrt((1.0 + e) / (1.0 - e)), ea / 2.0) * 2.0
    }
}
