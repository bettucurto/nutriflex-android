package com.example.nutriflex2.home.ui.tabs.training

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.nutriflex2.MainActivity
import com.example.nutriflex2.R

enum class WorkoutState { ACTIVE, RESTING }

class WorkoutForegroundService : Service() {

    private val channelId = "workout_service_channel"
    private val notificationId = 1001
    private var currentState = WorkoutState.ACTIVE

    private var workoutStartTime: Long = 0L
    private var restEndTime: Long = 0L
    private var restTimer: CountDownTimer? = null
    private var mainHandler = Handler(Looper.getMainLooper())
    
    private var exerciseName: String = "Workout in progress"
    private var nextExerciseName: String = "Next Exercise"

    companion object {
        const val ACTION_START_WORKOUT = "ACTION_START_WORKOUT"
        const val ACTION_STOP_WORKOUT = "ACTION_STOP_WORKOUT"
        const val ACTION_START_REST = "ACTION_START_REST"
        const val ACTION_STOP_REST = "ACTION_STOP_REST"
        
        const val ACTION_ADD_TIME = "ACTION_ADD_TIME"
        const val ACTION_SUBTRACT_TIME = "ACTION_SUBTRACT_TIME"
        const val ACTION_SKIP_REST = "ACTION_SKIP_REST"

        const val EXTRA_REST_SECONDS = "EXTRA_REST_SECONDS"
        const val EXTRA_EXERCISE_NAME = "EXTRA_EXERCISE_NAME"
        const val EXTRA_NEXT_EXERCISE_NAME = "EXTRA_NEXT_EXERCISE_NAME"
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (currentState == WorkoutState.ACTIVE) {
                updateNotification()
                mainHandler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_WORKOUT -> {
                if (workoutStartTime == 0L) {
                    workoutStartTime = SystemClock.elapsedRealtime()
                }
                currentState = WorkoutState.ACTIVE
                exerciseName = intent.getStringExtra(EXTRA_EXERCISE_NAME) ?: "Workout in progress"
                
                val notification = createNotification()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                } else {
                    startForeground(notificationId, notification)
                }
                
                mainHandler.post(updateRunnable)
            }
            ACTION_START_REST -> {
                currentState = WorkoutState.RESTING
                val seconds = intent.getIntExtra(EXTRA_REST_SECONDS, 90)
                nextExerciseName = intent.getStringExtra(EXTRA_NEXT_EXERCISE_NAME) ?: "Next Exercise"
                startRestTimer(seconds.toLong() * 1000)
            }
            ACTION_STOP_REST, ACTION_SKIP_REST -> {
                stopRestTimer()
                currentState = WorkoutState.ACTIVE
                mainHandler.post(updateRunnable)
            }
            ACTION_ADD_TIME -> addRestTime(15000)
            ACTION_SUBTRACT_TIME -> addRestTime(-15000)
            ACTION_STOP_WORKOUT -> {
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startRestTimer(millis: Long) {
        restTimer?.cancel()
        restEndTime = SystemClock.elapsedRealtime() + millis
        
        restTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (currentState == WorkoutState.RESTING) {
                    updateNotification()
                }
            }
            override fun onFinish() {
                if (currentState == WorkoutState.RESTING) {
                    currentState = WorkoutState.ACTIVE
                    mainHandler.post(updateRunnable)
                }
            }
        }.start()
    }

    private fun stopRestTimer() {
        restTimer?.cancel()
        restTimer = null
    }

    private fun addRestTime(millis: Long) {
        if (currentState == WorkoutState.RESTING) {
            val remaining = restEndTime - SystemClock.elapsedRealtime()
            val newTime = (remaining + millis).coerceAtLeast(0)
            startRestTimer(newTime)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.nutrilogo)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (currentState == WorkoutState.ACTIVE) {
            val elapsedMillis = SystemClock.elapsedRealtime() - workoutStartTime
            val timeFormatted = formatTime(elapsedMillis / 1000)
            builder.setContentTitle("Workout in progress: $timeFormatted")
            builder.setContentText(exerciseName)
        } else {
            val remainingMillis = (restEndTime - SystemClock.elapsedRealtime()).coerceAtLeast(0)
            val timeFormatted = formatTime(remainingMillis / 1000)
            
            builder.setContentTitle("Resting: $timeFormatted")
            builder.setContentText("Next: $nextExerciseName")

            // Rest Actions
            val skipIntent = Intent(this, WorkoutForegroundService::class.java).apply { action = ACTION_SKIP_REST }
            val skipPendingIntent = PendingIntent.getService(this, 1, skipIntent, PendingIntent.FLAG_IMMUTABLE)

            val addIntent = Intent(this, WorkoutForegroundService::class.java).apply { action = ACTION_ADD_TIME }
            val addPendingIntent = PendingIntent.getService(this, 2, addIntent, PendingIntent.FLAG_IMMUTABLE)

            val subIntent = Intent(this, WorkoutForegroundService::class.java).apply { action = ACTION_SUBTRACT_TIME }
            val subPendingIntent = PendingIntent.getService(this, 3, subIntent, PendingIntent.FLAG_IMMUTABLE)

            builder.addAction(0, "-15s", subPendingIntent)
            builder.addAction(0, "SKIP", skipPendingIntent)
            builder.addAction(0, "+15s", addPendingIntent)
        }

        return builder.build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        notificationManager.notify(notificationId, createNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            if (manager?.getNotificationChannel(channelId) == null) {
                val serviceChannel = NotificationChannel(
                    channelId,
                    "Workout Service Channel",
                    NotificationManager.IMPORTANCE_LOW
                )
                manager?.createNotificationChannel(serviceChannel)
            }
        }
    }

    private fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) {
            String.format("%02d:%02d:%02d", h, m, s)
        } else {
            String.format("%02d:%02d", m, s)
        }
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(updateRunnable)
        restTimer?.cancel()
        super.onDestroy()
    }
}
