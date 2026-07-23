/*
 * SkyViewModel.kt
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

import android.content.Context
import io.github.withlet11.clockwithplanispherelite.model.AbstractSkyModel.*
import java.time.LocalDate
import java.time.LocalTime

class SkyViewModel(
    context: Context,
    private val skyModel: AbstractSkyModel,
    latitude: Double,
    longitude: Double
) {
    private val horizonModel = HorizonModel(skyModel)
    private val sunAndMoonModel = SunAndMoonModel(skyModel)
    private val solarAndSiderealTime = SolarAndSiderealTime()

    var latitude = 0.0
        private set(value) {
            skyModel.latitude = value
            sunAndMoonModel.latitude = value
            horizonModel.latitude = value
            field = value
        }

    var longitude = 0.0
        private set(value) {
            solarAndSiderealTime.longitude = value
            field = value
        }

    val localDate: LocalDate
        get() = solarAndSiderealTime.localDateTime.toLocalDate()

    val localTime: LocalTime
        get() = solarAndSiderealTime.localDateTime.toLocalTime()

    val siderealAngle: Float
        get() = solarAndSiderealTime.siderealAngle

    val solarAngle: Float
        get() = solarAndSiderealTime.solarAngle

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

    val milkyWayDotList: List<MilkyWayDot>
        get() = skyModel.milkyWayDotList

    val milkyWayDotSize: Float
        get() = skyModel.milkyWayDotSize

    val equatorial: List<Pair<Int, Float>>
        get() = skyModel.equatorial

    val ecliptic: List<Pair<Float, Float>>
        get() = skyModel.ecliptic

    val analemma: List<Pair<Float, Float>>
        get() = sunAndMoonModel.analemmaGeometryList

    val monthlySunPositionList: List<Pair<Float, Float>>
        get() = sunAndMoonModel.monthlyPositionList

    val currentSunPosition: Pair<Pair<Float, Float>, Double>
        get() = sunAndMoonModel.getSunPosition(solarAndSiderealTime.jc)

    val currentMoonPosition: Pair<Pair<Float, Float>, Double>
        get() = sunAndMoonModel.getMoonPosition(solarAndSiderealTime.jc)

    val tenMinuteGridStep: Float
        get() = skyModel.tenMinuteGridStep

    val direction: Boolean
        get() = tenMinuteGridStep < 0.0

    init {
        this.latitude = latitude
        this.longitude = longitude

        with(skyModel) {
            loadDatabase(context)
            updatePositionList()
        }
    }

    fun setCurrentTime() {
        solarAndSiderealTime.setCurrentTime()
    }

}
