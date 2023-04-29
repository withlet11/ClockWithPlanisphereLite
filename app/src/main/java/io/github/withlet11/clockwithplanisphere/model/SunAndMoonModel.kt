/*
 * SunAndMoonModel.kt
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

package io.github.withlet11.clockwithplanispherelite.model

import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import kotlin.math.*

/** This class calculates the position of the Sun, and provide geometries of analemma. */
class SunAndMoonModel(private val skyModel: AbstractSkyModel) {
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
     * Calculates the position of the Sun.
     * @param jc Julian centuries
     */
    fun getSunPosition(jc: Double): Pair<Pair<Float, Float>, Double> {
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
        val eccentricAnomaly = meanAnomalyToEccentricAnomaly(eccentricity, meanAnomaly)
        val trueAnomaly = eccentricAnomalyToTrueAnomaly(eccentricity, eccentricAnomaly)
        val argumentOfLatitude = trueAnomaly + longitudeOfPerihelion
        val rightAscension =
            reducedLatitudeToGeocentricLatitude(cos(inclination), argumentOfLatitude)
        val declination = asin(sin(inclination) * sin(argumentOfLatitude))
        val equationOfTime = differenceOfAnomaly * direction - rightAscension
        return skyModel.convertToXYPosition(
            toDegrees(declination),
            toDegrees(equationOfTime) / 15.0
        ) to toDegrees(argumentOfLatitude) % 360.0
    }

