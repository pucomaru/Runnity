package com.example.runnity.ui.screens.workout

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraUpdateFactory
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelLayer
import com.example.runnity.utils.PermissionUtils

// 개인 러닝 화면
@Composable
fun WorkoutPersonalScreen(
    type: String?,
    km: String?,
    min: String?,
    navController: androidx.navigation.NavController? = null,
) {
    // 러닝 세션 관리
    val context = LocalContext.current
    val sessionViewModel: WorkoutSessionViewModel = viewModel(context as androidx.lifecycle.ViewModelStoreOwner)
    val metrics by sessionViewModel.metrics.collectAsState()
    val phase by sessionViewModel.phase.collectAsState()
    val currentPace by sessionViewModel.currentPaceSecPerKm.collectAsState()
    val currentLoc by sessionViewModel.currentLocation.collectAsState()

    // 화면 범위 트래커
    val tracker = remember(context) { WorkoutLocationTracker(context) }


    // 시작/정지 바인딩 (한 번만)
    LaunchedEffect(Unit) {
        sessionViewModel.start()
        tracker.start { lat, lon, elapsedMs, acc, speed ->
            sessionViewModel.ingestLocation(lat, lon, elapsedMs, acc, speed)
        }
    }

    // 정지/종료 바인딩 (한 번만)
    DisposableEffect(Unit) {
        onDispose {
            tracker.stop()
            sessionViewModel.stop()
        }
    }

    var selectedTab by remember { mutableStateOf(0) } // 0=통계, 1=지도
    val isGoalTime = type == "time"
    val isGoalDistance = type == "distance"
    val isFreeRun = type == null

    // Kakao Map refs (지도 탭에서 사용)
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var myLocationLabel by remember { mutableStateOf<Label?>(null) }
    var labelLayer by remember { mutableStateOf<LabelLayer?>(null) }
    var labelStyles by remember { mutableStateOf<LabelStyles?>(null) }

    // 현재 위치 변경 시 지도 카메라/라벨 이동 (생성은 onMapReady에서만)
    LaunchedEffect(currentLoc, selectedTab, kakaoMap) {
        if (selectedTab != 1) return@LaunchedEffect
        val loc = currentLoc ?: return@LaunchedEffect
        val map = kakaoMap ?: return@LaunchedEffect
        val target = LatLng.from(loc.latitude, loc.longitude)
        map.moveCamera(CameraUpdateFactory.newCenterPosition(target))
        if (myLocationLabel == null) {
            map.moveCamera(CameraUpdateFactory.zoomTo(16))
            return@LaunchedEffect
        }
        myLocationLabel?.moveTo(target)
        myLocationLabel?.scaleTo(0.18f, 0.18f)
    }

    // 탭 전환 시 MapView 상태를 명시적으로 동기화
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            mapView?.resume()
            kakaoMap?.let { map ->
                currentLoc?.let { loc ->
                    val target = LatLng.from(loc.latitude, loc.longitude)
                    map.moveCamera(CameraUpdateFactory.newCenterPosition(target))
                }
            }
        } else {
            mapView?.pause()
        }
    }

    // UI 구성
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPalette.Light.background)
    ) {

        Column(Modifier.fillMaxWidth().statusBarsPadding()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedTab = 0 },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DirectionsRun,
                        contentDescription = "통계",
                        tint = if (selectedTab == 0) ColorPalette.Light.primary else ColorPalette.Light.component
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedTab = 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Map,
                        contentDescription = "지도",
                        tint = if (selectedTab == 1) ColorPalette.Light.primary else ColorPalette.Light.component
                    )
                }
            }
            // 선택된 탭 하단선만 표시
            Box(Modifier.fillMaxWidth().height(3.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .align(if (selectedTab == 0) Alignment.CenterStart else Alignment.CenterEnd)
                        .background(ColorPalette.Common.accent)
                        .height(3.dp)
                )
            }
        }
        // 헤더와 본문 콘텐츠 사이 간격 (헤더 고정, 아래부터 1:1 분할)
        Spacer(modifier = Modifier.height(8.dp))

        // 본문: 정확히 상/하 절반 분할
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White)
                .then(if (selectedTab == 0) Modifier.padding(horizontal = 16.dp, vertical = 12.dp) else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val lifecycleOwner = LocalLifecycleOwner.current
            Box(modifier = Modifier.fillMaxSize()) {
                // 항상 MapView를 유지하고, 통계 탭일 때는 위에 UI를 덮어서 숨김 처리
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            mapView = this
                            start(
                                object : MapLifeCycleCallback() {
                                    override fun onMapDestroy() { }
                                    override fun onMapError(error: Exception) { }
                                },
                                object : KakaoMapReadyCallback() {
                                    @SuppressLint("MissingPermission")
                                    override fun onMapReady(map: KakaoMap) {
                                        kakaoMap = map
                                        myLocationLabel = null
                                        labelLayer = map.getLabelManager()?.getLayer()
                                        if (labelStyles == null) {
                                            labelStyles = map.getLabelManager()?.addLabelStyles(
                                                LabelStyles.from(LabelStyle.from(com.example.runnity.R.drawable.ic_my_location_dot))
                                            )
                                        }
                                        currentLoc?.let { loc ->
                                            val target = LatLng.from(loc.latitude, loc.longitude)
                                            kakaoMap?.moveCamera(CameraUpdateFactory.newCenterPosition(target))
                                            kakaoMap?.moveCamera(CameraUpdateFactory.zoomTo(16))
                                            val layer = labelLayer
                                            val styles = labelStyles
                                            if (layer != null && styles != null && myLocationLabel == null) {
                                                val opts = LabelOptions.from(target).setStyles(styles)
                                                myLocationLabel = layer.addLabel(opts)
                                                myLocationLabel?.scaleTo(0.18f, 0.18f)
                                            }
                                        }
                                        if (currentLoc == null && PermissionUtils.hasLocationPermission(context)) {
                                            val fused = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                                            val cts = com.google.android.gms.tasks.CancellationTokenSource()
                                            fused.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                                                .addOnSuccessListener { loc ->
                                                    if (loc != null) {
                                                        val target = LatLng.from(loc.latitude, loc.longitude)
                                                        kakaoMap?.moveCamera(CameraUpdateFactory.newCenterPosition(target))
                                                        kakaoMap?.moveCamera(CameraUpdateFactory.zoomTo(16))
                                                        val layer = labelLayer
                                                        val styles = labelStyles
                                                        if (layer != null && styles != null && myLocationLabel == null) {
                                                            val opts = LabelOptions.from(target).setStyles(styles)
                                                            myLocationLabel = layer.addLabel(opts)
                                                            myLocationLabel?.scaleTo(0.18f, 0.18f)
                                                        }
                                                    }
                                                }
                                        }
                                    }
                                }
                            )
                        }
                    }
                )

                // 통계 탭일 때만 지도 위에 덮어서 숨김 처리
                if (selectedTab == 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // 통계 탭 상단 영역 (목표 표시)
                        val label = when {
                            isGoalTime -> "소요 시간"
                            isGoalDistance -> "킬로미터"
                            else -> "킬로미터"
                        }
                        val mainText = when {
                            isGoalTime -> formatElapsed(metrics.activeElapsedMs)
                            else -> formatDistanceKm(metrics.distanceMeters)
                        }
                        Text(
                            text = label,
                            style = Typography.Subtitle,
                            color = ColorPalette.Light.secondary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = mainText,
                            style = Typography.LargeTitle.copy(fontSize = 72.sp),
                            color = ColorPalette.Light.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (!isFreeRun) {
                            LinearProgressIndicator(
                                progress = { 0.35f },
                                trackColor = Color(0xFFEAEAEA),
                                color = ColorPalette.Common.accent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            )
                        }
                    }
                }

                // MapView lifecycle binding (화면 생명주기와만 연동)
                DisposableEffect(lifecycleOwner, mapView) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> mapView?.resume()
                            Lifecycle.Event.ON_PAUSE -> mapView?.pause()
                            else -> Unit
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        mapView?.resume()
                    }
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        mapView?.pause()
                    }
                }
            }
        }

        // 공통 정보 UI 구성
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(ColorPalette.Light.containerBackground)
                .padding(horizontal = 16.dp, vertical = 16.dp)
            ,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // 공통 메트릭 (2x2)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                val paceText = currentPace?.let { formatPace(it) } ?: "--:--/km"
                MetricItem(label = "페이스", value = paceText)
                val secondValue = if (isGoalTime) formatDistanceKm(metrics.distanceMeters) else formatElapsed(metrics.activeElapsedMs)
                MetricItem(label = if (isGoalTime) "킬로미터" else "시간", value = secondValue)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MetricItem(label = "평균 심박수", value = metrics.avgHeartRate?.toString() ?: "--")
                MetricItem(label = "칼로리", value = formatCalories(metrics.caloriesKcal))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 컨트롤 영역 (세션 상태 기반 토글)
            if (phase != WorkoutPhase.Paused) {
                // Running: 일시정지 하나 (가로 중앙)
                CenterControlCircle(icon = "pause") { sessionViewModel.pause() }
            } else {
                // Paused: 종료(길게3초) + 재개
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CenterControlCircle(icon = "stop", accent = ColorPalette.Common.stopAccent) {
                        sessionViewModel.stop()
                        val route = buildString {
                            append("workout/result?")
                            if (type != null) append("type=$type&")
                            if (km != null) append("km=$km&")
                            if (min != null) append("min=$min&")
                        }.trimEnd('&')
                        navController?.navigate(route)
                    }
                    CenterControlCircle(icon = "play") { sessionViewModel.resume() }
                }
            }
        }
    }
}

