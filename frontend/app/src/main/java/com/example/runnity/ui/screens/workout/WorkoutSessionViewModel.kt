package com.example.runnity.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.*

// 운동 상태 머신 (UI/수집/타이머 제어의 기준 상태)
// - Idle: 시작 전/초기화 상태
// - Running: 위치 수집/타이머/거리 누적이 진행되는 상태
// - Paused: 일시정지(타이머/누적 멈춤, 지도는 보이되 측정은 중단)
// - Ended: 종료/요약 화면 준비 상태
enum class WorkoutPhase { Idle, Running, Paused, Ended }

// Utils: 하버사인 거리(m)
private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

// 위경도 샘플(필요 시 정확도/시간 등 필드는 추후 확장)
data class GeoPoint(val latitude: Double, val longitude: Double)

// 세션 누적 메트릭(초기값 0).
// - distanceMeters: 누적 이동거리(미터)
// - totalElapsedMs: 세션 전체 경과시간(일시정지 포함)
// - activeElapsedMs: 활동 시간(일시정지 제외)
// - avgPaceSecPerKm: 평균 페이스(초/킬로미터)
// - caloriesKcal: 추정 칼로리
// - avgHeartRate: 평균 심박(워치 연동 후 실제 값 주입)
data class WorkoutMetrics(
    val distanceMeters: Double = 0.0,
    val totalElapsedMs: Long = 0L,
    val activeElapsedMs: Long = 0L,
    val avgPaceSecPerKm: Double? = null,
    val caloriesKcal: Double = 0.0,
    val avgHeartRate: Int? = null
)

// 운동 세션을 관리하는 ViewModel
// - 외부(화면/서비스)에서 위치/거리/칼로리 업데이트를 주입하고, 상태와 메트릭을 UI에 내보냄
class WorkoutSessionViewModel : ViewModel() {
    // 현재 세션 상태
    private val _phase = MutableStateFlow(WorkoutPhase.Idle)
    val phase: StateFlow<WorkoutPhase> = _phase.asStateFlow()

    // 누적 메트릭 
    private val _metrics = MutableStateFlow(WorkoutMetrics())
    val metrics: StateFlow<WorkoutMetrics> = _metrics.asStateFlow()

    // 경로(실시간 폴리라인용)
    private val _route = MutableStateFlow<List<GeoPoint>>(emptyList())
    val route: StateFlow<List<GeoPoint>> = _route.asStateFlow()