    private fun updateAnalemma() {
        analemmaGeometryList = List(25) { it * 15 }.map { dayOfYear ->
            val jc = SolarAndSiderealTime.getJc(2020, 1, 1, 0) + dayOfYear / 36525.0
            getSunPosition(jc).first
        }
        monthlyPositionList =
            listOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334).map { dayOfYear ->
                val jc = SolarAndSiderealTime.getJc(2020, 1, 1, 0) + dayOfYear / 36525.0
                getSunPosition(jc).first
            }
    }

    /**
     * Calculates the position of the Moon
     * @param jc Julian centuries
     */
    fun getMoonPosition(jc: Double): Pair<Pair<Float, Float>, Double> {
        val t = jc * 100.0
        val inclination = toRadians(23.43658) // axial tilt
        val a = parameterA.sumOf { (p, q, r) -> p * sin(toRadians(q + r * t)) }
        val b = parameterB.sumOf { (p, q, r) -> p * sin(toRadians(q + r * t)) }
        val longitude = toRadians(218.3161 + 4812.67881 * t
                + 6.2887 * sin(toRadians(134.961 + 4771.9886 * t + a))
                + parameterLambda.sumOf { (p, q, r) -> p * sin(toRadians(q + r * t)) })
        val latitude = toRadians(5.1282 * sin(toRadians(93.273 + 4832.0202 * t + b))
                + parameterBeta.sumOf { (p, q, r) -> p * sin(toRadians(q + r * t)) })
        val (declination, rightAscension) = eclipticToEquatorial(latitude, longitude, inclination)
        return skyModel.convertToXYPosition(
            declination,
            -rightAscension
        ) to toDegrees(longitude) % 360.0
    }

    companion object {
        /**
         * Reduced latitude --> geocentric latitude
         * @return geocentric latitude
         */
        private fun reducedLatitudeToGeocentricLatitude(ratio: Double, theta: Double): Double {
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
        private fun meanAnomalyToEccentricAnomaly(e: Double, ma: Double): Double =
            (1..3).fold(ma) { ea, _ -> ma + e * sin(ea) }

        /**
         * Eccentric anomaly --> true anomaly
         * @param e eccentricity
         * @param ea eccentric anomaly
         * @return true anomaly
         */
        private fun eccentricAnomalyToTrueAnomaly(e: Double, ea: Double): Double =
            reducedLatitudeToGeocentricLatitude(sqrt((1.0 + e) / (1.0 - e)), ea / 2.0) * 2.0

        /**
         * Ecliptic coordinate --> Equatorial coordinate
         * @param latitude (radians)
         * @param longitude (radians)
         * @return pair of declination (degrees) and right ascension (hours)
         */
        private fun eclipticToEquatorial(
            latitude: Double,
            longitude: Double,
            inclination: Double
        ): Pair<Double, Double> {
            val rightAscension = toDegrees(
                atan2(
                    -sin(latitude) * sin(inclination) + cos(latitude) * sin(longitude) * cos(
                        inclination
                    ),
                    cos(latitude) * cos(longitude)
                )
            ) / 15.0
            val declination = toDegrees(
                asin(
                    sin(latitude) * cos(inclination) + cos(latitude) * sin(longitude) * sin(
                        inclination
                    )
                )
            )
            return declination to rightAscension
        }

        // parameters
        // http://astronomy.webcrow.jp/astrometry/moon_ecliptic_coordinate.html
        val parameterLambda: List<Triple<Double, Double, Double>> = listOf(
            Triple(1.2740, 100.738, 4133.3536),
            Triple(0.6583, 235.700, 8905.3422),
            Triple(0.2136, 269.926, 9543.9773),
            Triple(0.1856, 177.525, 359.9905),
            Triple(0.1143, 6.546, 9664.0404),
            Triple(0.0588, 214.22, 638.635),
            Triple(0.0572, 103.21, 3773.363),
            Triple(0.0533, 10.66, 13677.331),
            Triple(0.0459, 238.18, 8545.352),
            Triple(0.0410, 137.43, 4411.998),
            Triple(0.0348, 117.84, 4452.671),
            Triple(0.0305, 312.49, 5131.979),
            Triple(0.0153, 130.84, 758.698),
            Triple(0.0125, 141.51, 14436.029),
            Triple(0.0110, 231.59, 4892.052),
            Triple(0.0107, 336.44, 13038.696),
            Triple(0.0100, 44.89, 14315.966),
            Triple(0.0085, 201.5, 8266.71),
            Triple(0.0079, 278.2, 4493.34),
            Triple(0.0068, 53.2, 9265.33),
            Triple(0.0052, 197.2, 319.32),
            Triple(0.0050, 295.4, 4812.66),
            Triple(0.0048, 235.0, 19.34),
            Triple(0.0040, 13.2, 13317.34),
            Triple(0.0040, 145.6, 18449.32),
            Triple(0.0040, 119.5, 1.33),
            Triple(0.0039, 111.3, 17810.68),
            Triple(0.0037, 349.1, 5410.62),
            Triple(0.0027, 272.5, 9183.99),
            Triple(0.0026, 107.2, 13797.39),
            Triple(0.0024, 211.9, 998.63),
            Triple(0.0024, 252.8, 9224.66),
            Triple(0.0022, 240.6, 8185.36),
            Triple(0.0021, 87.5, 9903.97),
            Triple(0.0021, 175.1, 719.98),
            Triple(0.0021, 105.6, 3413.37),
            Triple(0.0020, 55.0, 19.34),
            Triple(0.0018, 4.1, 4013.29),
            Triple(0.0016, 242.2, 18569.38),
            Triple(0.0012, 339.0, 12678.71),
            Triple(0.0011, 276.5, 19208.02),
            Triple(0.0009, 218.0, 8586.0),
            Triple(0.0008, 188.0, 14037.3),
            Triple(0.0008, 204.0, 7906.7),
            Triple(0.0007, 140.0, 4052.0),
            Triple(0.0007, 275.0, 4853.3),
            Triple(0.0007, 216.0, 278.6),
            Triple(0.0006, 128.0, 1118.7),
            Triple(0.0005, 247.0, 22582.7),
            Triple(0.0005, 181.0, 19088.0),
            Triple(0.0005, 114.0, 17450.7),
            Triple(0.0005, 332.0, 5091.3),
            Triple(0.0004, 313.0, 398.7),
            Triple(0.0004, 278.0, 120.1),
            Triple(0.0004, 71.0, 9584.7),
            Triple(0.0004, 20.0, 720.0),
            Triple(0.0003, 83.0, 3814.0),
            Triple(0.0003, 66.0, 3494.7),
            Triple(0.0003, 147.0, 18089.3),
            Triple(0.0003, 311.0, 5492.0),
            Triple(0.0003, 161.0, 40.7),
            Triple(0.0003, 280.0, 23221.3)
        )

        val parameterA: List<Triple<Double, Double, Double>> = listOf(
            Triple(0.0040, 119.5, 1.33),
            Triple(0.0020, 55.0, 19.34),
            Triple(0.0006, 71.0, 0.2),
            Triple(0.0006, 54.0, 19.3)
        )

        val parameterBeta: List<Triple<Double, Double, Double>> = listOf(
            Triple(0.2806, 228.235, 9604.0088),
            Triple(0.2777, 138.311, 60.0316),
            Triple(0.1732, 142.427, 4073.3220),
            Triple(0.0554, 194.01, 8965.374),
            Triple(0.0463, 172.55, 698.667),
            Triple(0.0326, 328.96, 13737.362),
            Triple(0.0172, 3.18, 14375.997),
            Triple(0.0093, 277.4, 8845.31),
            Triple(0.0088, 176.7, 4711.96),
            Triple(0.0082, 144.9, 3713.33),
            Triple(0.0043, 307.6, 5470.66),
            Triple(0.0042, 103.9, 18509.35),
            Triple(0.0034, 319.9, 4433.31),
            Triple(0.0025, 196.5, 8605.38),
            Triple(0.0022, 331.4, 13377.37),
            Triple(0.0021, 170.1, 1058.66),
            Triple(0.0019, 230.7, 9244.02),
            Triple(0.0018, 243.3, 8206.68),
            Triple(0.0018, 270.8, 5192.01),
            Triple(0.0017, 99.8, 14496.06),
            Triple(0.0016, 135.7, 420.02),
            Triple(0.0015, 211.1, 9284.69),
            Triple(0.0015, 45.8, 9964.00),
            Triple(0.0014, 219.2, 299.96),
            Triple(0.0013, 95.8, 4472.03),
            Triple(0.0013, 155.4, 379.35),
            Triple(0.0012, 38.4, 4812.68),
            Triple(0.0012, 148.2, 4851.36),
            Triple(0.0011, 138.3, 19147.99),
            Triple(0.0010, 18.0, 12978.66),
            Triple(0.0008, 70.0, 17870.7),
            Triple(0.0008, 326.0, 9724.1),
            Triple(0.0007, 294.0, 13098.7),
            Triple(0.0006, 224.0, 5590.7),
            Triple(0.0006, 52.0, 13617.3),
            Triple(0.0005, 280.0, 8485.3),
            Triple(0.0005, 239.0, 4193.4),
            Triple(0.0004, 311.0, 9483.9),
            Triple(0.0004, 238.0, 23281.3),
            Triple(0.0004, 81.0, 10242.6),
            Triple(0.0004, 13.0, 9325.4),
            Triple(0.0004, 147.0, 14097.4),
            Triple(0.0003, 205.0, 22642.7),
            Triple(0.0003, 107.0, 18149.4),
            Triple(0.0003, 146.0, 3353.3),
            Triple(0.0003, 234.0, 19268.0)
        )

        val parameterB: List<Triple<Double, Double, Double>> = listOf(
            Triple(0.0267, 234.95, 19.341),
            Triple(0.0043, 322.1, 19.36),
            Triple(0.0040, 119.5, 1.33),
            Triple(0.0020, 55.0, 19.34),
            Triple(0.0005, 307.0, 19.4)
        )
    }
}
