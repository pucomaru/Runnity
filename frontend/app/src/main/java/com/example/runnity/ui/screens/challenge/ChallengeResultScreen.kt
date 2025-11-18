package com.example.runnity.ui.screens.challenge

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.request.RunCreateRequest
import com.example.runnity.data.model.request.RunLapCreateRequest
import com.example.runnity.data.repository.RunRepository
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.PageHeader
import com.example.runnity.ui.screens.workout.WorkoutSessionViewModel
import com.example.runnity.utils.MapUtil
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
import com.google.gson.Gson
import com.google.android.gms.location.LocationServices
import timber.log.Timber

@Composable
fun ChallengeResultScreen(
    challengeId: Int,
    socketViewModel: ChallengeSocketViewModel,
    sessionViewModel: WorkoutSessionViewModel,
    onClose: (() -> Unit)? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val metrics by sessionViewModel.metrics.collectAsState()
    val route by sessionViewModel.route.collectAsState()
    val sessionStartTime by sessionViewModel.sessionStartTime.collectAsState()
    val laps by sessionViewModel.laps.collectAsState()

    var posted by rememberSaveable { mutableStateOf(false) }

    // 챌린지 결과 화면 진입 시, 챌린지 러닝 기록을 백엔드에 한 번만 저장
    // - runType: CHALLENGE
    // - challengeId: 현재 챌린지 ID
    // - 나머지 필드(distance, durationSec, pace, bpm, calories, route, laps)는 개인 운동 결과 저장과 동일
    LaunchedEffect(sessionStartTime, challengeId) {
        if (challengeId <= 0) return@LaunchedEffect
        if (posted) return@LaunchedEffect
        val first = sessionViewModel.tryMarkPosted()
        if (!first) return@LaunchedEffect
        posted = true

        val runType = "CHALLENGE"
        val startMs = sessionStartTime ?: System.currentTimeMillis() - metrics.totalElapsedMs
        val endMs = startMs + metrics.totalElapsedMs
        val routeJson = try { Gson().toJson(route) } catch (_: Throwable) { "[]" } ?: "[]"
        val avgPace = metrics.avgPaceSecPerKm?.toInt() ?: 0
        val avgBpm = metrics.avgHeartRate ?: 0
        val lapRequests = laps.map {
            RunLapCreateRequest(
                sequence = it.sequence,
                distance = it.distanceMeters / 1000.0,
                durationSec = it.durationSec,
                pace = it.paceSecPerKm,
                bpm = it.bpm
            )
        }
        val req = RunCreateRequest(
            runType = runType,
            distance = metrics.distanceMeters / 1000.0,
            durationSec = (metrics.activeElapsedMs / 1000L).toInt(),
            startAt = java.time.Instant.ofEpochMilli(startMs).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().toString(),
            endAt = java.time.Instant.ofEpochMilli(endMs).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().toString(),
            pace = avgPace,
            bpm = avgBpm,
            calories = metrics.caloriesKcal,
            route = routeJson,
            laps = lapRequests,
            challengeId = challengeId
        )
        val repository = RunRepository()
        val result = runCatching { repository.createRun(req) }.getOrNull()
        when (result) {
            is ApiResponse.Success -> {
                Timber.d("createRun (challenge) success")
            }
            is ApiResponse.Error -> {
                Timber.w("createRun (challenge) failed: code=${result.code}, message=${result.message}")
            }
            ApiResponse.NetworkError -> {
                Timber.w("createRun (challenge) failed: network error")
            }
            null -> {
                Timber.w("createRun (challenge) failed: null response")
            }
        }
    }

    val participants by socketViewModel.participants.collectAsState()

    val visibleRanking = remember(participants) {
        val base = participants
            .filterNot { it.isRetired && it.distanceKm <= 0.0 }
            .filter { it.rank > 0 }
            .sortedBy { it.rank }
        val me = base.find { it.isMe }
        val myRank = me?.rank ?: 0
        if (myRank <= 0) emptyList() else base.filter { p -> p.rank in 1..myRank }
    }

    val myFinalRank = visibleRanking.find { it.isMe }?.rank ?: 0

    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var routeLine by remember { mutableStateOf<RouteLine?>(null) }
    var routeStyles by remember { mutableStateOf<RouteLineStyles?>(null) }
    var markerLayer by remember { mutableStateOf<LabelLayer?>(null) }
    var markerStyles by remember { mutableStateOf<LabelStyles?>(null) }
    var startPointLabel by remember { mutableStateOf<Label?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        BackHandler(enabled = true) { /* consume back */ }
        PageHeader(title = "챌린지 결과", onClose = onClose)
        HorizontalDivider(color = Color(0xFFDDDDDD))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
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
                                    @SuppressLint("MissingPermission")
                                    override fun onMapReady(map: KakaoMap) {
                                        kakaoMap = map
                                        markerLayer = map.getLabelManager()?.getLayer()
                                        if (markerStyles == null) {
                                            markerStyles = map.getLabelManager()?.addLabelStyles(
                                                LabelStyles.from(
                                                    LabelStyle.from(com.example.runnity.R.drawable.ic_my_location_dot)
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
                                            val curr = sessionViewModel.currentLocation.value
                                            if (curr != null) {
                                                val target = LatLng.from(curr.latitude, curr.longitude)
                                                map.moveCamera(CameraUpdateFactory.newCenterPosition(target))
                                                map.moveCamera(CameraUpdateFactory.zoomTo(16))
                                            } else {
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "최종 순위",
                    style = Typography.Subtitle,
                    color = ColorPalette.Light.secondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                val rankText = if (myFinalRank > 0) "${myFinalRank}위" else "--위"
                Text(
                    text = rankText,
                    style = Typography.LargeTitle.copy(fontSize = 72.sp),
                    color = ColorPalette.Light.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                            Text("이동거리", style = Typography.Caption, color = ColorPalette.Light.secondary)
                            Text(formatDistanceKm(metrics.distanceMeters), style = Typography.Title)
                        }
                        Column(
                            modifier = Modifier.width(half),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("시간", style = Typography.Caption, color = ColorPalette.Light.secondary)
                            Text(formatElapsed(metrics.activeElapsedMs), style = Typography.Title)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val third = this.maxWidth / 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.width(third),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("평균 페이스", style = Typography.Caption, color = ColorPalette.Light.secondary)
                            val paceText = metrics.avgPaceSecPerKm?.let { formatPace(it) } ?: "--:--/km"
                            Text(paceText, style = Typography.Title)
                        }
                        Column(
                            modifier = Modifier.width(third),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("평균 심박수", style = Typography.Caption, color = ColorPalette.Light.secondary)
                            Text(metrics.avgHeartRate?.toString() ?: "--", style = Typography.Title)
                        }
                        Column(
                            modifier = Modifier.width(third),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("칼로리", style = Typography.Caption, color = ColorPalette.Light.secondary)
                            Text(formatCalories(metrics.caloriesKcal), style = Typography.Title)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "랭킹",
                    style = Typography.Subtitle,
                    color = ColorPalette.Light.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    visibleRanking.forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = p.nickname,
                                style = Typography.Body,
                                color = ColorPalette.Light.primary,
                                maxLines = 1,
                                modifier = Modifier.weight(3f)
                            )

                            val paceText = p.paceSecPerKm?.takeIf { it > 0.0 }?.let { formatPace(it) } ?: "--:--/km"
                            Text(
                                text = paceText,
                                style = Typography.Caption,
                                color = ColorPalette.Light.secondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(2.5f)
                            )

                            val distanceText = if (p.distanceKm > 0.0) String.format("%.2fKM", p.distanceKm) else "0.00KM"
                            Text(
                                text = distanceText,
                                style = Typography.Caption,
                                color = ColorPalette.Light.secondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(2.5f)
                            )

                            val badgeText = if (p.isMe) "나" else "${p.rank}위"
                            Text(
                                text = badgeText,
                                style = Typography.Caption,
                                color = if (p.isMe) ColorPalette.Common.accent else ColorPalette.Light.secondary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDistanceKm(meters: Double): String {
    val km = meters / 1000.0
    return String.format("%.1f", km)
}

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

private fun formatCalories(kcal: Double): String = String.format("%.0f", kcal)
