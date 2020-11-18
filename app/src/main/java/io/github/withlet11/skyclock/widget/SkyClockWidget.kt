package io.github.withlet11.skyclock.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import io.github.withlet11.skyclock.MainActivity
import io.github.withlet11.skyclock.R
import io.github.withlet11.skyclock.model.NorthernSkyModel
import io.github.withlet11.skyclock.model.SkyViewModel
import io.github.withlet11.skyclock.model.SouthernSkyModel


class SkyClockWidget : AppWidgetProvider() {
    companion object {
        const val ACTION_UPDATE = "io.github.withlet11.skyclock.widget.SkyClockWidget.ACTION_UPDATE"
        private const val INTERVAL = 5000L // mill seconds

        fun scheduleUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = getAlarmIntent(context)
            alarmManager.cancel(pendingIntent)
            val triggerAtMills =
                (System.currentTimeMillis() + 1).let { it + INTERVAL - it % INTERVAL }
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

        fun loadPreviousPosition(context: Context): Triple<Double, Double, Boolean> {
            var latitude: Double
            var longitude: Double
            var isSouthernSky: Boolean
            val previous =
                context.getSharedPreferences("observation_position", Context.MODE_PRIVATE)

            try {
                latitude = previous.getFloat("latitude", 0f).toDouble()
                longitude = previous.getFloat("longitude", 0f).toDouble()
                isSouthernSky = previous.getBoolean("isSouthernSky", false)
            } catch (e: ClassCastException) {
                latitude = 0.0
                longitude = 0.0
                isSouthernSky = false
            } finally {
            }

            return Triple(latitude, longitude, isSouthernSky)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        scheduleUpdate(context)

        val remoteViews = RemoteViews(context.packageName, R.layout.widget_clock)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        remoteViews.setOnClickPendingIntent(R.id.launchButton, pendingIntent)
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews)

        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    /** Invokes this when the first widget is created. */
    override fun onEnabled(context: Context) {
        // scheduleUpdate(context)
    }

    /** Invokes this when the last widget is disabled */
    override fun onDisabled(context: Context) {
        clearUpdate(context)
    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == ACTION_UPDATE && context != null) onUpdate(context)
        else super.onReceive(context, intent)
    }

    private fun onUpdate(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisAppWidgetComponentName = ComponentName(context.packageName, javaClass.name)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName)
        onUpdate(context, appWidgetManager, appWidgetIds)
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val (latitude, longitude, isSouthernSky) = SkyClockWidget.loadPreviousPosition(context)
    val skyViewModel =
        SkyViewModel(
            context.applicationContext!!,
            if (isSouthernSky) SouthernSkyModel() else NorthernSkyModel(),
            latitude,
            longitude
        )
    val clockBasePanel = ClockBasePanel(context)
    val skyPanel = SkyPanel(context)
    val sunPanel = SunPanel(context)
    val horizonPanel = HorizonPanel(context)
    val clockHandsPanel = ClockHandsPanel(context)

    with(skyViewModel) {
        clockBasePanel.set(offset, direction)
        skyPanel.set(
            starGeometryList,
            constellationLineList,
            equatorial,
            ecliptic,
            tenMinuteGridStep
        )
        sunPanel.set(
            analemma,
            monthlySunPositionList,
            currentSunPosition,
            tenMinuteGridStep
        )
        horizonPanel.set(horizon, altAzimuth, directionLetters)
    }

    skyViewModel.setCurrentTime()
    clockBasePanel.currentDate = skyViewModel.localDate
    clockHandsPanel.localTime = skyViewModel.localTime
    skyPanel.siderealAngle = skyViewModel.siderealAngle
    sunPanel.solarAngle = skyViewModel.solarAngle

    // Draws panels
    clockBasePanel.draw()
    skyPanel.draw()
    sunPanel.draw()
    horizonPanel.draw()
    clockHandsPanel.draw()

    // Constructs the RemoteViews object
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_clock)

    // Adds panels to views
    remoteViews.setImageViewBitmap(R.id.widgetClockBasePanel, clockBasePanel.bmp)
    remoteViews.setImageViewBitmap(R.id.widgetSkyPanel, skyPanel.bmp)
    remoteViews.setImageViewBitmap(R.id.widgetSunPanel, sunPanel.bmp)
    remoteViews.setImageViewBitmap(R.id.widgetHorizonPanel, horizonPanel.bmp)
    remoteViews.setImageViewBitmap(R.id.widgetClockHandsPanel, clockHandsPanel.bmp)

    // Instructs the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
}
