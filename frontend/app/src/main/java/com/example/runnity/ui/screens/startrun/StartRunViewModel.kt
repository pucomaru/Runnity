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

    private val _uiState = MutableStateFlow<StartRunUiState>(StartRunUiState.Idle)
    val uiState: StateFlow<StartRunUiState> = _uiState.asStateFlow()

    /**
     * 러닝 시작
     */
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

/**
 * 러닝 시작 화면 UI 상태
 */
sealed class StartRunUiState {
    object Idle : StartRunUiState()
    object Starting : StartRunUiState()
    object Running : StartRunUiState()
    data class Error(val message: String) : StartRunUiState()
}
