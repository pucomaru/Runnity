package com.example.runnity.ui.screens.mypage

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.R
import com.example.runnity.data.model.response.RunRecordDetailResponse
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.ActionHeader
import com.example.runnity.ui.screens.workout.GeoPoint
import com.example.runnity.utils.MapUtil
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.route.RouteLine
import com.kakao.vectormap.route.RouteLineStyles
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 챌린지 운동 기록 상세 화면
 * - 마이페이지 -> 챌린지 탭 -> 리스트 항목 클릭 시 이동
 * - 챌린지 달리기 기록의 상세 정보 표시
 *
 * @param runId 운동 기록 ID
 * @param navController 뒤로가기용
 */
@Composable
fun ChallengeRunDetailScreen(
    runId: String,
    navController: NavController,
    viewModel: RunDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val challengeDetail by viewModel.challengeDetail.collectAsState()

    // 데이터 로드
    LaunchedEffect(runId) {
        viewModel.fetchRunDetail(runId.toLongOrNull() ?: 0L)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 헤더
        ActionHeader(
            title = "운동 기록 상세",
            onBack = { navController.navigateUp() },
            height = 56.dp
        )

        HorizontalDivider(color = Color(0xFFDDDDDD))

        // 내용
        when (val state = uiState) {
            is RunDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ColorPalette.Common.accent)
                }
            }
            is RunDetailUiState.Success -> {
                ChallengeRunDetailContent(
                    data = state.data,
                    challengeDetail = challengeDetail
                )
            }
            is RunDetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        style = Typography.Body,
                        color = ColorPalette.Light.secondary
                    )
                }
            }
        }
    }
}

/**
 * 챌린지 운동 기록 상세 내용 (지도 + 요약 + 랭킹)
 */
