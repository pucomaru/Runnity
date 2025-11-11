package com.example.runnity.health

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.health.services.client.HealthServices
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.Availability
import java.util.concurrent.Executors
import com.example.runnity.R

// 포그라운드 서비스 알림/액션 식별자
private const val CHANNEL_ID = "runnity_exercise"
private const val NOTIFICATION_ID = 2001
private const val ACTION_START = "com.example.runnity.action.START"
private const val ACTION_STOP = "com.example.runnity.action.STOP"

// 포그라운드 서비스: 운동 측정을 백그라운드에서도 유지
class ExerciseFgService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null
    
    // 필요 시점에 HS 클라이언트 지연 초기화
    private var healthClient: HealthServicesClient? = null
    // 필요 시점에 운동 클라이언트 지연 초기화
    private var exerciseClient: ExerciseClient? = null
    // HS 콜백/비동기 작업용 단일 스레드 실행기
    private val callbackExecutor = Executors.newSingleThreadExecutor()

    // 운동 업데이트 콜백: 상태/가용성/랩 요약 수신
    private val updateCallback = object : ExerciseUpdateCallback {
        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            Log.d(
                "ExerciseFgService",
                "ExerciseUpdate: state=${update.exerciseStateInfo.state}"
            )
        }

        override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {
            Log.d("ExerciseFgService", "Availability: ${dataType} -> ${availability}")
        }

        // 시작형 서비스만 사용. bind( )는 제공하지 않음
        override fun onRegistered() {
            Log.d("ExerciseFgService", "UpdateCallback registered")
        }

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
            Log.d("ExerciseFgService", "LapSummary: ${lapSummary}")
        }

        override fun onRegistrationFailed(throwable: Throwable) {
            Log.e("ExerciseFgService", "UpdateCallback registration failed", throwable)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("ExerciseFgService", "onCreate")
        // 포그라운드 요구사항: 채널 생성 후 상시 알림 표시
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildOngoingNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 액티비티/폰에서 Intent.action으로 제어할 때 사용
        when (intent?.action) {
            ACTION_START -> {
                Log.d("ExerciseFgService", "ACTION_START received")
                // 준비/진행 중 센서(HR, 위치 등) 가용성 변경 알림
                ensureExerciseClient()
                startRunningSession()
            }
            ACTION_STOP -> {
                Log.d("ExerciseFgService", "ACTION_STOP received")
                stopRunningSession()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                Log.d("ExerciseFgService", "onStartCommand with no action")
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("ExerciseFgService", "onDestroy")
        super.onDestroy()
    }

    // 헬스 서비스 클라이언트 초기화
    private fun ensureExerciseClient() {
        if (healthClient == null) {
            healthClient = HealthServices.getClient(this)
        }
        if (exerciseClient == null) {
            exerciseClient = healthClient!!.exerciseClient
        }
    }

    // 운동 세션 시작
    private fun startRunningSession() {
        val ec = exerciseClient ?: return
        // Wear OS Health Services 클라이언트/운동 클라이언트
        // 업데이트 콜백 등록
        try {
            ec.setUpdateCallback(callbackExecutor, updateCallback)
        } catch (t: Throwable) {
            Log.w("ExerciseFgService", "setUpdateCallback failed: ${t}")
        }

        val config = ExerciseConfig(
            ExerciseType.RUNNING,
            setOf(
                DataType.HEART_RATE_BPM,
                DataType.DISTANCE,
                DataType.LOCATION
            ),
            /* isGpsEnabled = */ true,
            // 수집할 데이터 타입 지정 + GPS/자동 일시정지 설정
            /* shouldEnableAutoPauseAndResume = */ true
        )
        ec.startExerciseAsync(config)
            .addListener({
                Log.d("ExerciseFgService", "startExerciseAsync requested")
            }, callbackExecutor)
    }

    // 운동 세션 종료
    private fun stopRunningSession() {
        val ec = exerciseClient ?: return
        ec.endExerciseAsync()
            .addListener({
                // 세션을 먼저 종료하고 포그라운드/서비스를 내려간다
                Log.d("ExerciseFgService", "endExerciseAsync requested")
            }, callbackExecutor)
        // 세션 종료 및 콜백 해제
        ec.clearUpdateCallbackAsync(updateCallback)
            .addListener({
                Log.d("ExerciseFgService", "clearUpdateCallbackAsync requested")
            }, callbackExecutor)
    }

    // 알림 생성
    private fun buildOngoingNotification(): Notification {
        // 세션/서비스 활성 동안 유지할 포그라운드 알림
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("운동 측정 진행 중")
            .setOngoing(true)
            .setShowWhen(false)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    // RUNNING 세션 시작 및 업데이트 구독(HR/거리/위치)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // O+에서는 포그라운드 알림 표시를 위해 채널이 필요
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Runnity Exercise",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "운동 중 건강/위치 측정 유지"
            mgr.createNotificationChannel(channel)
        }
    }
}
