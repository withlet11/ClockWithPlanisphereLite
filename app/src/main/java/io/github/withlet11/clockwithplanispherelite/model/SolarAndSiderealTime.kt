/*
 * SolarAndSiderealTime.kt
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

import java.time.*
import kotlin.math.floor

/**
 * This class provides local mean solar time and local mean sidereal time.
 * @property localDateTime local date and time
 * @property offset angle difference of local mean solar time and UTC
 * @property longitude longitude of the observation site
 * @property siderealAngle local mean sidereal time as angle (degrees), 0 degree is 0 hour angle.
 * @property solarAngle local mean solar time as angle (degrees), 0 degree is 0 hour angle.
 */
class SolarAndSiderealTime {
    var localDateTime: LocalDateTime = LocalDateTime.now()

    var offset = 0f
        private set

    var longitude: Double = 0.0
        set(value) {
            field = value
            offset = ((zonedDateTime.offset.totalSeconds / 3600.0 / 24.0 -
                    getGmst(ut1.year, 1, 1, 0)) * 360.0 - value).toFloat()

        }

    private var zonedDateTime = ZonedDateTime.now()

    private val dut1: Duration = Duration.ofNanos(-243 * 1000000)  // 2020-06-25

    private val utc: LocalDateTime
        get() = LocalDateTime.ofInstant(zonedDateTime.toInstant(), ZoneOffset.UTC)

    private val ut1: LocalDateTime get() = utc + this.dut1

    private val elapsedSeconds get() = Duration.ofNanos(ut1.toLocalTime().toNanoOfDay())

    /** Local mean sidereal time as angle (degrees), 0 degree is 0 hour angle. */
    val siderealAngle: Float
        get() = (getGmst(
            ut1.year,
            ut1.monthValue,
            ut1.dayOfMonth,
            elapsedSeconds.seconds
        ) * 360.0 + longitude).toFloat()

    /** Local mean time as angle (degrees), 0 degree is 0 hour angle. */
    val solarAngle: Float
        get() = ((ut1.toLocalTime().toSecondOfDay() / 3600.0 + 12.0) / 24.0 * 360.0
                + longitude).toFloat()

    /** Current Julian centuries */
    val jc: Double
        get() = getJc(ut1.year, ut1.monthValue, ut1.dayOfMonth, elapsedSeconds.seconds)

    /** Set current time to the properties */
    fun setCurrentTime() {
        zonedDateTime = ZonedDateTime.now()
        localDateTime = zonedDateTime.toLocalDateTime()
    }

    companion object {
        /** Julian day at UT1=0 */
        private fun getJdAt0(year: Int, monthValue: Int, dayOfMonth: Int): Double =
            floor(365.25 * (year - floor((12 - monthValue) / 10.0))) +
                    floor((year - floor((12 - monthValue) / 10.0)) / 400) -
                    floor((year - floor((12 - monthValue) / 10.0)) / 100) +
                    floor(30.59 * (monthValue + floor((12 - monthValue) / 10.0) * 12 - 2)) +
                    dayOfMonth +
                    1721088.5

        /**  Julian centuries at UT1=0 */
        private fun getJcAt0(year: Int, monthValue: Int, dayOfMonth: Int): Double =
            (getJdAt0(year, monthValue, dayOfMonth) - 2451545.0) / 36525.0

        /** Greenwich Mean Sidereal Time (GMST) */
        fun getGmst(year: Int, monthValue: Int, dayOfMonth: Int, elapsedSeconds: Long): Double {
            val c0 = 24110.54841  // https://www.cfa.harvard.edu/~jzhao/times.html
            val c1 = 8640184.812866
            val c2 = 0.093104
            val c3 = 0.0000062
            val t = getJcAt0(year, monthValue, dayOfMonth)
            val solarTimeIntervals = 1.0 + c1 / 36525.0 / 86400.0
            val second: Double = ((c0 + c1 * t +
                    c2 * t * t -
                    c3 * t * t * t) % 86400.0 +
                    solarTimeIntervals * elapsedSeconds) % 86400.0

            return second / 3600.0 / 24.0
        }

        /** Julian day */
        private fun getJd(
            year: Int,
            monthValue: Int,
            dayOfMonth: Int,
            elapsedSeconds: Long
        ): Double =
            getJdAt0(year, monthValue, dayOfMonth) + elapsedSeconds / 86400.0

        /**  Julian centuries */
        fun getJc(
            year: Int,
            monthValue: Int,
            dayOfMonth: Int,
            elapsedSeconds: Long
        ): Double =
            (getJd(year, monthValue, dayOfMonth, elapsedSeconds) - 2451545.0) / 36525.0

    }
}