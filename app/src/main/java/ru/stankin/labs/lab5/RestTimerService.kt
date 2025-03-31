package ru.stankin.labs.lab5

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import androidx.core.app.NotificationCompat
import android.app.Service
import android.content.Context
import android.os.IBinder

class RestTimerService : Service() {

    private var timer: CountDownTimer? = null
    private var remainingTime = 15 * 60 * 1000L
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    // Уникальный идентификатор канала уведомлений
    private val notificationChannelId = "rest_timer_channel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startTimer()
        showNotification()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Rest Timer Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startTimer() {
        timer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                updateNotification()
            }

            override fun onFinish() {
                stopSelf()
                cancelNotification()
            }
        }.start()
    }

    @SuppressLint("ForegroundServiceType")
    private fun showNotification() {
        val prolongIntent = Intent(this, RestTimerService::class.java).apply {
            action = ACTION_PROLONG
        }
        val finishIntent = Intent(this, RestTimerService::class.java).apply {
            action = ACTION_FINISH
        }

        val prolongPendingIntent = PendingIntent.getService(this, 0, prolongIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val finishPendingIntent = PendingIntent.getService(this, 1, finishIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Таймер отдыха")
            .setContentText("Осталось времени: ${formatTime(remainingTime)}")
            .setSmallIcon(R.drawable.ic_timer)
            .setOngoing(true)
            .addAction(R.drawable.ic_prolong, "Продлить", prolongPendingIntent)
            .addAction(R.drawable.ic_finish, "Завершить", finishPendingIntent)
            .build()

        startForeground(1, notification)
    }

    private fun updateNotification() {
        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Таймер отдыха")
            .setContentText("Осталось времени: ${formatTime(remainingTime)}")
            .setSmallIcon(R.drawable.ic_timer)
            .setOngoing(true)
            .addAction(R.drawable.ic_prolong, "Продлить", getProlongIntent())
            .addAction(R.drawable.ic_finish, "Завершить", getFinishIntent())
            .build()

        notificationManager.notify(1, notification)
    }

    private fun cancelNotification() {
        notificationManager.cancel(1)
    }

    private fun getProlongIntent(): PendingIntent {
        val prolongIntent = Intent(this, RestTimerService::class.java).apply {
            action = ACTION_PROLONG
        }
        return PendingIntent.getService(this, 0, prolongIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getFinishIntent(): PendingIntent {
        val finishIntent = Intent(this, RestTimerService::class.java).apply {
            action = ACTION_FINISH
        }
        return PendingIntent.getService(this, 1, finishIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ACTION_PROLONG -> prolongTimer()
            ACTION_FINISH -> stopTimerAndService()
        }
        return START_NOT_STICKY
    }

    private fun prolongTimer() {
        remainingTime += 5 * 60 * 1000
        timer?.cancel()
        startTimer()
    }

    private fun stopTimerAndService() {

        timer?.cancel()
        stopSelf()
        cancelNotification()
    }

    private fun formatTime(timeInMillis: Long): String {
        val hours = (timeInMillis / 1000) / 3600
        val minutes = (timeInMillis / 1000 % 3600) / 60
        val seconds = (timeInMillis / 1000 % 60)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val ACTION_PROLONG = "action_prolong"
        const val ACTION_FINISH = "action_finish"
    }
}
