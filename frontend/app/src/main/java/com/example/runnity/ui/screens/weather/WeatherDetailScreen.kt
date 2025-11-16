package com.example.runnity.ui.screens.weather

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.R
import com.example.runnity.data.model.response.WeatherUiModel
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.ActionHeader
import com.example.runnity.utils.PermissionUtils
import com.google.android.gms.location.LocationServices
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 날씨 상세 화면
 */
@SuppressLint("MissingPermission")
@Composable
fun WeatherDetailScreen(
    navController: NavController,
    viewModel: WeatherDetailViewModel = viewModel()
) {
    val weatherData by viewModel.weather.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val context = LocalContext.current

    // 날씨 데이터 로드
    LaunchedEffect(Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (PermissionUtils.hasLocationPermission(context)) {
            fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.fetchWeather(location.latitude, location.longitude)
                } else {
                    // 위치를 가져올 수 없으면 서울 좌표 사용
                    viewModel.fetchWeather(37.5665, 126.9780)
                }
            }.addOnFailureListener {
                // 실패 시 서울 좌표 사용
                viewModel.fetchWeather(37.5665, 126.9780)
            }
        } else {
            // 권한 없으면 서울 좌표 사용
            viewModel.fetchWeather(37.5665, 126.9780)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPalette.Light.background)
    ) {
        // 헤더
        ActionHeader(
            title = "날씨",
            onBack = { navController.navigateUp() }
        )

        if (loading) {
            // 로딩 중
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ColorPalette.Common.accent)
            }
        } else {
            weatherData?.let { weather ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. 현재 날씨 큰 카드
                CurrentWeatherCard(weather)

                // 2. 러닝 지수 카드
                RunningIndexCard(weather)

                // 3. 상세 정보 섹션
                Text(
                    text = "상세 정보",
                    style = Typography.Subheading,
                    color = ColorPalette.Light.primary
                )

                // 4. 상세 정보 그리드
                WeatherDetailsGrid(weather)
            }
            }
        }
    }
}

/**
 * 현재 날씨 큰 카드
 */
@Composable
fun CurrentWeatherCard(weather: WeatherUiModel) {
    // 날씨 상태에 따른 배경 이미지 선택
    val backgroundImage = when (weather.weatherMain) {
        "Clear" -> R.drawable.weather_clear
        "Clouds" -> R.drawable.weather_clouds
        "Rain" -> R.drawable.weather_rain
        "Snow" -> R.drawable.weather_snow
        "Thunderstorm" -> R.drawable.weather_thunderstorm
        "Drizzle", "Mist", "Fog" -> R.drawable.weather_drizzle
        else -> R.drawable.weather_clouds
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // 1. 배경 이미지
            Image(
                painter = painterResource(id = backgroundImage),
                contentDescription = "날씨 배경",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 2. 그라데이션 오버레이 (텍스트 가독성 향상)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.2f)
                            )
                        )
                    )
            )

            // 3. 텍스트 콘텐츠
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 상단: 위치
                Column {
                    Text(
                        text = weather.cityName,
                        style = Typography.Title,
                        color = Color.White
                    )
                    Text(
                        text = weather.country,
                        style = Typography.Caption,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                // 중앙: 온도 + 날씨
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // 온도
                    Text(
                        text = "${weather.temperature}°",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // 날씨 상태
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = getWeatherKorean(weather.weatherMain),
                            style = Typography.Subheading,
                            color = Color.White
                        )
                        Text(
                            text = "최고 ${weather.tempMax}° · 최저 ${weather.tempMin}°",
                            style = Typography.Caption,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            }
        }
    }
}

/**
 * 러닝 지수 카드
 */
@Composable
fun RunningIndexCard(weather: WeatherUiModel) {
    val runningScore = calculateRunningScore(weather)
    val recommendation = getRunningRecommendation(runningScore, weather)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ColorPalette.Common.accent.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsRun,
                    contentDescription = null,
                    tint = ColorPalette.Common.accent,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "러닝 지수",
                    style = Typography.Subheading,
                    color = ColorPalette.Light.primary
                )
            }

            // 별점 표시
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < runningScore) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = null,
                        tint = if (index < runningScore) ColorPalette.Common.accent else ColorPalette.Light.component,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "($runningScore.0/5.0)",
                    style = Typography.Body,
                    color = ColorPalette.Light.secondary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Text(
                text = recommendation,
                style = Typography.Body,
                color = ColorPalette.Light.secondary
            )
        }
    }
}

/**
 * 상세 정보 그리드
 */
