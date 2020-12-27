/*
 * SkyClockWidget.kt
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

package io.github.withlet11.skyclocklite.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import io.github.withlet11.skyclocklite.MainActivity
import io.github.withlet11.skyclocklite.R
import io.github.withlet11.skyclocklite.model.NorthernSkyModel
import io.github.withlet11.skyclocklite.model.SkyViewModel
import io.github.withlet11.skyclocklite.model.SouthernSkyModel
import java.time.LocalTime


class SkyClockWidget : AppWidgetProvider() {
    companion object {
        const val ACTION_UPDATE =
            "io.github.withlet11.skyclocklite.widget.SkyClockWidget.ACTION_UPDATE"
        const val PARTIAL_UPDATE_INTERVAL = 5000L // mill seconds
        const val FULL_UPDATE_INTERVAL = 60000L // mill seconds

        fun scheduleUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = getAlarmIntent(context)
            alarmManager.cancel(pendingIntent)
            val triggerAtMills =
                (System.currentTimeMillis() + 1).let { it + PARTIAL_UPDATE_INTERVAL - it % PARTIAL_UPDATE_INTERVAL }
            alarmManager.setExact(
                AlarmManager.RTC, triggerAtMills, pendingIntent
            )
        }

        private fun getAlarmIntent(context: Context): PendingIntent {
            val intent = Intent(context, SkyClockWidget::class.java)
            intent.action = ACTION_UPDATE
            return PendingIntent.getBroadcast(context, 0, intent, 0)
        }

        fun clearUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(getAlarmIntent(context))
        }

        fun loadPreviousPosition(context: Context): Pair<Pair<Double, Double>, Pair<Boolean, Boolean>> {
            var latitude: Double
            var longitude: Double
            var isSouthernSky: Boolean
            var isClockHandsVisible: Boolean
            val previous =
                context.getSharedPreferences(
                    MainActivity.OBSERVATION_POSITION,
                    Context.MODE_PRIVATE
                )

            try {
                latitude = previous.getFloat(MainActivity.LATITUDE, 0f).toDouble()
                longitude = previous.getFloat(MainActivity.LONGITUDE, 0f).toDouble()
                isSouthernSky = previous.getBoolean(MainActivity.IS_SOUTHERN_SKY, false)
                isClockHandsVisible = previous.getBoolean(MainActivity.IS_CLOCK_HANDS_VISIBLE, true)
            } catch (e: ClassCastException) {
                latitude = 0.0
                longitude = 0.0
                isSouthernSky = false
                isClockHandsVisible = true
            } finally {
            }

            return (latitude to longitude) to (isSouthernSky to isClockHandsVisible)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        scheduleUpdate(context!!)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateAppWidget(context, appWidgetManager, appWidgetIds, true)
        scheduleUpdate(context)
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    /** Invokes this when the first widget is created. */
    override fun onEnabled(context: Context) {
    }

    /** Invokes this when the last widget is disabled */
    override fun onDisabled(context: Context) {
        clearUpdate(context)
    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (context == null) super.onReceive(context, intent)
        else when (intent.action) {
            ACTION_UPDATE -> onUpdate(context)
            Intent.ACTION_BOOT_COMPLETED -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val ids = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, SkyClockWidget::class.java)
                )
                if (ids.isNotEmpty()) scheduleUpdate(context)
                super.onReceive(context, intent)
            }
            else -> super.onReceive(context, intent)
        }
    }

    private fun onUpdate(context: Context) {
        scheduleUpdate(context)

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisAppWidgetComponentName = ComponentName(context.packageName, javaClass.name)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName)
        val shouldUpdateFull =
            System.currentTimeMillis() % FULL_UPDATE_INTERVAL < PARTIAL_UPDATE_INTERVAL + 100
        updateAppWidget(context, appWidgetManager, appWidgetIds, shouldUpdateFull)
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
    shouldUpdateFull: Boolean
) {
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_clock)
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
    remoteViews.setOnClickPendingIntent(R.id.launchButton, pendingIntent)
    appWidgetManager.updateAppWidget(appWidgetIds, remoteViews)

    // There may be multiple widgets active, so update all of them
    for (appWidgetId in appWidgetIds) {
        if (shouldUpdateFull) updateAppWidget(context, appWidgetManager, appWidgetId)
        else updateAppWidgetPartially(context, appWidgetManager, appWidgetId)
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val (location, mode) = SkyClockWidget.loadPreviousPosition(context)
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

    // Constructs the RemoteViews object
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_clock)

    // Adds panels to views
    remoteViews.setImageViewBitmap(R.id.widgetClockBasePanel, clockBasePanel.bmp)
    remoteViews.setImageViewBitmap(R.id.widgetSkyPanel, skyPanel.bmp)
    remoteViews.setImageViewBitmap(R.id.widgetSunAndMoonPanel, sunAndMoonPanel.bmp)
    remoteViews.setImageViewBitmap(R.id.widgetHorizonPanel, horizonPanel.bmp)
    clockHandsPanel?.let { remoteViews.setImageViewBitmap(R.id.widgetClockHandsPanel, it.bmp) }

    // Instructs the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
}

internal fun updateAppWidgetPartially(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val isClockHandsVisible = SkyClockWidget.loadPreviousPosition(context).second.second
    val clockHandsPanel = ClockHandsPanel(context)
    if (isClockHandsVisible) {
        clockHandsPanel.localTime = LocalTime.now()
        clockHandsPanel.draw()
    }
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_clock)
    remoteViews.setImageViewBitmap(R.id.widgetClockHandsPanel, clockHandsPanel.bmp)
    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, remoteViews)
}