    // 현재 위치(마커용)
    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)
    val currentLocation: StateFlow<GeoPoint?> = _currentLocation.asStateFlow()

    // 현재 페이스(최근 100m 기준, sec/km). null이면 표시하지 않음
    private val _currentPaceSecPerKm = MutableStateFlow<Double?>(null)
    val currentPaceSecPerKm: StateFlow<Double?> = _currentPaceSecPerKm.asStateFlow()

    // 시간 계산용 기준값들
    private var sessionStartMs: Long? = null
    private val _sessionStartTime = MutableStateFlow<Long?>(null)
    val sessionStartTime: StateFlow<Long?> = _sessionStartTime.asStateFlow()
    private var activeStartMs: Long? = null
    private var activeBaseOnResumeMs: Long = 0L
    private var tickerJob: Job? = null

    // 목표 관련 상태
    data class Goal(
        val type: String, // "time" | "distance"
        val targetTimeMs: Long? = null,
        val targetDistanceMeters: Double? = null
    )
    private val _goal = MutableStateFlow<Goal?>(null)
    val goal: StateFlow<Goal?> = _goal.asStateFlow()
    private val _goalProgress = MutableStateFlow<Float?>(null)
    val goalProgress: StateFlow<Float?> = _goalProgress.asStateFlow()
    private val _remainingTimeMs = MutableStateFlow<Long?>(null)
    val remainingTimeMs: StateFlow<Long?> = _remainingTimeMs.asStateFlow()
    private val _remainingDistanceMeters = MutableStateFlow<Double?>(null)
    val remainingDistanceMeters: StateFlow<Double?> = _remainingDistanceMeters.asStateFlow()

    // 누적 계산용 내부 상태
    private var lastPoint: GeoPoint? = null
    private var lastPointTimeMs: Long? = null
    private var totalDistanceMetersInternal: Double = 0.0
    private val userWeightKg: Double = 70.0 // TODO: 설정 연동 전까지 기본값

    // 100m 세그먼트 기반 현재 페이스 계산용 버퍼
    private var segmentDistanceMeters: Double = 0.0
    private var segmentTimeMs: Long = 0L

    // 세션 시작: 기준 시각 세팅 후 Running으로 전환, 1초 티커 시작
    fun start() {
        if (_phase.value != WorkoutPhase.Idle && _phase.value != WorkoutPhase.Ended) return
        sessionStartMs = System.currentTimeMillis()
        _sessionStartTime.value = sessionStartMs
        activeStartMs = sessionStartMs
        activeBaseOnResumeMs = 0L
        _phase.value = WorkoutPhase.Running
        startTicker()
    }

    // 새로운 세션을 시작하기 전에 이전 상태를 완전히 초기화
    fun resetSession() {
        stopTicker()
        _phase.value = WorkoutPhase.Idle
        _metrics.value = WorkoutMetrics()
        _route.value = emptyList()
        _currentLocation.value = null
        _currentPaceSecPerKm.value = null
        sessionStartMs = null
        _sessionStartTime.value = null
        activeStartMs = null
        activeBaseOnResumeMs = 0L
        lastPoint = null
        lastPointTimeMs = null
        totalDistanceMetersInternal = 0.0
        segmentDistanceMeters = 0.0
        segmentTimeMs = 0L
        _goal.value = null
        _goalProgress.value = null
        _remainingTimeMs.value = null
        _remainingDistanceMeters.value = null
    }

    // 외부에서 목표 설정 (null이면 자유 달리기)
    fun setGoal(type: String?, km: String?, min: String?) {
        if (type == null) {
            _goal.value = null
            _goalProgress.value = null
            _remainingTimeMs.value = null
            _remainingDistanceMeters.value = null
            return
        }
        when (type) {
            "time" -> {
                val minutes = min?.toLongOrNull() ?: 0L
                val target = minutes * 60_000L
                _goal.value = Goal(type = "time", targetTimeMs = target)
            }
            "distance" -> {
                val kmVal = km?.toDoubleOrNull() ?: 0.0
                val target = kmVal * 1000.0
                _goal.value = Goal(type = "distance", targetDistanceMeters = target)
            }
            else -> {
                _goal.value = null
            }
        }
        _goalProgress.value = 0f
    }

    // 일시정지: 지금까지의 active 시간을 누적 저장하고 시간 잠시 중단
    fun pause() {
        if (_phase.value != WorkoutPhase.Running) return
        val now = System.currentTimeMillis()
        val prevActiveStart = activeStartMs ?: now
        val addActive = now - prevActiveStart
        _metrics.value = _metrics.value.copy(
            totalElapsedMs = (sessionStartMs?.let { now - it } ?: _metrics.value.totalElapsedMs),
            activeElapsedMs = _metrics.value.activeElapsedMs + addActive
        )
        activeStartMs = null
        activeBaseOnResumeMs = _metrics.value.activeElapsedMs
        _phase.value = WorkoutPhase.Paused
        stopTicker()
    }

    // 재개: active 기준 시각 재설정하고 시간 재시작
    fun resume() {
        if (_phase.value != WorkoutPhase.Paused) return
        activeStartMs = System.currentTimeMillis()
        activeBaseOnResumeMs = _metrics.value.activeElapsedMs
        _phase.value = WorkoutPhase.Running
        startTicker()
    }

    // 종료: 최종 total/active 시간 확정 후 Ended 전환
    fun stop() {
        val now = System.currentTimeMillis()
        val total = sessionStartMs?.let { now - it } ?: _metrics.value.totalElapsedMs
        // Use currentActiveElapsedMs to avoid double-counting the last slice on stop
        val active = currentActiveElapsedMs()
        _metrics.value = _metrics.value.copy(totalElapsedMs = total, activeElapsedMs = active)
        _phase.value = WorkoutPhase.Ended
        stopTicker()
    }

    // 위치 업데이트 주입: 현재 위치 갱신 + Running 상태에서만 경로에 추가
    fun submitLocation(point: GeoPoint) {
        _currentLocation.value = point
        if (_phase.value == WorkoutPhase.Running) {
            _route.value = _route.value + point
        }
    }

    // Fused 수집으로부터 들어온 데이터를 세션 누적 로직으로 흡수
    // 필터링: 최소 이동 임계, 과속, 정확도(선택적)
    fun ingestLocation(
        latitude: Double,
        longitude: Double,
        elapsedRealtimeMs: Long? = null,
        accuracyMeters: Float? = null,
        speedMps: Float? = null
    ) {
        val point = GeoPoint(latitude, longitude)
        submitLocation(point)

        if (_phase.value != WorkoutPhase.Running) {
            lastPoint = point
            lastPointTimeMs = elapsedRealtimeMs
            return
        }

        val prev = lastPoint
        val prevT = lastPointTimeMs
        lastPoint = point
        lastPointTimeMs = elapsedRealtimeMs
        if (prev == null) return

        // 정확도 필터 (약간 엄격)
        if ((accuracyMeters ?: 0f) > 60f) return

        val d = haversineMeters(prev.latitude, prev.longitude, point.latitude, point.longitude)
        // 최소 이동 임계 (약간 엄격: 2m)
        if (d < 2.0) return

        // 속도 필터 (약간 엄격: ~30.6 km/h 초과 제외)
        if (speedMps != null && speedMps > 8.5f) return
        // 저속 노이즈 억제: 거의 정지 상태에서의 미세 이동은 무시
        if (speedMps != null && speedMps < 0.3f && d < 3.0) return

        totalDistanceMetersInternal += d

        // 짧은 간격에서의 비현실적 점프 제거 (예: 1.5초 이내에 20m 이상 이동)
        segmentDistanceMeters += d
        val dtMs = if (elapsedRealtimeMs != null && prevT != null && elapsedRealtimeMs >= prevT) (elapsedRealtimeMs - prevT) else 0L
        if (dtMs in 1..1500 && d > 20.0) {
            // 점프 샘플 버림
            return
        }
        // 비정상적으로 큰 간격은 상한(10초) 적용
        val clampedDt = dtMs.coerceIn(0L, 10_000L)
        segmentTimeMs += clampedDt

        // 100m 달성 시 현재 페이스 산출 및 이벤트 발행
        if (segmentDistanceMeters >= 100.0 && segmentTimeMs > 0L) {
            val paceSecPerKm = (segmentTimeMs / 1000.0) / (segmentDistanceMeters / 1000.0)
            if (paceSecPerKm.isFinite() && paceSecPerKm > 0) {
                _currentPaceSecPerKm.value = paceSecPerKm
            }

            // 남는 거리/시간 비례 이월(초과분 유지)
            val overMeters = segmentDistanceMeters - 100.0
            if (overMeters > 0.0) {
                val keepRatio = if (segmentDistanceMeters > 0.0) overMeters / segmentDistanceMeters else 0.0
                segmentDistanceMeters = overMeters
                segmentTimeMs = (segmentTimeMs * keepRatio).toLong()
            } else {
                segmentDistanceMeters = 0.0
                segmentTimeMs = 0L
            }
        }

        // 칼로리 간이 추정: 1 km당 체중(kg) kcal
        val kcal = (totalDistanceMetersInternal / 1000.0) * userWeightKg
        updateDistanceAndCalories(totalDistanceMetersInternal, kcal)
    }

    // 외부에서 계산된 거리/칼로리를 반영, 평균 페이스는 active 시간 기준으로 내부 계산
    fun updateDistanceAndCalories(distanceMeters: Double, caloriesKcal: Double) {
        val pace = if (distanceMeters > 0.0) {
            val activeSec = currentActiveElapsedMs() / 1000.0
            if (activeSec > 0) (activeSec / (distanceMeters / 1000.0)) else null
        } else null
        _metrics.value = _metrics.value.copy(
            distanceMeters = distanceMeters,
            caloriesKcal = caloriesKcal,
            avgPaceSecPerKm = pace
        )

        // 목표가 거리일 때 진행도/잔여 갱신 및 자동 종료
        val g = _goal.value
        if (g?.type == "distance") {
            val target = g.targetDistanceMeters ?: 0.0
            if (target > 0.0) {
                val progress = (distanceMeters / target).toFloat().coerceIn(0f, 1f)
                _goalProgress.value = progress
                _remainingDistanceMeters.value = (target - distanceMeters).coerceAtLeast(0.0)
                if (progress >= 1f && _phase.value == WorkoutPhase.Running) {
                    stop()
                }
            }
        }
    }

    // 현재 시점의 활동 시간(일시정지 제외)을 계산
    private fun currentActiveElapsedMs(): Long {
        return if (_phase.value == WorkoutPhase.Running && activeStartMs != null) {
            activeBaseOnResumeMs + (System.currentTimeMillis() - (activeStartMs ?: 0L))
        } else _metrics.value.activeElapsedMs
    }

    // 1초마다 total/active 시간을 갱신해 UI가 실시간으로 반영되게 함
    private fun startTicker() {
        if (tickerJob != null) return
        tickerJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val total = sessionStartMs?.let { now - it } ?: 0L
                val active = currentActiveElapsedMs()
                _metrics.value = _metrics.value.copy(totalElapsedMs = total, activeElapsedMs = active)

                // 목표가 시간일 때 진행도/잔여 갱신 및 자동 종료
                val g = _goal.value
                if (g?.type == "time") {
                    val target = g.targetTimeMs ?: 0L
                    if (target > 0L) {
                        val progress = (total.toFloat() / target.toFloat()).coerceIn(0f, 1f)
                        _goalProgress.value = progress
                        _remainingTimeMs.value = (target - total).coerceAtLeast(0L)
                        if (progress >= 1f && _phase.value == WorkoutPhase.Running) {
                            stop()
                        }
                    }
                }
                delay(1000L)
            }
        }
    }

    // 시간 중단(일시정지/종료 시 호출)
    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }
}