@Composable
fun WeatherDetailsGrid(weather: WeatherUiModel) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1행
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeatherDetailItem(
                icon = Icons.Filled.Thermostat,
                label = "체감온도",
                value = "${weather.feelsLike}°",
                modifier = Modifier.weight(1f)
            )
            WeatherDetailItem(
                icon = Icons.Filled.WaterDrop,
                label = "습도",
                value = "${weather.humidity}%",
                modifier = Modifier.weight(1f)
            )
        }

        // 2행
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeatherDetailItem(
                icon = Icons.Filled.Air,
                label = "바람",
                value = "${String.format("%.1f", weather.windSpeed)} m/s",
                modifier = Modifier.weight(1f)
            )
            WeatherDetailItem(
                icon = Icons.Filled.Compress,
                label = "기압",
                value = "${weather.pressure} hPa",
                modifier = Modifier.weight(1f)
            )
        }

        // 3행
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeatherDetailItem(
                icon = Icons.Filled.Cloud,
                label = "구름",
                value = "${weather.clouds}%",
                modifier = Modifier.weight(1f)
            )
            WeatherDetailItem(
                icon = Icons.Filled.Visibility,
                label = "가시거리",
                value = weather.visibility?.let { "${it / 1000}km" } ?: "정보없음",
                modifier = Modifier.weight(1f)
            )
        }

        // 4행
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeatherDetailItem(
                icon = Icons.Filled.WbSunny,
                label = "일출",
                value = formatTime(weather.sunrise),
                modifier = Modifier.weight(1f)
            )
            WeatherDetailItem(
                icon = Icons.Filled.NightsStay,
                label = "일몰",
                value = formatTime(weather.sunset),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 상세 정보 아이템
 */
@Composable
fun WeatherDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ColorPalette.Light.component,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = Typography.Caption,
                color = ColorPalette.Light.component
            )
            Text(
                text = value,
                style = Typography.Subheading,
                color = ColorPalette.Light.primary
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = Typography.Caption,
                    color = ColorPalette.Light.secondary
                )
            }
        }
    }
}

// ===== 헬퍼 함수 =====

/**
 * 날씨별 그라데이션 색상
 */
fun getWeatherGradient(weatherMain: String): List<Color> {
    return when (weatherMain) {
        "Clear" -> listOf(Color(0xFF4A90E2), Color(0xFF50C9FF))
        "Clouds" -> listOf(Color(0xFF6C757D), Color(0xFFADB5BD))
        "Rain", "Drizzle" -> listOf(Color(0xFF5C6BC0), Color(0xFF7986CB))
        "Snow" -> listOf(Color(0xFFB0BEC5), Color(0xFFECEFF1))
        "Thunderstorm" -> listOf(Color(0xFF455A64), Color(0xFF607D8B))
        else -> listOf(Color(0xFF6C757D), Color(0xFFADB5BD))
    }
}

/**
 * 날씨 한글 변환
 */
fun getWeatherKorean(weatherMain: String): String {
    return when (weatherMain) {
        "Clear" -> "맑음"
        "Clouds" -> "흐림"
        "Rain" -> "비"
        "Snow" -> "눈"
        "Thunderstorm" -> "천둥번개"
        "Drizzle" -> "이슬비"
        "Mist", "Fog" -> "안개"
        else -> weatherMain
    }
}

/**
 * 러닝 지수 계산 (1-5점)
 */
fun calculateRunningScore(weather: WeatherUiModel): Int {
    var score = 3 // 기본 3점

    // 온도 평가 (10-20도가 최적)
    when (weather.temperature) {
        in 10..20 -> score += 2
        in 5..25 -> score += 1
        in 0..5, in 25..30 -> score += 0
        else -> score -= 1
    }

    // 습도 평가
    when (weather.humidity) {
        in 40..60 -> score += 1
        in 70..100 -> score -= 1
    }

    // 바람 평가
    when {
        weather.windSpeed < 5 -> score += 0
        weather.windSpeed in 5.0..10.0 -> score -= 1
        weather.windSpeed > 10 -> score -= 2
    }

    return score.coerceIn(1, 5)
}

/**
 * 러닝 추천 메시지
 */
fun getRunningRecommendation(score: Int, weather: WeatherUiModel): String {
    return when {
        score >= 4 -> "러닝하기 좋은 날씨입니다! 🏃"
        score == 3 -> when {
            weather.temperature < 10 -> "쌀쌀해요. 가벼운 겉옷을 챙기세요! 🧥"
            weather.temperature > 25 -> "따뜻해요. 수분 섭취를 잊지 마세요! 💧"
            weather.humidity > 70 -> "습도가 높아요. 땀이 많이 날 거예요! 💦"
            weather.windSpeed > 7 -> "바람이 강해요. 주의하세요! 💨"
            else -> "러닝하기 괜찮은 날씨예요! 😊"
        }
        score == 2 -> "야외 러닝보다 실내 운동을 추천해요! 🏠"
        else -> "오늘은 휴식을 취하는 게 좋겠어요! 😴"
    }
}

/**
 * 풍향 변환
 */
fun getWindDirection(deg: Int): String {
    return when (deg) {
        in 0..22, in 338..360 -> "북풍"
        in 23..67 -> "북동풍"
        in 68..112 -> "동풍"
        in 113..157 -> "남동풍"
        in 158..202 -> "남풍"
        in 203..247 -> "남서풍"
        in 248..292 -> "서풍"
        in 293..337 -> "북서풍"
        else -> ""
    }
}

/**
 * 시간 포맷 (Unix timestamp → HH:mm)
 */
fun formatTime(timestamp: Long): String {
    return try {
        Instant.ofEpochSecond(timestamp)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        "-"
    }
}
