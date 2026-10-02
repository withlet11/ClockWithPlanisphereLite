/*
 * CwpGlanceWidget.kt
 *
 * Copyright 2020-2026 Yasuhiro Yamakawa <withlet11@gmail.com>
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

package io.github.withlet11.clockwithplanispherelite.widget

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size
import io.github.withlet11.clockwithplanispherelite.MainActivity
import io.github.withlet11.clockwithplanispherelite.model.NorthernSkyModel
import io.github.withlet11.clockwithplanispherelite.model.SkyViewModel
import io.github.withlet11.clockwithplanispherelite.model.SouthernSkyModel

class CwpGlanceWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val (location, mode) = CwpGlanceReceiver.loadPreviousPosition(context)
        val (latitude, longitude) = location
        val (isSouthernSky, isClockHandsVisible) = mode
        val skyViewModel =
            SkyViewModel(
                context,
                if (isSouthernSky) SouthernSkyModel() else NorthernSkyModel(),
                latitude,
                longitude
            )
        val clockBasePanel = ClockBasePanel(context)
        val skyPanel = SkyPanel(context)
        val sunAndMoonPanel = SunAndMoonPanel(context)
        val horizonPanel = HorizonPanel(context)
        val clockHandsPanel = if (isClockHandsVisible) ClockHandsPanel(context) else null

        with(skyViewModel) {
            clockBasePanel.set(offset, direction)
            skyPanel.set(
                starGeometryList,
                constellationLineList,
                milkyWayDotList,
                milkyWayDotSize,
                equatorial,
                ecliptic,
                tenMinuteGridStep
            )
            sunAndMoonPanel.set(
                analemma,
                monthlySunPositionList,
                currentSunPosition,
                currentMoonPosition,
                tenMinuteGridStep
            )

            horizonPanel.set(horizon, altAzimuth, directionLetters)
        }

        skyViewModel.setCurrentTime()
        clockBasePanel.currentDate = skyViewModel.localDate
        clockHandsPanel?.localTime = skyViewModel.localTime
        skyPanel.siderealAngle = skyViewModel.siderealAngle
        sunAndMoonPanel.solarAngle = skyViewModel.solarAngle
        sunAndMoonPanel.siderealAngle = skyViewModel.siderealAngle

        // Draws panels
        clockBasePanel.draw()
        skyPanel.draw()
        sunAndMoonPanel.draw()
        horizonPanel.draw()
        clockHandsPanel?.draw()

        val baseBmp = clockBasePanel.bmp
        val skyBmp = skyPanel.bmp
        val sunMoonBmp = sunAndMoonPanel.bmp
        val horizonBmp = horizonPanel.bmp
        val handsBmp = clockHandsPanel?.bmp

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(
                        actionStartActivity(
                            ComponentName(
                                context,
                                MainActivity::class.java
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(baseBmp),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                Image(
                    provider = ImageProvider(skyBmp),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                Image(
                    provider = ImageProvider(sunMoonBmp),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                Image(
                    provider = ImageProvider(horizonBmp),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                if (handsBmp != null) {
                    Image(
                        provider = ImageProvider(handsBmp),
                        contentDescription = null,
                        modifier = GlanceModifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
