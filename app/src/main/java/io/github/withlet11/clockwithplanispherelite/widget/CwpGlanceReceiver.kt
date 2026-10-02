/*
 * CwpGlanceReceiver.kt
 *
 * Copyright 2026 Yasuhiro Yamakawa <withlet11@gmail.com>
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

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.github.withlet11.clockwithplanispherelite.MainActivity

class CwpGlanceReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CwpGlanceWidget()

    companion object {
        const val ACTION_UPDATE =
            "io.github.withlet11.clockwithplanispherelite.widget.CwpGlanceReceiver.ACTION_UPDATE"
        const val PARTIAL_UPDATE_INTERVAL = 10000L // mill seconds
        const val FULL_UPDATE_INTERVAL = 60000L // mill seconds

        fun scheduleUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = getAlarmIntent(context)
            alarmManager.cancel(pendingIntent)
            val triggerAtMills =
                (System.currentTimeMillis() + 1).let { it + PARTIAL_UPDATE_INTERVAL - it % PARTIAL_UPDATE_INTERVAL }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.set(AlarmManager.RTC, triggerAtMills, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC, triggerAtMills, pendingIntent)
            }
        }

        private fun getAlarmIntent(context: Context): PendingIntent {
            val intent = Intent(context, CwpGlanceReceiver::class.java)
            intent.action = ACTION_UPDATE
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
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
            } catch (_: ClassCastException) {
                latitude = 0.0
                longitude = 0.0
                isSouthernSky = false
                isClockHandsVisible = true
            }

            return (latitude to longitude) to (isSouthernSky to isClockHandsVisible)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleUpdate(context)
    }

    override fun onDisabled(context: Context) {
        clearUpdate(context)
        super.onDisabled(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        scheduleUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_UPDATE -> {
                scheduleUpdate(context)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        CwpGlanceWidget().updateAll(context)
                    } catch (e: Exception) {
                        Log.e("CwpGlanceReceiver", "Widget update failed", e)
                    }
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                scheduleUpdate(context)
            }
        }
    }
}
