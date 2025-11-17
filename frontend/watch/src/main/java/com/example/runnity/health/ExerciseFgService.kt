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
import androidx.health.services.client.data.WarmUpConfig
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.Availability
import java.util.concurrent.Executors
import com.example.runnity.R
import com.example.runnity.MainActivity
import com.example.runnity.ui.WatchWorkoutActivity
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

// 포그라운드 서비스 알림/액션 식별자
private const val CHANNEL_ID = "runnity_exercise"
private const val NOTIFICATION_ID = 2001
private const val ACTION_START = "com.example.runnity.action.START"
private const val ACTION_PREPARE = "com.example.runnity.action.PREPARE"
private const val ACTION_PAUSE = "com.example.runnity.action.PAUSE"
private const val ACTION_RESUME = "com.example.runnity.action.RESUME"
private const val ACTION_STOP = "com.example.runnity.action.STOP"
private const val ACTION_METRICS_BROADCAST = "com.example.runnity.action.METRICS"
private const val ACTION_FINISH_UI = "com.example.runnity.action.FINISH_UI"

// 포그라운드 서비스: 운동 측정을 백그라운드에서도 유지
class ExerciseFgService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null
    
    // 필요 시점에 HS 클라이언트 지연 초기화
    private var healthClient: HealthServicesClient? = null
    // 필요 시점에 운동 클라이언트 지연 초기화
    private var exerciseClient: ExerciseClient? = null
    // HS 콜백/비동기 작업용 단일 스레드 실행기
    private val callbackExecutor = Executors.newSingleThreadExecutor()

    // 메트릭 상태 보관
    @Volatile private var latestHrBpm: Int? = null
    @Volatile private var latestDistanceM: Double? = null
    @Volatile private var latestCaloriesKcal: Double? = null
    @Volatile private var isRunning: Boolean = false
    private var sessionStartMs: Long = 0L
    private var activeStartMs: Long = 0L
    private var activeElapsedAccumMs: Long = 0L
    private var lastStopAtMs: Long = 0L

    // 운동 업데이트 콜백: 상태/가용성/랩 요약 수신
    private val updateCallback = object : ExerciseUpdateCallback {
        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            Log.d(
                "ExerciseFgService",
                "ExerciseUpdate: state=${update.exerciseStateInfo.state}"
            )
            // 메트릭 파싱 (가능한 경우에만)
            runCatching {
                val container = update.latestMetrics
                // 심박 (샘플 타입) - 가장 최근 값 사용
                val hr = container.getData(DataType.HEART_RATE_BPM).lastOrNull()?.value
                if (hr != null) {
                    latestHrBpm = hr.toInt()
                }
                // 거리 (델타 타입) - 누적 값 사용
                val dist = container.getData(DataType.DISTANCE).lastOrNull()?.value
                if (dist != null) {
                    latestDistanceM = dist
                }
                // 칼로리(있으면 사용)
                val cal = runCatching { container.getData(DataType.CALORIES).lastOrNull()?.value }.getOrNull()
                if (cal != null) {
                    latestCaloriesKcal = cal
                }
                if (hr != null || dist != null || cal != null) {
                    Log.d("ExerciseFgService", "parsed metrics hr=${hr?.toInt()} dist=${dist} cal=${cal}")
                }
            }.onFailure {
                Log.w("ExerciseFgService", "failed to parse latestMetrics", it)
            }
            // 임시 전체 덤프가 필요하면 아래 주석 해제
            // Log.d("ExerciseFgService", "Update dump: $update")
        }

        override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {
            Log.d("ExerciseFgService", "Availability changed: $dataType -> $availability")
        }

        // 시작형 서비스만 사용. bind( )는 제공하지 않음
        override fun onRegistered() {
            Log.d("ExerciseFgService", "ExerciseUpdateCallback registered")
        }

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
            Log.d("ExerciseFgService", "Lap summary: $lapSummary")
        }

        override fun onRegistrationFailed(throwable: Throwable) {
            Log.w("ExerciseFgService", "ExerciseUpdateCallback registration failed", throwable)
        }
    }

    // --------------------
    // 메트릭 1초 주기 송신
    // --------------------
    private var metricsScheduler: ScheduledExecutorService? = null
    private var metricsTask: ScheduledFuture<*>? = null

    private fun startMetricsTicker() {
        if (metricsScheduler == null) metricsScheduler = Executors.newSingleThreadScheduledExecutor()
        if (metricsTask != null && !(metricsTask?.isCancelled ?: true)) return
        Log.d("ExerciseFgService", "Start metrics ticker")
        metricsTask = metricsScheduler!!.scheduleAtFixedRate({
            try {
                sendMetricsOnce()
            } catch (_: Throwable) { }
        }, 0, 1, TimeUnit.SECONDS)
    }

    private fun stopMetricsTicker() {
        try { metricsTask?.cancel(true) } catch (_: Throwable) {}
        metricsTask = null
        Log.d("ExerciseFgService", "Stop metrics ticker")
    }

    private fun sendMetricsOnce() {
        val now = System.currentTimeMillis()
        val activeElapsed = if (isRunning) activeElapsedAccumMs + (now - activeStartMs) else activeElapsedAccumMs
        val dist = latestDistanceM
        val rawPaceSpKm = if (dist != null && dist > 0.0 && activeElapsed > 0L) {
            (activeElapsed / 1000.0) / (dist / 1000.0)
        } else null
        // 워치 페이스도 2'30"/km ~ 20'00"/km 범위만 정상값으로 사용
        val paceSpKm = rawPaceSpKm?.takeIf { it.isFinite() && it in 150.0..1200.0 }
        val json = JSONObject()
        json.put("type", "metrics")
        if (latestHrBpm != null) json.put("hr_bpm", latestHrBpm)
        if (dist != null) json.put("distance_m", dist)
        if (paceSpKm != null && paceSpKm.isFinite()) json.put("pace_spkm", paceSpKm)
        if (latestCaloriesKcal != null) json.put("cal_kcal", latestCaloriesKcal)
        json.put("elapsed_ms", activeElapsed)
        val bytes = json.toString().toByteArray()
        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener { nodes ->
                nodes.filter { it.isNearby }.forEach { node ->
                    Wearable.getMessageClient(this)
                        .sendMessage(node.id, "/metrics/update", bytes)
                }
            }

        // Also broadcast locally on watch for UI updates
        val intent = Intent(ACTION_METRICS_BROADCAST).apply {
            setPackage(packageName)
            putExtra("elapsed_ms", activeElapsed)
            latestHrBpm?.let { putExtra("hr_bpm", it) }
            dist?.let { putExtra("distance_m", it) }
            if (paceSpKm != null && paceSpKm.isFinite()) putExtra("pace_spkm", paceSpKm)
            latestCaloriesKcal?.let { putExtra("cal_kcal", it) }
        }
        sendBroadcast(intent)
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
        val act = intent?.action
        Log.d("ExerciseFgService", "onStartCommand action=$act")
        when (act) {
            ACTION_PREPARE -> {
                ensureExerciseClient()
                prepareRunningSession()
            }
            ACTION_START -> {
                // 새 세션 시작 시 직전 상태 완전 초기화
                latestHrBpm = null
                latestDistanceM = null
                latestCaloriesKcal = null
                activeElapsedAccumMs = 0L
                sessionStartMs = 0L
                activeStartMs = 0L
                isRunning = false

                // 준비/진행 중 센서(HR, 위치 등) 가용성 변경 알림
                ensureExerciseClient()
                startRunningSession()
                isRunning = true
                val now = System.currentTimeMillis()
                sessionStartMs = now
                activeStartMs = now
                // 최소 UI: 운동 진행 화면 표시
                try {
                    val uiIntent = Intent(
                        this,
                        WatchWorkoutActivity::class.java
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    startActivity(uiIntent)
                } catch (t: Throwable) {
                    Log.w("ExerciseFgService", "failed to launch WatchWorkoutActivity", t)
                }
            }
            ACTION_PAUSE -> {
                if (!isRunning) {
                    Log.d("ExerciseFgService", "PAUSE ignored: not running")
                    return START_STICKY
                }
                ensureExerciseClient()
                pauseExercise()
                stopMetricsTicker()
                val now = System.currentTimeMillis()
                activeElapsedAccumMs += (now - activeStartMs)
                isRunning = false
            }
            ACTION_RESUME -> {
                ensureExerciseClient()
                resumeExercise()
                startMetricsTicker()
                if (!isRunning) {
                    activeStartMs = System.currentTimeMillis()
                    isRunning = true
                }
            }
            ACTION_STOP -> {
                val now = System.currentTimeMillis()
                if (!isRunning) {
                    Log.d("ExerciseFgService", "STOP ignored: not running")
                    return START_STICKY
                }
                // Grace period: ignore STOP within first 4 seconds after START
                if (sessionStartMs > 0L && now - sessionStartMs < 4000L) {
                    Log.d("ExerciseFgService", "STOP ignored: within grace period ${(now - sessionStartMs)}ms after start")
                    return START_STICKY
                }
                // Debounce rapid duplicate STOPs within 2 seconds
                if (lastStopAtMs > 0L && now - lastStopAtMs < 2000L) {
                    Log.d("ExerciseFgService", "STOP ignored: debounced ${(now - lastStopAtMs)}ms since last stop")
                    return START_STICKY
                }
                lastStopAtMs = now

                stopRunningSession()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopMetricsTicker()
                isRunning = false
                activeStartMs = 0L
                activeElapsedAccumMs = 0L
                latestHrBpm = null
                latestDistanceM = null
                latestCaloriesKcal = null
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
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

        // 메트릭 송신 시작
        startMetricsTicker()
    }

    // 운동 세션 준비(센서 워밍업/가용성 확인)
    private fun prepareRunningSession() {
        val ec = exerciseClient ?: return
        try {
            ec.setUpdateCallback(callbackExecutor, updateCallback)
            Log.d("ExerciseFgService", "setUpdateCallback done")
        } catch (t: Throwable) {
            Log.w("ExerciseFgService", "setUpdateCallback (prepare) failed: ${t}")
        }

        val warmUp = WarmUpConfig(
            ExerciseType.RUNNING,
            setOf(
                DataType.DISTANCE
            )
        )
        ec.prepareExerciseAsync(warmUp)
            .addListener({
                Log.d("ExerciseFgService", "prepareExerciseAsync requested")
            }, callbackExecutor)
    }

    // 운동 세션 종료
    private fun stopRunningSession() {
        val ec = exerciseClient ?: return
        ec.endExerciseAsync()
            .addListener({
                // 세션을 먼저 종료하고 포그라운드/서비스를 내려간다
            }, callbackExecutor)
        // 세션 종료 및 콜백 해제
        ec.clearUpdateCallbackAsync(updateCallback)
            .addListener({
            }, callbackExecutor)
        stopMetricsTicker()
    }

    private fun pauseExercise() {
        val ec = exerciseClient ?: return
        ec.pauseExerciseAsync()
            .addListener({
                // paused
            }, callbackExecutor)
    }

    private fun resumeExercise() {
        val ec = exerciseClient ?: return
        ec.resumeExerciseAsync()
            .addListener({
                // resumed
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
