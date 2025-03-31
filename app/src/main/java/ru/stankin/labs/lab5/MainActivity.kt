package ru.stankin.labs.lab5

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)

        startButton.setOnClickListener {
            // Запускаем сервис
            val serviceIntent = Intent(this, RestTimerService::class.java)
            startService(serviceIntent)

            // Скрываем кнопку старта и показываем кнопку остановки
            startButton.visibility = View.GONE
            stopButton.visibility = View.VISIBLE
        }

        stopButton.setOnClickListener {
            // Останавливаем сервис
            val serviceIntent = Intent(this, RestTimerService::class.java)

            stopTimerAndNotification()

            // Скрываем кнопку остановки и показываем кнопку старта
            stopButton.visibility = View.GONE
            startButton.visibility = View.VISIBLE

        }
    }

    private fun stopTimerAndNotification() {
        // Останавливаем таймер и удаляем уведомление, отправляя команду в сервис
        val stopIntent = Intent(this, RestTimerService::class.java).apply {
            action = RestTimerService.ACTION_FINISH
        }
        startService(stopIntent) // Отправляем сервису команду остановки таймера и удаления уведомления
    }
}