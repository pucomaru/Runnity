package com.example.runnity.ui.screens.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.response.WeatherUiModel
import com.example.runnity.data.model.response.toUiModel
import com.example.runnity.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 날씨 상세 화면 ViewModel
 */
class WeatherDetailViewModel : ViewModel() {

    private val weatherRepository = WeatherRepository()

    private val _weather = MutableStateFlow<WeatherUiModel?>(null)
    val weather: StateFlow<WeatherUiModel?> = _weather.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    /**
     * 위치 기반 날씨 정보 조회
     */
    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _loading.value = true

            when (val response = weatherRepository.getCurrentWeather(lat, lon)) {
                is ApiResponse.Success -> {
                    _weather.value = response.data.toUiModel()
                    Timber.d("날씨 상세 정보 로드 성공")
                }
                is ApiResponse.Error -> {
                    Timber.e("날씨 조회 실패: ${response.message}")
                    _weather.value = null
                }
                is ApiResponse.NetworkError -> {
                    Timber.e("날씨 네트워크 오류")
                    _weather.value = null
                }
            }

            _loading.value = false
        }
    }

    /**
     * 외부에서 날씨 데이터 설정 (HomeViewModel에서 전달받은 경우)
     */
    fun setWeather(weatherData: WeatherUiModel) {
        _weather.value = weatherData
    }
}
