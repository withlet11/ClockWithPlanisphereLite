/*
 * SouthernSkyModel.kt
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

import java.lang.Math.toRadians
import kotlin.math.PI

class SouthernSkyModel : AbstractSkyModel() {
    override val tenMinuteGridStep = -180f / 72f
    override fun toAngle(declination: Double): Double = 180.0 + declination
    override fun toDeclinationFromPole(angle: Int): Int = -90 + angle
    override fun toRadius(declination: Double): Double = (90.0 + declination) / maxAngle
    override fun toRadiansFromHours(hour: Double): Double = -hour / 12.0 * PI
    override fun toRadiansFromDegrees(degree: Double): Double = -toRadians(degree)

    override fun updatePositionList() {
        super.updatePositionList()
        milkyWayDotList = cwpDao.getSouthMilkyWay().mapNotNull { (_, x, y, argb) ->
            makeMilkyWayDot(x, y, argb)
        }
    }
}
