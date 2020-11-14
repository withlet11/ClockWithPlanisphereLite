/*
 * SolarAndSiderealTime.kt
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

import java.time.*
import kotlin.math.floor
import kotlin.math.truncate

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

    /**
     * Changes date by using solar time with fixed sidereal time.
     * @param rotate changes of solar time as degree
     * Date: change
     * Sidereal time: fix
     */
    fun changeDateWithFixedSiderealTime(rotate: Float) {
        // Calculate the difference of solar angle between the current and the target
        val currentSolarAngle = solarAngle
        val targetSolarAngle = -rotate
        val differenceOfSolarAngle =
            normalizeDegree(targetSolarAngle - currentSolarAngle + 180.0) - 180.0 // -180 to 180 deg

        // Calculate with UT1 because it is easier to calculate without timezone or daylight saving time.
        val currentDateTime = ut1
        val currentDayOfYear = currentDateTime.dayOfYear
        val currentYear = currentDateTime.year
        val currentElapsedSeconds = elapsedSeconds.seconds

        // Calculate the target day of year
        // The precise orbital period is 365.256... days, but 1 rotate is 365 or 366 days on this planisphere.
        val lengthOfYear = Year.of(currentYear).length()
        var targetDayOfYear =
            (currentDayOfYear - truncate(differenceOfSolarAngle / 360.0 * lengthOfYear).toInt()).let { dayOfYear ->
                when {
                    dayOfYear < 1 -> dayOfYear + lengthOfYear
                    dayOfYear > lengthOfYear -> dayOfYear - lengthOfYear
                    else -> dayOfYear
                }
            }

        // Calculate the target elapsed seconds
        // Apply not actual solar time intervals but the time interval derived from the length of year
        val solarTimeIntervals = (lengthOfYear + 1.0) / lengthOfYear
        val solarTimeSeconds = (targetDayOfYear - currentDayOfYear) * 86400.0
        val differenceOfSolarTime =
            (solarTimeSeconds - solarTimeSeconds / solarTimeIntervals).toLong()
        val targetElapsedSeconds = (currentElapsedSeconds - differenceOfSolarTime).let { target ->
            when {
                target < 0 -> {
                    targetDayOfYear -= 1
                    target + 86400
                }
                target >= 86400 -> {
                    targetDayOfYear += 1
                    target - 86400
                }
                else -> {
                    target
                }
            }
        }

        updateLocalTime(currentYear, targetDayOfYear, targetElapsedSeconds)
    }

    /**
     * Changes date by using sidereal time with fixed solar time.
     * @param rotate changes of sidereal time as degree
     * Date: change
     * Solar time: fix
     */
    fun changeDateWithFixedSolarTime(rotate: Float) {
        // Calculate with UT1 because it is easier to calculate without timezone or daylight
        // saving time.
        val currentDateTime = ut1
        val currentYear = currentDateTime.year
        val currentElapsedSeconds = elapsedSeconds.seconds

        // Calculates the target day of year. The precise orbital period is 365.256... days, but
        // 1 rotate is 365 or 366 days on this planisphere.
        val lengthOfYear = Year.of(currentYear).length()
        val targetDayOfYear =
            (normalizeDegree(-rotate.toDouble()) / 360.0 * lengthOfYear).toInt() + 1

        updateLocalTime(currentYear, targetDayOfYear, currentElapsedSeconds)
    }

    /**
     * Changes sidereal time by using solar time with fixed date
     * @param rotate changes of solar time as degree
     * Date: fix
     * Sidereal time: change
     */
    fun changeSiderealTimeWithFixedDate(rotate: Float) {
        // Calculate with UT1 because it is easier to calculate without timezone or daylight
        // saving time.
        val currentDateTime = ut1
        val currentYear = currentDateTime.year
        val currentDayOfYear = currentDateTime.dayOfYear
        val currentElapsedSeconds = elapsedSeconds.seconds

        val differenceOfSeconds = rotate / 360.0 * 86400.0
        val targetElapsedSeconds =
            (currentElapsedSeconds - differenceOfSeconds).toLong().let { seconds ->
                when {
                    seconds < 0 -> seconds + 86400
                    seconds >= 86400 -> seconds - 86400
                    else -> seconds
                }
            }

        updateLocalTime(currentYear, currentDayOfYear, targetElapsedSeconds)
    }

    /**
     *  Sets any year, any day and elapsed seconds to properties. The timezone offset is fixed
     *  with the current one because the date (month-day) ring is fixed with the current timezone
     *  offset.
     */
    private fun updateLocalTime(year: Int, dayOfYear: Int, elapsedSeconds: Long) {
        val timezone = ZonedDateTime.now().zone
        val localDate = LocalDate.ofYearDay(year, dayOfYear)
        val utcTime = LocalTime.ofSecondOfDay(elapsedSeconds)
        zonedDateTime =
            ZonedDateTime.of(localDate, utcTime, ZoneOffset.UTC).withZoneSameInstant(timezone)
        localDateTime = LocalDateTime.of(localDate, zonedDateTime.toLocalTime())
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

        /** Normalizes degrees into the range between 0 to 360 */
        fun normalizeDegree(angle: Double) = (angle % 360.0 + 360.0) % 360.0
    }
}