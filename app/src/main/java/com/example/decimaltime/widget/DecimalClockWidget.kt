package com.example.decimaltime.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import com.example.decimaltime.MainActivity
import com.example.decimaltime.R
import com.example.decimaltime.time.DecimalTimeFormatter
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DecimalClockWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_DECIMAL_TICK) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, DecimalClockWidget::class.java))
            updateWidgets(context, manager, ids)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        scheduleNextUpdate(context)
    }

    override fun onDisabled(context: Context) {
        cancelUpdates(context)
    }

    private fun updateWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            val decimalTime = DecimalTimeFormatter.now().toString()
            val standardTime = LocalTime.now().format(STANDARD_TIME_FORMAT)
            val views = RemoteViews(context.packageName, R.layout.widget_decimal_clock).apply {
                setTextViewText(R.id.widgetDecimalTime, decimalTime)
                setTextViewText(R.id.widgetStandardTime, standardTime)
                setOnClickPendingIntent(
                    R.id.widgetRoot,
                    PendingIntent.getActivity(
                        context,
                        0,
                        Intent(context, MainActivity::class.java),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }
            appWidgetManager.updateAppWidget(id, views)
        }
        scheduleNextUpdate(context)
    }

    private fun scheduleNextUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = updatePendingIntent(context)
        val triggerAt = SystemClock.elapsedRealtime() + UPDATE_INTERVAL_MS
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAt,
            pendingIntent
        )
    }

    private fun cancelUpdates(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(updatePendingIntent(context))
    }

    private fun updatePendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_UPDATE,
            Intent(context, DecimalClockWidget::class.java).apply {
                action = ACTION_DECIMAL_TICK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    companion object {
        private const val REQUEST_CODE_UPDATE = 2024
        private const val ACTION_DECIMAL_TICK = "com.example.decimaltime.UPDATE_WIDGET"
        private const val UPDATE_INTERVAL_MS = 30_000L
        private val STANDARD_TIME_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("HH:mm")
    }
}
