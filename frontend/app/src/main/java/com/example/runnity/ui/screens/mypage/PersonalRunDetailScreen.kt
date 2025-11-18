package com.example.runnity.ui.screens.mypage

import android.annotation.SuppressLint
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
 * 개인 운동 기록 상세 화면
 * - 마이페이지 -> 개인 탭 -> 리스트 항목 클릭 시 이동
 * - 개인 달리기 기록의 상세 정보 표시
 *
 * @param runId 운동 기록 ID
 * @param navController 뒤로가기용
 */
@Composable
fun PersonalRunDetailScreen(
    runId: String,
    navController: NavController,
    viewModel: RunDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                RunDetailContent(data = state.data)
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
 * 운동 기록 상세 내용 (지도 + 요약)
 */
@SuppressLint("MissingPermission")
@Composable
fun RunDetailContent(data: RunRecordDetailResponse) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

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

    // 지도 + 요약 (반응형)
    Column(modifier = Modifier.fillMaxSize()) {
        // 상단: 지도 (고정 높이)
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

        // 하단: 라벨 + 요약 (남은 공간 활용)
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 라벨 행 (운동 타입 + 시작 시간)
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
                    val dateTime = LocalDateTime.parse(data.startAt, DateTimeFormatter.ISO_DATE_TIME)
                    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))
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

            // 요약 내용
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
                    text = String.format("%.2f", data.distance),
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
        }
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
