package com.example.runnity.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.key
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.R
import com.example.runnity.theme.ColorPalette
import com.example.runnity.ui.components.*
import com.example.runnity.utils.PermissionUtils
import com.example.runnity.utils.hasNotificationPermission
import com.example.runnity.utils.rememberLocationPermissionLauncher
import com.example.runnity.utils.rememberNotificationPermissionLauncher
import com.example.runnity.utils.requestLocationPermissions
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import android.os.Build
import android.Manifest
import java.time.format.DateTimeFormatter
import timber.log.Timber

/**
 * 홈 화면
 * - 메인 대시보드
 * - 날씨 카드, 추천 챌린지, 예약한 챌린지 표시
 * - 챌린지 클릭 시 세부 화면으로 이동
 *
 * @param navController 세부 화면으로 이동하기 위한 NavController (탭 내부 이동)
 * @param parentNavController 앱 전체 이동용 NavController (로그인 화면 등으로 이동)
 * @param viewModel 홈 화면의 ViewModel (데이터 관리)
 */
@Composable
fun HomeScreen(
    navController: NavController? = null,        // 세부 화면 이동용 (MainTabScreen에서 전달)
    parentNavController: NavController? = null,  // 앱 전체 이동용
    viewModel: HomeViewModel = viewModel()       // viewModel(): ViewModel 자동 생성
) {
    val context = LocalContext.current

    // 날씨 정보 구독
    val weatherData by viewModel.weather.collectAsState()
    val weatherLoading by viewModel.weatherLoading.collectAsState()

    val locationLauncher = rememberLocationPermissionLauncher { granted ->
        if (granted) {
            @SuppressLint("MissingPermission")
            fun fetchLocation() {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val cancellationToken = CancellationTokenSource().token

                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationToken
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.fetchWeather(location.latitude, location.longitude)
                        Timber.d("현재 위치 (정확도: ${location.accuracy}m): ${location.latitude}, ${location.longitude}")
                    } else {
                        viewModel.fetchWeather(37.5665, 126.9780)
                        Timber.w("위치 정보 없음 → 서울 기본값 사용")
                    }
                }.addOnFailureListener { exception ->
                    Timber.e(exception, "위치 조회 실패 → 서울 기본값 사용")
                    viewModel.fetchWeather(37.5665, 126.9780)
                }
            }
            fetchLocation()
        } else {
            viewModel.fetchWeather(37.5665, 126.9780)
            Timber.w("위치 권한 거부 → 서울 기본값 사용")
        }
    }

    val notificationLauncher = rememberNotificationPermissionLauncher { }

    // 위치 가져오기 및 날씨 조회 + 권한 요청 (캐시 사용)
    LaunchedEffect(Unit) {
        if (!PermissionUtils.hasLocationPermission(context)) {
            requestLocationPermissions(locationLauncher)
        } else {
            @SuppressLint("MissingPermission")
            fun fetchInitialLocation() {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val cancellationToken = CancellationTokenSource().token

                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationToken
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.fetchWeatherIfNeeded(location.latitude, location.longitude)
                        Timber.d("현재 위치 (정확도: ${location.accuracy}m): ${location.latitude}, ${location.longitude}")
                    } else {
                        viewModel.fetchWeatherIfNeeded(37.5665, 126.9780)
                        Timber.w("위치 정보 없음 → 서울 기본값 사용")
                    }
                }.addOnFailureListener { exception ->
                    Timber.e(exception, "위치 조회 실패 → 서울 기본값 사용")
                    viewModel.fetchWeatherIfNeeded(37.5665, 126.9780)
                }
            }
            fetchInitialLocation()
        }

        if (Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission(context)) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // 홈 입장/소켓 연결 관련 에러 메시지 토스트 표시
    LaunchedEffect(Unit) {
        viewModel.errorEvents.collect { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // 추천 챌린지 샘플 데이터
    val recommendedChallenges = listOf(
        RecommendedChallengeItem(
            id = "rec_1",
            imageUrl = null,
            title = "러니티 추천 챌린지",
            description = "대규모 러닝 실시간 경쟁"
        ),
        RecommendedChallengeItem(
            id = "rec_2",
            imageUrl = null,
            title = "주말 마라톤",
            description = "함께 달리는 즐거움"
        ),
        RecommendedChallengeItem(
            id = "rec_3",
            imageUrl = null,
            title = "초보 러너 환영",
            description = "천천히 함께 달려요"
        )
    )

    // 예약한 챌린지: ViewModel의 실제 데이터 사용
    val reservedChallenges = viewModel.reservedChallenges.collectAsState().value

    // 전체 레이아웃 (Box로 FAB 배치)
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // 1. 상단 앱바 (로고)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorPalette.Common.accent)  // 액센트 색상 배경
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 로고 (이미지)
            Image(
                painter = painterResource(id = R.drawable.runnity_logo),
                contentDescription = "Runnity Logo",
                modifier = Modifier.height(32.dp),
                contentScale = ContentScale.Fit
            )
        }

        // 2. 스크롤 가능한 내용
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)  // 컴포넌트 간 16dp 간격
        ) {
            // 2-1. 날씨 카드
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorPalette.Common.accent)  // 액센트 색상 배경
                    .padding(16.dp)
            ) {
                if (weatherLoading) {
                    // 로딩 중
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else {
                    // 실제 날씨 데이터 표시
                    val weather = weatherData
                    val currentTime = remember {
                        java.time.LocalTime.now()
                            .format(DateTimeFormatter.ofPattern("h:mm a"))
                    }

                    val weatherMain = weather?.weatherMain ?: "Clouds"
                    val weatherKorean = when (weatherMain) {
                        "Clear" -> "맑음"
                        "Clouds" -> "흐림"
                        "Rain" -> "비"
                        "Snow" -> "눈"
                        "Thunderstorm" -> "천둥번개"
                        "Drizzle" -> "이슬비"
                        "Mist", "Fog" -> "안개"
                        else -> weather?.weatherDescription ?: "흐림"
                    }
                    val backgroundImage = when (weatherMain) {
                        "Clear" -> R.drawable.weather_clear
                        "Clouds" -> R.drawable.weather_clouds
                        "Rain" -> R.drawable.weather_rain
                        "Snow" -> R.drawable.weather_snow
                        "Thunderstorm" -> R.drawable.weather_thunderstorm
                        "Drizzle", "Mist", "Fog" -> R.drawable.weather_drizzle
                        else -> R.drawable.weather_clouds
                    }

                    WeatherCard(
                        country = weather?.country ?: "Korea",
                        city = weather?.cityName ?: "Seoul",
                        weather = weatherKorean,
                        temperature = "${weather?.temperature ?: 10}°",
                        time = currentTime,
                        backgroundImageRes = backgroundImage,
                        onClick = {
                            navController?.navigate("weather_detail")
                        },
                        onRefresh = {
                            if (PermissionUtils.hasLocationPermission(context)) {
                                @SuppressLint("MissingPermission")
                                fun refreshLocation() {
                                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                                    val cancellationToken = CancellationTokenSource().token

                                    fusedLocationClient.getCurrentLocation(
                                        Priority.PRIORITY_HIGH_ACCURACY,
                                        cancellationToken
                                    ).addOnSuccessListener { location ->
                                        if (location != null) {
                                            viewModel.fetchWeather(location.latitude, location.longitude)
                                            Timber.d("새로고침 - 현재 위치 (정확도: ${location.accuracy}m): ${location.latitude}, ${location.longitude}")
                                        } else {
                                            viewModel.fetchWeather(37.5665, 126.9780)
                                            Timber.w("새로고침 - 위치 정보 없음")
                                        }
                                    }.addOnFailureListener { exception ->
                                        Timber.e(exception, "새로고침 - 위치 조회 실패")
                                        viewModel.fetchWeather(37.5665, 126.9780)
                                    }
                                }
                                refreshLocation()
                            } else {
                                viewModel.fetchWeather(37.5665, 126.9780)
                                Timber.w("새로고침 - 위치 권한 없음")
                            }
                        }
                    )
                }
            }

            // 2-2. 운영진 추천 챌린지 섹션
            SectionHeader(
                subtitle = "운영진 추천 챌린지",
                caption = "이번 주 인기 챌린지"
            )

            RecommendedChallengeCarousel(
                challenges = recommendedChallenges,
                onChallengeClick = { id ->
                    navController?.navigate("challenge_detail/$id")
                }
            )

            // 2-3. 예약한 챌린지 섹션 + 새로고침 버튼
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                SectionHeader(
                    subtitle = "예약한 챌린지",
                    caption = "내가 예약한 챌린지를 확인하세요",
                )

                IconButton(
                    onClick = { viewModel.fetchReservedChallenges() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "예약한 챌린지 새로고침",
                        tint = ColorPalette.Light.component
                    )
                }
            }

            // 예약한 챌린지 리스트
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                reservedChallenges.forEach { challenge ->
                    ChallengeCard(
                        title = challenge.title,
                        distance = challenge.distance,
                        startDateTime = challenge.startDateTime,
                        participants = challenge.participants,
                        buttonState = challenge.buttonState,
                        onCardClick = {
                            // 소켓 방이 활성화되어 "참여하기" 버튼이 뜬 챌린지는
                            // 카드 전체를 눌러도 상세 조회로 이동하지 않도록 막는다.
                            if (challenge.buttonState != ChallengeButtonState.Join) {
                                navController?.navigate("challenge_detail/${challenge.id}")
                            }
                        },
                        onButtonClick = {
                            val needsLocation = !PermissionUtils.hasLocationPermission(context)
                            val needsNotification = Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission(context)
                            if (needsLocation || needsNotification) {
                                if (needsLocation) {
                                    requestLocationPermissions(locationLauncher)
                                } else if (needsNotification && Build.VERSION.SDK_INT >= 33) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                viewModel.joinChallengeAndConnect(challenge.id) {
                                    navController?.navigate("challenge_waiting/${challenge.id}")
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
        }

        // FloatingActionButton (중계방 보기)
        FloatingActionButton(
            onClick = {
                 navController?.navigate("broadcast_view")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = ColorPalette.Common.accent,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Filled.LiveTv,
                contentDescription = "중계방 보기",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
