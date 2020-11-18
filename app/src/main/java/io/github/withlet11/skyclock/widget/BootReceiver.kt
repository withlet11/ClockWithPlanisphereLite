package io.github.withlet11.skyclock.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                if (context != null) {
                    val ids = appWidgetManager.getAppWidgetIds(
                        ComponentName(context, SkyClockWidget::class.java)
                    )
                    println("BootReceiver")
                    if (ids.isNotEmpty()) SkyClockWidget.scheduleUpdate(context)
                    // println("BootReceiver now")
                    // context.startService(Intent(context, SkyClockWidget.UpdateService::class.java))
                    // }
                }
            }
        }
    }
}
