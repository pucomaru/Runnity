package com.example.runnity.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.response.RunRecordDetailResponse
import com.example.runnity.data.repository.RunHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 운동 기록 상세 화면 ViewModel
 * - 개인 운동 기록과 챌린지 운동 기록 모두 사용
 */
class RunDetailViewModel : ViewModel() {

    private val repository = RunHistoryRepository()

    private val _uiState = MutableStateFlow<RunDetailUiState>(RunDetailUiState.Loading)
    val uiState: StateFlow<RunDetailUiState> = _uiState.asStateFlow()

    /**
     * 운동 기록 상세 정보 조회
     */
    fun fetchRunDetail(runId: Long) {
        viewModelScope.launch {
            _uiState.value = RunDetailUiState.Loading

            when (val response = repository.getRunRecordDetail(runId)) {
                is ApiResponse.Success -> {
                    _uiState.value = RunDetailUiState.Success(response.data)
                    Timber.d("운동 기록 상세 조회 성공: runId=$runId")
                }
                is ApiResponse.Error -> {
                    _uiState.value = RunDetailUiState.Error(response.message)
                    Timber.e("운동 기록 상세 조회 실패: ${response.message}")
                }
                is ApiResponse.NetworkError -> {
                    _uiState.value = RunDetailUiState.Error("네트워크 오류가 발생했습니다")
                    Timber.e("운동 기록 상세 조회 네트워크 오류")
                }
            }
        }
    }
}

/**
 * 운동 기록 상세 UI 상태
 */
sealed class RunDetailUiState {
    object Loading : RunDetailUiState()
    data class Success(val data: RunRecordDetailResponse) : RunDetailUiState()
    data class Error(val message: String) : RunDetailUiState()
}