@SuppressLint("MissingPermission")
@Composable
fun ChallengeRunDetailContent(
    data: RunRecordDetailResponse,
    challengeDetail: com.example.runnity.data.model.response.ChallengeDetailResponse?
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // 현재 사용자 정보
    val currentProfile = com.example.runnity.data.util.UserProfileManager.getProfile()
    val currentUserId = currentProfile?.memberId

    // 지도 상태
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var routeLine by remember { mutableStateOf<RouteLine?>(null) }
    var routeStyles by remember { mutableStateOf<RouteLineStyles?>(null) }
    var markerLayer by remember { mutableStateOf<LabelLayer?>(null) }
    var markerStyles by remember { mutableStateOf<LabelStyles?>(null) }
    var startPointLabel by remember { mutableStateOf<Label?>(null) }

    // route JSON 문자열 파싱
    val route = try {
        if (!data.route.isNullOrBlank()) {
            val type = object : TypeToken<List<GeoPoint>>() {}.type
            Gson().fromJson<List<GeoPoint>>(data.route, type)
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }

    // 스크롤 가능 (지도 + 요약 + 랭킹)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 1. 지도
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapView = this
                        start(
                            object : MapLifeCycleCallback() {
                                override fun onMapDestroy() {}
                                override fun onMapError(error: Exception) {}
                            },
                            object : KakaoMapReadyCallback() {
                                override fun onMapReady(map: KakaoMap) {
                                    kakaoMap = map
                                    markerLayer = map.getLabelManager()?.getLayer()
                                    if (markerStyles == null) {
                                        markerStyles = map.getLabelManager()?.addLabelStyles(
                                            LabelStyles.from(
                                                LabelStyle.from(R.drawable.ic_my_location_dot)
                                            )
                                        )
                                    }

                                    val latLngs: List<LatLng> = MapUtil.getLatLngRoutePathFromGeo(route)
                                    routeStyles = MapUtil.setRoutePathStyle(ctx)

                                    if (latLngs.size >= 2) {
                                        routeLine = MapUtil.drawRouteLine(map, latLngs, routeStyles!!)
                                        MapUtil.moveCameraToRoute(map, latLngs, MapUtil.DEFAULT_ZOOM_LEVEL)
                                    } else if (latLngs.size == 1) {
                                        val p = latLngs.first()
                                        map.moveCamera(CameraUpdateFactory.newCenterPosition(p))
                                        map.moveCamera(CameraUpdateFactory.zoomTo(16))
                                        val layer = markerLayer
                                        val styles = markerStyles
                                        if (layer != null && styles != null && startPointLabel == null) {
                                            val opts = LabelOptions.from(p).setStyles(styles)
                                            startPointLabel = layer.addLabel(opts)
                                            startPointLabel?.scaleTo(0.18f, 0.18f)
                                        }
                                    } else {
                                        // 경로 정보 없을 때 현재 위치로 이동
                                        val fused = LocationServices.getFusedLocationProviderClient(ctx)
                                        fused.lastLocation.addOnSuccessListener { loc ->
                                            loc?.let {
                                                val target = LatLng.from(it.latitude, it.longitude)
                                                map.moveCamera(CameraUpdateFactory.newCenterPosition(target))
                                                map.moveCamera(CameraUpdateFactory.zoomTo(16))
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            )

            DisposableEffect(lifecycleOwner, mapView) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> mapView?.resume()
                        Lifecycle.Event.ON_PAUSE -> mapView?.pause()
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }
        }

        // 2. 라벨 행 (운동 타입 + 시작 시간)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val typeLabel = when (data.runType) {
                "PERSONAL" -> "개인 러닝"
                "CHALLENGE" -> "챌린지"
                else -> "러닝"
            }
            Text(
                text = typeLabel,
                style = Typography.Subtitle,
                color = ColorPalette.Light.secondary
            )

            val startText = try {
                val dateTime = LocalDateTime.parse(data.startAt)
                dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"))
            } catch (e: Exception) {
                data.startAt ?: ""
            }
            Text(
                text = startText,
                style = Typography.Caption,
                color = ColorPalette.Light.secondary
            )
        }

        HorizontalDivider(color = Color(0xFFDDDDDD))

        // 3. 요약 내용
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 큰 글씨: 총 거리
            Text(
                text = "총 km",
                style = Typography.Subtitle,
                color = ColorPalette.Light.secondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = String.format("%.1f", data.distance),
                style = Typography.LargeTitle.copy(fontSize = 72.sp),
                color = ColorPalette.Light.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 1행: 평균 페이스 + 시간
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val half = this.maxWidth / 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.width(half),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "평균 페이스",
                            style = Typography.Caption,
                            color = ColorPalette.Light.secondary
                        )
                        Text(
                            formatPace(data.pace.toDouble()),
                            style = Typography.Title
                        )
                    }
                    Column(
                        modifier = Modifier.width(half),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "시간",
                            style = Typography.Caption,
                            color = ColorPalette.Light.secondary
                        )
                        Text(
                            formatElapsed(data.durationSec.toLong() * 1000),
                            style = Typography.Title
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 2행: 평균 심박수 + 칼로리
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val half = this.maxWidth / 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.width(half),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "평균 심박수",
                            style = Typography.Caption,
                            color = ColorPalette.Light.secondary
                        )
                        Text(
                            data.bpm.toString(),
                            style = Typography.Title
                        )
                    }
                    Column(
                        modifier = Modifier.width(half),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "칼로리",
                            style = Typography.Caption,
                            color = ColorPalette.Light.secondary
                        )
                        Text(
                            "${data.calories.toInt()} kcal",
                            style = Typography.Title
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFFDDDDDD), modifier = Modifier.padding(horizontal = 16.dp))

        // 4. 랭킹 섹션
        if (challengeDetail != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "챌린지 랭킹",
                    style = Typography.Subheading,
                    color = ColorPalette.Light.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 랭킹 순으로 정렬
                val sortedParticipants = challengeDetail.participants.sortedBy { it.rank }

                if (sortedParticipants.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "참가자가 없습니다",
                            style = Typography.Body,
                            color = ColorPalette.Light.secondary
                        )
                    }
                } else {
                    // 랭킹 리스트
                    sortedParticipants.forEach { participant ->
                        ChallengeRankingItem(
                            participant = participant,
                            currentUserId = currentUserId
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * 챌린지 랭킹 아이템
 */
@Composable
private fun ChallengeRankingItem(
    participant: com.example.runnity.data.model.response.ChallengeParticipantInfo,
    currentUserId: Long?
) {
    val isCurrentUser = currentUserId != null && participant.memberId == currentUserId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorPalette.Light.background, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 왼쪽: 순위 + 프로필 이미지 + 닉네임
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.weight(1f)
        ) {
            // 순위
            Text(
                text = "${participant.rank}위",
                style = Typography.Subheading,
                color = when (participant.rank) {
                    1 -> Color(0xFFFFD700) // 금색
                    2 -> Color(0xFFC0C0C0) // 은색
                    3 -> Color(0xFFCD7F32) // 동색
                    else -> ColorPalette.Light.primary
                },
                modifier = Modifier.width(50.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 프로필 이미지
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ColorPalette.Light.containerBackground)
                    .border(
                        width = if (isCurrentUser) 2.dp else 1.dp,
                        color = if (isCurrentUser) ColorPalette.Common.accent else ColorPalette.Light.component,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!participant.profileImage.isNullOrBlank()) {
                    AsyncImage(
                        model = participant.profileImage,
                        contentDescription = "프로필 이미지",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "프로필",
                        tint = if (isCurrentUser) ColorPalette.Common.accent else ColorPalette.Light.component,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 닉네임 + 나 배지
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = participant.nickname,
                    style = Typography.Body,
                    color = ColorPalette.Light.primary
                )

                if (isCurrentUser) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = ColorPalette.Common.accent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "나",
                            style = Typography.Caption,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 오른쪽: 페이스 (해당 챌린지에 대한 페이스)
        val paceToShow = participant.paceSec ?: participant.averagePaceSec
        Text(
            text = formatPace(paceToShow.toDouble()),
            style = Typography.Caption,
            color = ColorPalette.Light.secondary
        )
    }
}

// ===== 헬퍼 함수 =====

private fun formatElapsed(ms: Long): String {
    val totalSec = (ms / 1000).toInt()
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}

private fun formatPace(secPerKm: Double): String {
    val total = secPerKm.toInt()
    val m = total / 60
    val s = total % 60
    return String.format("%d'%02d\"/km", m, s)
}
