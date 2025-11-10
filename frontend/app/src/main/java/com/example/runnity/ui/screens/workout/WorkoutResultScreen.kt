package com.example.runnity.ui.screens.workout

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.PageHeader
import com.example.runnity.utils.MapUtil
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.route.RouteLine
import com.kakao.vectormap.route.RouteLineStyles
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.google.android.gms.location.LocationServices

// 운동 결과 화면
@Composable
fun WorkoutResultScreen(
    type: String?,
    km: String?,
    min: String?,
    onClose: (() -> Unit)? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val sessionViewModel: WorkoutSessionViewModel = viewModel(context as androidx.lifecycle.ViewModelStoreOwner)
    val metrics by sessionViewModel.metrics.collectAsState()
    val route by sessionViewModel.route.collectAsState()

    // 지도
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
        PageHeader(title = "러닝 결과", onClose = onClose)
        HorizontalDivider(color = Color(0xFFDDDDDD))

        // 헤더 아래 영역을 1:1로 상하 분할 (상: 지도, 하: 요약)
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val halfH = this.maxHeight / 2
            Column(modifier = Modifier.fillMaxSize()) {
                // 상단: 지도
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(halfH)
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
                } // end top map Box

                // 하단: 라벨 + 요약 (세로 배치)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(halfH)
                ) {
                    // 라벨 행
                    val modeLabel = when (type) {
                        "distance" -> "거리 달리기"
                        "time" -> "시간 달리기"
                        else -> "자유 달리기"
                    }
                    val startTime by sessionViewModel.sessionStartTime.collectAsState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = modeLabel, style = Typography.Subtitle, color = ColorPalette.Light.secondary)
                        val startText = startTime?.let { ts ->
                            val d = java.util.Date(ts)
                            val fmt = java.text.SimpleDateFormat("yyyy.MM.dd HH:mm:ss", java.util.Locale.getDefault())
                            fmt.format(d)
                        } ?: ""
                        Text(text = startText, style = Typography.Caption, color = ColorPalette.Light.secondary)
                    }

                    HorizontalDivider(color = Color(0xFFDDDDDD))

                    // 요약 내용
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (type == "time") {
                            Text(text = "총 시간", style = Typography.Subtitle, color = ColorPalette.Light.secondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = formatElapsedHM(metrics.totalElapsedMs), style = Typography.LargeTitle.copy(fontSize = 72.sp), color = ColorPalette.Light.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                val half = this.maxWidth / 2
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.width(half), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("평균 페이스", style = Typography.Caption, color = ColorPalette.Light.secondary)
                                        val paceText = metrics.avgPaceSecPerKm?.let { formatPace(it) } ?: "--:--/km"
                                        Text(paceText, style = Typography.Title)
                                    }
                                    Column(modifier = Modifier.width(half), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("거리", style = Typography.Caption, color = ColorPalette.Light.secondary)
                                        Text(formatDistanceKm(metrics.distanceMeters), style = Typography.Title)
                                    }
                                }
                            }
                        } else {
                            Text(text = "총 km", style = Typography.Subtitle, color = ColorPalette.Light.secondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = formatDistanceKm(metrics.distanceMeters), style = Typography.LargeTitle.copy(fontSize = 72.sp), color = ColorPalette.Light.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                val half = this.maxWidth / 2
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.width(half), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("평균 페이스", style = Typography.Caption, color = ColorPalette.Light.secondary)
                                        val paceText = metrics.avgPaceSecPerKm?.let { formatPace(it) } ?: "--:--/km"
                                        Text(paceText, style = Typography.Title)
                                    }
                                    Column(modifier = Modifier.width(half), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("시간", style = Typography.Caption, color = ColorPalette.Light.secondary)
                                        Text(formatElapsedHM(metrics.totalElapsedMs), style = Typography.Title)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val half = this.maxWidth / 2
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.width(half), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("평균 심박수", style = Typography.Caption, color = ColorPalette.Light.secondary)
                                    Text(metrics.avgHeartRate?.toString() ?: "--", style = Typography.Title)
                                }
                                Column(modifier = Modifier.width(half), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("칼로리", style = Typography.Caption, color = ColorPalette.Light.secondary)
                                    Text(formatCalories(metrics.caloriesKcal), style = Typography.Title)
                                }
                            }
                        }
                    }
                }
            } // end inner Column (map + summary)
        } // end BoxWithConstraints
    } // end outer Column
} // end composable


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

private fun formatElapsedHM(ms: Long): String {
    val totalSec = (ms / 1000).toInt()
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    return String.format("%02d:%02d", h, m)
}

private fun formatPace(secPerKm: Double): String {
    val total = secPerKm.toInt()
    val m = total / 60
    val s = total % 60
    return String.format("%d'%02d\"/km", m, s)
}

private fun formatCalories(kcal: Double): String = String.format("%.0f", kcal)
