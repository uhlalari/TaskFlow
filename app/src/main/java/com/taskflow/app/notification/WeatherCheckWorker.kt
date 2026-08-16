package com.taskflow.app.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.taskflow.app.R
import com.taskflow.app.domain.model.WeatherAlert
import com.taskflow.app.domain.usecase.CheckWeatherAlertsUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

class WeatherCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val checkWeatherAlertsUseCase: CheckWeatherAlertsUseCase by inject()

    override suspend fun doWork(): Result {
        val alerts = runCatching { checkWeatherAlertsUseCase() }.getOrDefault(emptyList())
        if (alerts.isNotEmpty()) {
            createNotificationChannelIfNeeded()
            showNotification(alerts)
        }
        return Result.success()
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            applicationContext.getString(R.string.weather_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = applicationContext.getString(R.string.weather_notification_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    private fun showNotification(alerts: List<WeatherAlert>) {
        val hasPermission = ActivityCompat.checkSelfPermission(
            applicationContext, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) return

        val messages = alerts.map { it.toMessage(applicationContext) }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_task_notification)
            .setContentTitle(applicationContext.getString(R.string.weather_notification_title))
            .setContentText(messages.first())
            .setStyle(NotificationCompat.BigTextStyle().bigText(messages.joinToString("\n")))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompatWrapper.notify(applicationContext, NOTIFICATION_ID, notification)
    }

    private fun WeatherAlert.toMessage(context: Context): String = when (this) {
        is WeatherAlert.HeavyRain -> context.getString(
            R.string.weather_alert_heavy_rain,
            date.format(DATE_FORMATTER),
            probabilityPercent
        )

        is WeatherAlert.LowHumidity -> context.getString(
            R.string.weather_alert_low_humidity,
            date.format(DATE_FORMATTER),
            humidityPercent
        )

        is WeatherAlert.TemperatureSwing -> context.getString(
            R.string.weather_alert_temperature_swing,
            fromDate.format(DATE_FORMATTER),
            toDate.format(DATE_FORMATTER),
            deltaCelsius.roundToInt()
        )
    }

    companion object {
        const val CHANNEL_ID = "weather_alerts"
        private const val NOTIFICATION_ID = 9001
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM", Locale("pt", "BR"))
    }
}
