package com.example.nutriflex2.home.ui.tabs.training

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.nutriflex2.MainActivity
import com.example.nutriflex2.R

class RestTimerService : Service() {

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftMillis: Long = 0
    private val channelId = "rest_timer_channel"
    private val notificationId = 1001

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_SKIP = "ACTION_SKIP"
        const val ACTION_ADD_TIME = "ACTION_ADD_TIME"
        const val ACTION_SUBTRACT_TIME = "ACTION_SUBTRACT_TIME"
        const val EXTRA_TIME_SECONDS = "EXTRA_TIME_SECONDS"
        const val EXTRA_EXERCISE_NAME = "EXTRA_EXERCISE_NAME"
        
        const val NOTIFICATION_CLICK_ACTION = "NOTIFICATION_CLICK_ACTION"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val seconds = intent.getIntExtra(EXTRA_TIME_SECONDS, 90)
                val exerciseName = intent.getStringExtra(EXTRA_EXERCISE_NAME) ?: "Next Exercise"
                startTimer(seconds.toLong() * 1000, exerciseName)
            }
            ACTION_STOP -> stopSelf()
            ACTION_SKIP -> stopSelf()
            ACTION_ADD_TIME -> {
                val exerciseName = intent.getStringExtra(EXTRA_EXERCISE_NAME) ?: "Next Exercise"
                addTime(15000, exerciseName)
            }
            ACTION_SUBTRACT_TIME -> {
                val exerciseName = intent.getStringExtra(EXTRA_EXERCISE_NAME) ?: "Next Exercise"
                addTime(-15000, exerciseName)
            }
        }
        return START_NOT_STICKY
    }

    private fun startTimer(millis: Long, exerciseName: String) {
        countDownTimer?.cancel()
        timeLeftMillis = millis
        
        createNotificationChannel()
        startForeground(notificationId, createNotification(exerciseName))

        countDownTimer = object : CountDownTimer(timeLeftMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftMillis = millisUntilFinished
                updateNotification(exerciseName)
            }

            override fun onFinish() {
                stopSelf()
            }
        }.start()
    }

    private fun addTime(millis: Long, exerciseName: String) {
        val newTime = (timeLeftMillis + millis).coerceAtLeast(0)
        startTimer(newTime, exerciseName)
    }

    private fun createNotification(exerciseName: String): Notification {
        val timeFormatted = formatTime(timeLeftMillis / 1000)
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val skipIntent = Intent(this, RestTimerService::class.java).apply { action = ACTION_SKIP }
        val skipPendingIntent = PendingIntent.getService(this, 1, skipIntent, PendingIntent.FLAG_IMMUTABLE)

        val addIntent = Intent(this, RestTimerService::class.java).apply { action = ACTION_ADD_TIME }
        val addPendingIntent = PendingIntent.getService(this, 2, addIntent, PendingIntent.FLAG_IMMUTABLE)

        val subIntent = Intent(this, RestTimerService::class.java).apply { action = ACTION_SUBTRACT_TIME }
        val subPendingIntent = PendingIntent.getService(this, 3, subIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Resting: $timeFormatted")
            .setContentText("Next: $exerciseName")
            .setSmallIcon(R.drawable.nutrilogo) // Using existing icon
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "-15s", subPendingIntent)
            .addAction(0, "SKIP", skipPendingIntent)
            .addAction(0, "+15s", addPendingIntent)
            .build()
    }

    private fun updateNotification(exerciseName: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, createNotification(exerciseName))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Rest Timer Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun formatTime(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}
