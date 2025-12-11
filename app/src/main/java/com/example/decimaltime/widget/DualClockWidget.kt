package com.example.decimaltime.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build

class DualClockWidget : AppWidgetProvider() {

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
        // Мы используем тот же сервис, что и для одиночного виджета, 
        // поэтому останавливать его нужно только если нет активных виджетов обоих типов.
        // Но для простоты пока оставим управление сервисом на совести системы или доработаем сервис.
        // В данном случае сервис сам проверяет наличие виджетов в updateWidgets()
        // и останавливается, если их нет.
        startService(context) // Чтобы сервис проверил, остались ли виджеты
    }

    private fun startService(context: Context) {
        val intent = Intent(context, DecimalClockService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                context.startService(intent)
            } catch (e: IllegalStateException) {
                // Ignore if in background
            }
        } else {
            context.startService(intent)
        }
    }
}
