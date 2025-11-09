package com.example.runnity.ui.screens.startrun

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 개인 러닝 시작 화면 ViewModel
 * - 러닝 목표 설정
 * - 경로 선택
 * - GPS 권한 확인
 */
class StartRunViewModel : ViewModel() {

    // UI 상태
    private val _uiState = MutableStateFlow<StartRunUiState>(StartRunUiState.Idle)
    val uiState: StateFlow<StartRunUiState> = _uiState.asStateFlow()

    // 목표 탭
    enum class GoalTab { Distance, Time }

    // 현재 활성화된 탭
    private val _activeTab = MutableStateFlow(GoalTab.Distance)
    val activeTab: StateFlow<GoalTab> = _activeTab.asStateFlow()

    // 거리 텍스트
    private val _distanceText = MutableStateFlow("")
    val distanceText: StateFlow<String> = _distanceText.asStateFlow()

    // 시간 텍스트
    private val _timeMinutesText = MutableStateFlow("")
    val timeMinutesText: StateFlow<String> = _timeMinutesText.asStateFlow()

    // 탭 변경
    fun setActiveTab(tab: GoalTab) {
        _activeTab.value = tab
        when (tab) {
            GoalTab.Distance -> _timeMinutesText.value = ""
            GoalTab.Time -> _distanceText.value = ""
        }
    }

    // 거리 텍스트 변경
    fun setDistanceText(text: String) {
        _activeTab.value = GoalTab.Distance
        _distanceText.value = text
        _timeMinutesText.value = "" // clear time when distance is set
    }

    // 시간 텍스트 변경
    fun setTimeMinutesText(text: String) {
        _activeTab.value = GoalTab.Time
        _timeMinutesText.value = text
        _distanceText.value = "" // clear distance when time is set
    }

    // 거리/시간 텍스트 파싱
    private fun parseDistanceKm(): Double? {
        val v = _distanceText.value.trim()
        if (v.isEmpty()) return null
        return v.toDoubleOrNull()
    }
    // 시간 텍스트 파싱
    private fun parseTimeMinutes(): Int? {
        val v = _timeMinutesText.value.trim()
        if (v.isEmpty()) return null
        return v.toIntOrNull()
    }

    // 거리/시간 텍스트 유효성 검사
    fun isValidDistance(): Boolean {
        val d = parseDistanceKm() ?: return false
        // 1.0 ~ 100.0 km, step 0.1 (step is enforced by UI keyboard; here we only bound-check)
        return d in 1.0..100.0
    }

    // 시간 텍스트 유효성 검사
    fun isValidTime(): Boolean {
        val m = parseTimeMinutes() ?: return false
        // 1 ~ 600 minutes (10 hours)
        return m in 1..600
    }

    // 목표 결정
    fun resolveGoal(): Goal {
        return when (_activeTab.value) {
            GoalTab.Distance -> if (isValidDistance()) Goal.Distance(parseDistanceKm()!!) else Goal.FreeRun
            GoalTab.Time -> if (isValidTime()) Goal.Time(parseTimeMinutes()!!) else Goal.FreeRun
        }
    }

    // 러닝 시작
    fun startRun() {
        viewModelScope.launch {
            try {
                _uiState.value = StartRunUiState.Starting
                // TODO: 러닝 세션 시작 로직
                _uiState.value = StartRunUiState.Running
            } catch (e: Exception) {
                _uiState.value = StartRunUiState.Error(e.message ?: "러닝 시작 실패")
            }
        }
    }
}

// 러닝 시작 화면 UI 상태
sealed class StartRunUiState {
    object Idle : StartRunUiState()
    object Starting : StartRunUiState()
    object Running : StartRunUiState()
    data class Error(val message: String) : StartRunUiState()
}

// 러닝 목표 모델
sealed class Goal {
    data class Distance(val km: Double) : Goal()
    data class Time(val minutes: Int) : Goal()
    object FreeRun : Goal()
}
