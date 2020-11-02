/*
 * LocalTime.kt
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

import java.beans.PropertyChangeSupport
import java.time.*
import kotlin.math.floor

data class DateObject(
    val dayOfYear: Int,
    val dayOfMonth: Int,
    val monthString: String,
    val isToday: Boolean,
    val isThisMonth: Boolean
)

class LocalTime {
    private var currentDayOfYear = 0
    var hour = 0
    var minute = 0
    var second = 0
    var dateList = listOf<DateObject>()
    var offset = 0f
        private set

    var longitude: Double = 0.0
        set(value) {
            field = value
            offset = ((currentTime.offset.totalSeconds / 3600.0 / 24.0 -
                    getGmst(ut1.year, 1, 1, 0)) * 360.0 - value).toFloat()

        }

    private var currentTime = ZonedDateTime.now()

    private val dut1: Duration = Duration.ofNanos(-243 * 1000000)  // 2020-06-25

    private val utc: LocalDateTime
        get() = LocalDateTime.ofInstant(currentTime.toInstant(), ZoneOffset.UTC)

    private val ut1: LocalDateTime
        get() = utc + this.dut1

    private val elapsedSeconds
        get() = Duration.ofHours(ut1.hour.toLong()) +
                Duration.ofMinutes(ut1.minute.toLong()) +
                Duration.ofSeconds(ut1.second.toLong()) +
                Duration.ofNanos(ut1.nano.toLong())

    /**
     * Local mean sidereal time as angle
     */
    val siderealAngle: Float
        get() = (getGmst(
            ut1.year,
            ut1.monthValue,
            ut1.dayOfMonth,
            elapsedSeconds.seconds
        ) * 360.0 + longitude).toFloat()

    /**
     * Local mean time as angle
     */
    val localAngle: Float
        get() = ((ut1.hour + 12 + (ut1.minute + ut1.second / 60.0) / 60.0) / 24.0 * 360.0 + longitude).toFloat()

    /**
     * current Julian centuries
     */
    val jc: Double
        get() = getJc(ut1.year, ut1.monthValue, ut1.dayOfMonth, elapsedSeconds.seconds)

    private val pcs = PropertyChangeSupport(this)

    init {
        setCurrentTime()
    }

    fun setCurrentTime() {
        val previous = currentDayOfYear
        currentTime = ZonedDateTime.now().also {
            hour = it.hour
            minute = it.minute
            second = it.second
            setDate(it)
        }

        if (previous != currentDayOfYear) pcs.firePropertyChange("dateChange", null, this)

        pcs.firePropertyChange("localTime", null, this)
    }

    private fun setDate(currentTime: ZonedDateTime) {
        val current = currentTime.dayOfYear
        if (currentDayOfYear != current) {
            currentDayOfYear = current
            val monthList = List(12) { Month.of(it + 1).toString() }
            dateList = List(currentTime.toLocalDate().lengthOfYear()) {
                val date = LocalDate.ofYearDay(currentTime.year, it + 1)
                DateObject(
                    it + 1,
                    date.dayOfMonth,
                    monthList[date.monthValue - 1],
                    it + 1 == currentTime.dayOfYear,
                    date.monthValue == currentTime.monthValue
                )
            }
        }
    }

    companion object {
        /**
         * Julian day at UT1=0
         */
        private fun getJdAt0(year: Int, monthValue: Int, dayOfMonth: Int): Double =
            floor(365.25 * (year - floor((12 - monthValue) / 10.0))) +
                    floor((year - floor((12 - monthValue) / 10.0)) / 400) -
                    floor((year - floor((12 - monthValue) / 10.0)) / 100) +
                    floor(30.59 * (monthValue + floor((12 - monthValue) / 10.0) * 12 - 2)) +
                    dayOfMonth +
                    1721088.5

        /**
         *  Julian centuries at UT1=0
         */
        private fun getJcAt0(year: Int, monthValue: Int, dayOfMonth: Int): Double =
            (getJdAt0(year, monthValue, dayOfMonth) - 2451545.0) / 36525.0

        /**
         * Greenwich Mean Sidereal Time (GMST)
         */
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

        /**
         * Julian day
         */
        private fun getJd(
            year: Int,
            monthValue: Int,
            dayOfMonth: Int,
            elapsedSeconds: Long
        ): Double =
            getJdAt0(year, monthValue, dayOfMonth) + elapsedSeconds / 86400.0

        /**
         *  Julian centuries
         */
        fun getJc(
            year: Int,
            monthValue: Int,
            dayOfMonth: Int,
            elapsedSeconds: Long
        ): Double =
            (getJd(year, monthValue, dayOfMonth, elapsedSeconds) - 2451545.0) / 36525.0
    }
}