package com.example.decimaltime.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.decimaltime.MainActivity
import com.example.decimaltime.R
import com.example.decimaltime.time.DecimalTimeFormatter
import kotlinx.coroutines.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DecimalClockService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var updateJob: Job? = null
    private var screenReceiver: ScreenReceiver? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Начинаем foreground немедленно при создании, чтобы система не убила сервис
        startForeground(NOTIFICATION_ID, createNotification(), 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                 ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE 
            } else 0
        )
        registerScreenReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startUpdates()
        return START_STICKY
    }

    private fun startUpdates() {
        if (updateJob?.isActive == true) return
        
        updateJob = scope.launch {
            while (isActive) {
                updateWidgets()
                // 1 decimal second = 0.864 standard seconds = 864ms
                delay(864)
            }
        }
    }

    private fun stopUpdates() {
        updateJob?.cancel()
        updateJob = null
    }

    private fun updateWidgets() {
        val manager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, DecimalClockWidget::class.java)
        val ids = manager.getAppWidgetIds(componentName)
        
        if (ids.isEmpty()) {
            stopSelf()
            return
        }

        val decimalTime = DecimalTimeFormatter.now().toString()

        if (ids.isNotEmpty()) {
            val views = RemoteViews(packageName, R.layout.widget_decimal_clock).apply {
                setTextViewText(R.id.widgetDecimalTime, decimalTime)
                setOnClickPendingIntent(R.id.widgetRoot, getPendingIntent())
            }
            manager.updateAppWidget(ids, views)
        }
    }

    private fun getPendingIntent(): PendingIntent {
        return PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Decimal Clock Service",
                NotificationManager.IMPORTANCE_LOW // Low importance so it doesn't pop up
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Decimal Clock is running")
            .setContentText("Updating widgets...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(getPendingIntent())
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopUpdates()
        unregisterScreenReceiver()
        job.cancel()
    }

    private fun registerScreenReceiver() {
        if (screenReceiver == null) {
            screenReceiver = ScreenReceiver()
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            }
            registerReceiver(screenReceiver, filter)
        }
    }

    private fun unregisterScreenReceiver() {
        screenReceiver?.let {
            unregisterReceiver(it)
            screenReceiver = null
        }
    }

    private inner class ScreenReceiver : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> stopUpdates()
                Intent.ACTION_SCREEN_ON -> startUpdates()
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "DecimalClockChannel"
        private const val NOTIFICATION_ID = 1
    }
}
