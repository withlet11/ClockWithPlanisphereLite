/*
 * SkyViewModel.kt
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
import kotlin.math.sign

class SkyViewModel(
    context: Context,
    private val skyModel: AbstractSkyModel,
    latitude: Double,
    longitude: Double
) {
    private val horizonModel = HorizonModel(skyModel)
    private val sunModel = SunModel(skyModel)
    private val solarAndSiderealTime = SolarAndSiderealTime()

    var latitude = 0.0
        private set(value) {
            skyModel.latitude = value
            sunModel.latitude = value
            horizonModel.latitude = value
            field = value
        }

    var longitude = 0.0
        private set(value) {
            solarAndSiderealTime.longitude = value
            field = value
        }

    val hour: Int
        get() = solarAndSiderealTime.hour

    val minute: Int
        get() = solarAndSiderealTime.minute

    val second: Int
        get() = solarAndSiderealTime.second

    val siderealAngle: Float
        get() = solarAndSiderealTime.siderealAngle

    val solarAngle: Float
        get() = solarAndSiderealTime.solarAngle

    val dateList: List<DateObject>
        get() = solarAndSiderealTime.dateList

    val offset: Float
        get() = solarAndSiderealTime.offset

    val horizon: List<Pair<Float, Float>>
        get() = horizonModel.horizon

    val altAzimuth: List<List<Pair<Float, Float>?>>
        get() = horizonModel.altAzimuth

    val directionLetters: List<Triple<String, Float, Float>>
        get() = horizonModel.directionLetters

    val starGeometryList: List<StarGeometry>
        get() = skyModel.starGeometryList

    val constellationLineList: List<ConstellationLineGeometry>
        get() = skyModel.constellationLineList

    val equatorial: List<Pair<Int, Float>>
        get() = skyModel.equatorial

    val ecliptic: List<Pair<Float, Float>>
        get() = skyModel.ecliptic

    val analemma: List<Pair<Float, Float>>
        get() = sunModel.analemmaGeometryList

    val monthlySunPositionList: List<Pair<Float, Float>>
        get() = sunModel.monthlyPositionList

    val currentSunPosition: Pair<Float, Float>
        get() = sunModel.getSunPosition(solarAndSiderealTime.jc)

    val tenMinuteGridStep: Float
        get() = skyModel.tenMinuteGridStep

    val direction: Boolean
        get() = tenMinuteGridStep < 0.0

    init {
        this.latitude = latitude
        this.longitude = longitude

        with(skyModel) {
            loadStarCatalog(context)
            loadConstellationLine(context)
            updatePositionList()
        }
    }

    fun setCurrentTime() {
        solarAndSiderealTime.setCurrentTime()
    }

    fun changeLocation(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }

    fun changeDateWithFixedSiderealTime(rotate: Float) {
        solarAndSiderealTime.changeDateWithFixedSiderealTime(rotate * sign(tenMinuteGridStep))
    }

    fun changeDateWithFixedSolarTime(rotate: Float) {
        solarAndSiderealTime.changeDateWithFixedSolarTime(rotate * sign(tenMinuteGridStep))
    }

    fun changeSiderealTimeWithFixedDate(rotate: Float) {
        solarAndSiderealTime.changeSiderealTimeWithFixedDate(rotate * sign(tenMinuteGridStep))
    }
}
