package com.example.decimaltime.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build

class DecimalClockWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        startService(context)
    }

    override fun onEnabled(context: Context) {
        startService(context)
    }

    override fun onDisabled(context: Context) {
        context.stopService(Intent(context, DecimalClockService::class.java))
    }

    private fun startService(context: Context) {
        val intent = Intent(context, DecimalClockService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Для виджетов часов фоновый сервис ограничен в Android 8+, 
            // но так как мы обновляем UI, система часто дает послабления, 
            // или нужно использовать startForegroundService и показывать уведомление.
            // Однако для простого примера часов часто используют просто startService пока приложение не убито,
            // или JobScheduler.
            // В данном случае, самый надежный способ без Foreground Notification - AlarmManager (как было),
            // но пользователь жалуется на лаги.
            // Попробуем Service. Если система убьет его - виджет остановится.
            try {
                context.startService(intent)
            } catch (e: IllegalStateException) {
                // Если приложение в фоне и нельзя запустить сервис - ничего не поделаешь без foreground
            }
        } else {
            context.startService(intent)
        }
    }
}