// 메트릭 아이템
@Composable
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(text = label, color = ColorPalette.Light.secondary, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = ColorPalette.Light.primary, style = Typography.Heading, textAlign = TextAlign.Center)
    }
}

// 단순한 원형 버튼
@Composable
private fun CenterControlCircle(icon: String, accent: Color = ColorPalette.Common.accent, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .background(accent, shape = androidx.compose.foundation.shape.CircleShape)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // 임시 텍스트 아이콘 표시 (UI 단계)
        val symbol = when (icon) {
            "pause" -> "Ⅱ"
            "play" -> "▶"
            "stop" -> "■"
            else -> "●"
        }
        Text(text = symbol, color = Color.White, style = Typography.Title)
    }
}

// 거리 포맷팅
private fun formatDistanceKm(meters: Double): String {
    val km = meters / 1000.0
    return String.format("%.2f", km)
}

// 시간 포맷팅
private fun formatElapsed(ms: Long): String {
    val totalSec = (ms / 1000).toInt()
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}

// 페이스 포맷팅
private fun formatPace(secPerKm: Double): String {
    val total = secPerKm.toInt()
    val m = total / 60
    val s = total % 60
    return String.format("%d'%02d\"/km", m, s)
}

// 칼로리 포맷팅
private fun formatCalories(kcal: Double): String = String.format("%.0f", kcal)
