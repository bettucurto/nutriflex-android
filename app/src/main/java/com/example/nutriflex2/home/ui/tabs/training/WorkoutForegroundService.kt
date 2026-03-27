package com.example.nutriflex2.home.ui.tabs.training

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.os.*
import androidx.core.app.NotificationCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.nutriflex2.MainActivity
import com.example.nutriflex2.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

enum class WorkoutState { ACTIVE, RESTING }

class WorkoutForegroundService : Service() {

    private val channelId = "workout_service_channel"
    private val notificationId = 1001
    private var currentState = WorkoutState.ACTIVE

    private var workoutStartTime: Long = 0L
    private var restEndTime: Long = 0L
    private var restTimer: CountDownTimer? = null
    private var mainHandler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var exerciseName: String = "Workout"
    private var nextExerciseName: String = "Next Exercise"

    // Dados da série ativa
    private var currentSet: Int = 1
    private var totalSets: Int = 1
    private var repsString: String = ""
    private var exerciseIconBitmap: Bitmap? = null
    
    // Índices para o Broadcast de volta à UI
    private var lastExIndex: Int = -1
    private var lastSetIndex: Int = -1

    companion object {
        const val ACTION_START_WORKOUT = "ACTION_START_WORKOUT"
        const val ACTION_STOP_WORKOUT = "ACTION_STOP_WORKOUT"
        const val ACTION_START_REST = "ACTION_START_REST"
        const val ACTION_STOP_REST = "ACTION_STOP_REST"
        const val ACTION_UPDATE_ACTIVE_SET = "ACTION_UPDATE_ACTIVE_SET"
        
        const val ACTION_ADD_TIME = "ACTION_ADD_TIME"
        const val ACTION_SUBTRACT_TIME = "ACTION_SUBTRACT_TIME"
        const val ACTION_SKIP_REST = "ACTION_SKIP_REST"
        const val ACTION_FINISH_SET_NOTIFICATION = "ACTION_FINISH_SET_NOTIFICATION"
        
        const val ACTION_TIMER_ADJUSTED = "ACTION_TIMER_ADJUSTED"
        const val ACTION_TIMER_SKIPPED = "ACTION_TIMER_SKIPPED"

        const val EXTRA_REST_SECONDS = "EXTRA_REST_SECONDS"
        const val EXTRA_EXERCISE_NAME = "EXTRA_EXERCISE_NAME"
        const val EXTRA_NEXT_EXERCISE_NAME = "EXTRA_NEXT_EXERCISE_NAME"
        const val EXTRA_CURRENT_SET = "EXTRA_CURRENT_SET"
        const val EXTRA_TOTAL_SETS = "EXTRA_TOTAL_SETS"
        const val EXTRA_REPS_INFO = "EXTRA_REPS_INFO"
        const val EXTRA_IMAGE_URL = "EXTRA_IMAGE_URL"
        const val EXTRA_ADJUST_SECONDS = "EXTRA_ADJUST_SECONDS"
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
                if (workoutStartTime == 0L) workoutStartTime = SystemClock.elapsedRealtime()
                currentState = WorkoutState.ACTIVE
                startForegroundCompat()
                mainHandler.post(updateRunnable)
            }
            ACTION_UPDATE_ACTIVE_SET -> {
                // Só muda para ACTIVE se não estivermos a descansar
                if (currentState != WorkoutState.RESTING) {
                    currentState = WorkoutState.ACTIVE
                }
                
                exerciseName = intent.getStringExtra(EXTRA_EXERCISE_NAME) ?: exerciseName
                currentSet = intent.getIntExtra(EXTRA_CURRENT_SET, 1)
                totalSets = intent.getIntExtra(EXTRA_TOTAL_SETS, 1)
                repsString = intent.getStringExtra(EXTRA_REPS_INFO) ?: ""
                lastExIndex = intent.getIntExtra("EXTRA_EX_INDEX", -1)
                lastSetIndex = intent.getIntExtra("EXTRA_SET_INDEX", -1)
                
                val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
                if (imageUrl != null) loadExerciseBitmap(imageUrl) else {
                    exerciseIconBitmap = null
                    updateNotification()
                }
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
                if (intent?.action == ACTION_SKIP_REST) {
                    sendBroadcast(Intent(ACTION_TIMER_SKIPPED).apply { setPackage(packageName) })
                }
            }
            ACTION_ADD_TIME -> {
                addRestTime(15000)
                sendBroadcast(Intent(ACTION_TIMER_ADJUSTED).apply { 
                    setPackage(packageName)
                    putExtra(EXTRA_ADJUST_SECONDS, 15)
                })
            }
            ACTION_SUBTRACT_TIME -> {
                addRestTime(-15000)
                sendBroadcast(Intent(ACTION_TIMER_ADJUSTED).apply { 
                    setPackage(packageName)
                    putExtra(EXTRA_ADJUST_SECONDS, -15)
                })
            }
            ACTION_STOP_WORKOUT -> stopSelf()
        }
        return START_STICKY
    }

    private fun loadExerciseBitmap(url: String) {
        serviceScope.launch {
            val loader = ImageLoader(this@WorkoutForegroundService)
            val request = ImageRequest.Builder(this@WorkoutForegroundService).data(url).build()
            val result = loader.execute(request)
            if (result is SuccessResult) {
                exerciseIconBitmap = (result.drawable as android.graphics.drawable.BitmapDrawable).bitmap
                updateNotification()
            }
        }
    }

    private fun startRestTimer(millis: Long) {
        restTimer?.cancel()
        restEndTime = SystemClock.elapsedRealtime() + millis
        restTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) { if (currentState == WorkoutState.RESTING) updateNotification() }
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
            val remaining = (restEndTime - SystemClock.elapsedRealtime()).coerceAtLeast(0)
            restEndTime += millis
            updateNotification()
        }
    }

    private fun createNotification(): Notification {
        val mainIntent = Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
        val pendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.nutrilogo)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (currentState == WorkoutState.ACTIVE) {
            val desc = "Set $currentSet/$totalSets • $repsString reps"
            builder.setContentTitle(exerciseName)
            builder.setContentText(desc)
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(desc))
            builder.setLargeIcon(exerciseIconBitmap)

            val finishIntent = Intent(ACTION_FINISH_SET_NOTIFICATION).apply {
                setPackage(packageName)
                putExtra("exIndex", lastExIndex)
                putExtra("setIndex", lastSetIndex)
            }
            val finishPending = PendingIntent.getBroadcast(this, 0, finishIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            builder.clearActions()
            builder.addAction(0, "Finish Set", finishPending)
        } else {
            val remaining = (restEndTime - SystemClock.elapsedRealtime()).coerceAtLeast(0)
            builder.setContentTitle("Resting")
            builder.setContentText("${formatTime(remaining / 1000)} • Next: $nextExerciseName")
            builder.setLargeIcon(null as Bitmap?)
            
            val skipIntent = Intent(this, WorkoutForegroundService::class.java).apply { action = ACTION_SKIP_REST }
            val skipP = PendingIntent.getService(this, 1, skipIntent, PendingIntent.FLAG_IMMUTABLE)
            val addIntent = Intent(this, WorkoutForegroundService::class.java).apply { action = ACTION_ADD_TIME }
            val addP = PendingIntent.getService(this, 2, addIntent, PendingIntent.FLAG_IMMUTABLE)
            val subIntent = Intent(this, WorkoutForegroundService::class.java).apply { action = ACTION_SUBTRACT_TIME }
            val subP = PendingIntent.getService(this, 3, subIntent, PendingIntent.FLAG_IMMUTABLE)

            builder.clearActions()
            builder.addAction(0, "-15s", subP)
            builder.addAction(0, "SKIP", skipP)
            builder.addAction(0, "+15s", addP)
        }
        return builder.build()
    }

    private fun startForegroundCompat() {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(notificationId, notification)
        }
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, createNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            if (manager?.getNotificationChannel(channelId) == null) {
                val chan = NotificationChannel(channelId, "Workout Service", NotificationManager.IMPORTANCE_LOW)
                manager?.createNotificationChannel(chan)
            }
        }
    }

    private fun formatTime(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(updateRunnable)
        restTimer?.cancel()
        super.onDestroy()
    }
}
