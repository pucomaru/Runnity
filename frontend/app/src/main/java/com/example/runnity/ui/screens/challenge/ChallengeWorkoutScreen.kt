package com.example.runnity.ui.screens.challenge

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import timber.log.Timber
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import androidx.navigation.NavController
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.PrimaryButton
import com.example.runnity.ui.components.TabBar
import com.example.runnity.socket.WebSocketManager
import com.example.runnity.data.datalayer.SessionMetricsBus
import com.example.runnity.ui.screens.workout.WorkoutLocationTracker
import com.example.runnity.ui.screens.workout.WorkoutPhase
import com.example.runnity.ui.screens.workout.WorkoutSessionViewModel
import com.example.runnity.utils.PermissionUtils
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

@Composable
fun ChallengeWorkoutScreen(
    challengeId: String,
    navController: NavController,
    socketViewModel: ChallengeSocketViewModel,
    sessionViewModel: WorkoutSessionViewModel
) {
    val challengeViewModel: ChallengeViewModel = viewModel()
    val metrics by sessionViewModel.metrics.collectAsState()
    val currentPace by sessionViewModel.currentPaceSecPerKm.collectAsState()
    val currentLoc by sessionViewModel.currentLocation.collectAsState()
    val phase by sessionViewModel.phase.collectAsState()
    val websocketState by WebSocketManager.state.collectAsState()

    val challengeDetailState by challengeViewModel.challengeDetail.collectAsState()
    val participantsState by socketViewModel.participants.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val tracker = remember(context) { WorkoutLocationTracker(context) }

    // 목표 거리(km) 캐시 및 최종 RECORD 전송 여부 플래그
    var targetKm by remember { mutableStateOf<Double?>(null) }
    var finalRecordSent by remember { mutableStateOf(false) }
    // 최근 전송 실패한 RECORD/Pace 데이터를 임시로 보관해두었다가
    // 소켓이 다시 열렸을 때 한 번만 재전송하기 위한 버퍼
    var pendingRecord by remember { mutableStateOf<String?>(null) }
    var pendingFinalRecord by remember { mutableStateOf<String?>(null) }

    val challengeIdLong = challengeId.toLongOrNull() ?: 0L

    // 챌린지 상세 정보 로드 (목표 거리 확인용) 및 실시간 참가자/랭킹 관찰 시작
    LaunchedEffect(challengeIdLong) {
        if (challengeIdLong > 0) {
            challengeViewModel.loadChallengeDetail(challengeIdLong)
            socketViewModel.observeSession(challengeIdLong)
        }
    }

    // 세션 시작 (챌린지용: 우선 자유 러닝처럼 시작, 이후 상세 정보 도착 시 목표 거리 설정)
    var hasStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        sessionViewModel.resetSession()
        sessionViewModel.start()
        hasStarted = true
        tracker.start { lat, lon, elapsedMs, acc, speed ->
            sessionViewModel.ingestLocation(lat, lon, elapsedMs, acc, speed)
        }
    }

    // 워치에서 올라오는 메트릭을 챌린지 세션에도 동일하게 적용
    LaunchedEffect(Unit) {
        SessionMetricsBus.events.collectLatest { m ->
            sessionViewModel.ingestWatchMetrics(
                hrBpm = m.hrBpm,
                distanceM = m.distanceM,
                elapsedMs = m.elapsedMs,
                paceSpKm = m.paceSpKm,
                caloriesKcal = m.caloriesKcal
            )
        }
    }

    // 챌린지 상세 정보의 distance enum을 기준으로 목표 거리(km)를 설정
    LaunchedEffect(challengeDetailState) {
        val detail = challengeDetailState ?: return@LaunchedEffect
        val goalKm = mapDistanceEnumToKm(detail.distance)
        if (goalKm > 0.0) {
            targetKm = goalKm
            sessionViewModel.setGoal("distance", goalKm.toString(), null)
        }
    }

    // 주기적으로 RECORD 메시지 전송 (예: 5초마다)
    LaunchedEffect(targetKm) {
        val goalKm = targetKm ?: return@LaunchedEffect
        while (true) {
            delay(5000L)
            if (phase != WorkoutPhase.Running) continue

            val distanceKm = metrics.distanceMeters / 1000.0
            if (distanceKm <= 0.0) continue

            // 목표 거리를 초과한 이후에는 추가 RECORD 전송 중단 (최종 RECORD는 Ended 감지 시 별도 전송)
            if (distanceKm >= goalKm) continue

            val paceToSend = currentPace ?: metrics.avgPaceSecPerKm ?: 0.0
            if (paceToSend <= 0.0) continue

            val recordJson = "{" +
                "\"type\":\"RECORD\"," +
                "\"distance\":" + distanceKm + "," +
                "\"pace\":" + paceToSend + "," +
                "\"timestamp\":" + System.currentTimeMillis() +
                "}"

            // 랭킹/UI는 전송 성공 여부와 상관없이 항상 최신 내 기록을 반영
            socketViewModel.updateMyStats(distanceKm = distanceKm, paceSecPerKm = paceToSend)

            // 최근 RECORD를 버퍼에 넣어두고, 실제 전송이 성공한 경우에만 버퍼를 비운다.
            pendingRecord = recordJson
            if (WebSocketManager.isOpen && WebSocketManager.send(recordJson)) {
                pendingRecord = null
            }
        }
    }

    // 세션이 Ended로 전환될 때 최종 RECORD를 한 번만 전송하고 결과 화면으로 이동
    LaunchedEffect(phase, targetKm) {
        val goalKm = targetKm ?: return@LaunchedEffect
        if (phase == WorkoutPhase.Ended) {
            val distanceKm = metrics.distanceMeters / 1000.0
            val paceToSend = currentPace ?: metrics.avgPaceSecPerKm ?: 0.0
            if (!finalRecordSent && distanceKm > 0.0 && paceToSend > 0.0) {
                val recordJson = "{" +
                    "\"type\":\"RECORD\"," +
                    "\"distance\":" + distanceKm + "," +
                    "\"pace\":" + paceToSend + "," +
                    "\"timestamp\":" + System.currentTimeMillis() +
                    "}"

                // 최종 기록도 랭킹에는 항상 반영
                socketViewModel.updateMyStats(distanceKm = distanceKm, paceSecPerKm = paceToSend)

                // 최종 RECORD도 동일하게 OPEN일 때만 송신하고 실패 시 버퍼에 유지
                pendingFinalRecord = recordJson
                val sent = WebSocketManager.isOpen && WebSocketManager.send(recordJson)
                if (sent) {
                    finalRecordSent = true
                    pendingFinalRecord = null
                }
            }

            // 목표 거리 이상을 채운 정상 종료인 경우에만 결과 화면으로 이동
            if (distanceKm >= goalKm) {
                navController.navigate("challenge_result/$challengeId") {
                    popUpTo("challenge_workout/$challengeId") { inclusive = true }
                }
            }
        }
    }

    // (서버 요구사항: 재접속 이후의 최신 데이터만 필요)
    // 웹소켓 상태가 Open으로 전환될 때, 버퍼에 남아 있던 최신 RECORD/최종 RECORD를 한 번만 재전송
    LaunchedEffect(websocketState) {
        if (websocketState is WebSocketManager.WsState.Open) {
            pendingRecord?.let { latest ->
                if (WebSocketManager.send(latest)) {
                    pendingRecord = null
                }
            }

            pendingFinalRecord?.let { latest ->
                if (WebSocketManager.send(latest)) {
                    pendingFinalRecord = null
                    finalRecordSent = true
                }
            }
        }
    }

    // 화면 종료 시 세션/트래커 정리
    DisposableEffect(Unit) {
        onDispose {
            tracker.stop()
            if (phase != WorkoutPhase.Ended) {
                sessionViewModel.stop()
            }
        }
    }

    // 뒤로가기 막고, 반드시 "챌린지 나가기" 버튼을 통해 종료하도록 강제
    BackHandler(enabled = true) {
        // consume back press
    }

    var selectedTab by remember { mutableStateOf(0) } // 0 = 운동, 1 = 지도

    // Kakao Map refs (지도 탭에서 사용)
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var myLocationLabel by remember { mutableStateOf<Label?>(null) }
    var labelLayer by remember { mutableStateOf<LabelLayer?>(null) }
    var labelStyles by remember { mutableStateOf<LabelStyles?>(null) }

    // 현재 위치 변경 시 지도 카메라/라벨 이동
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

    // 탭 전환 시 MapView 상태 동기화
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

    val lifecycleOwner = LocalLifecycleOwner.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPalette.Light.background)
    ) {
        // 탭 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            TabBar(
                selectedIndex = selectedTab,
                onSelected = { selectedTab = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 탭 아래 영역의 상단 절반: 운동/지도 콘텐츠
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White)
        ) {
            if (selectedTab == 0) {
                // 운동 탭: 현재 순위 + 메트릭 (WorkoutPersonalScreen 메트릭 레이아웃 참고)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "현재 순위",
                            style = Typography.Subtitle.copy(fontSize = 20.sp),
                            color = ColorPalette.Light.secondary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val myRankText = remember(participantsState) {
                            val me = participantsState.firstOrNull { it.isMe }
                            if (me?.rank != null && me.rank > 0) "${me.rank}위" else "--위"
                        }

                        Text(
                            text = myRankText,
                            style = Typography.LargeTitle.copy(fontSize = 38.sp),
                            color = ColorPalette.Light.primary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        val distanceKm = formatDistanceKm(metrics.distanceMeters)
                        val calories = formatCalories(metrics.caloriesKcal)
                        val bpm = metrics.avgHeartRate?.toString() ?: "--"
                        val paceText = currentPace?.let { formatPace(it) } ?: "-'--\""
                        val timeText = formatElapsedCompact(metrics.activeElapsedMs)

                        // 1행: 이동거리 + 페이스 + 시간
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "이동거리",
                                    style = Typography.Caption,
                                    color = ColorPalette.Light.secondary
                                )
                                Text(
                                    distanceKm + "km",
                                    style = Typography.Title
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "페이스",
                                    style = Typography.Caption,
                                    color = ColorPalette.Light.secondary
                                )
                                Text(
                                    paceText,
                                    style = Typography.Title
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "시간",
                                    style = Typography.Caption,
                                    color = ColorPalette.Light.secondary
                                )
                                Text(
                                    timeText,
                                    style = Typography.Title
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2행: BPM + 칼로리
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "BPM",
                                    style = Typography.Caption,
                                    color = ColorPalette.Light.secondary
                                )
                                Text(
                                    bpm,
                                    style = Typography.Title
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "칼로리",
                                    style = Typography.Caption,
                                    color = ColorPalette.Light.secondary
                                )
                                Text(
                                    calories + "Kcal",
                                    style = Typography.Title
                                )
                            }
                        }
                    }
                }
            } else {
                // 지도 탭: WorkoutPersonalScreen과 동일한 Kakao Map 구성
                Box(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.ui.viewinterop.AndroidView(
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
                                            myLocationLabel = null
                                            labelLayer = map.getLabelManager()?.getLayer()
                                            if (labelStyles == null) {
                                                labelStyles = map.getLabelManager()?.addLabelStyles(
                                                    LabelStyles.from(
                                                        LabelStyle.from(com.example.runnity.R.drawable.ic_my_location_dot)
                                                    )
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
                                            if (currentLoc == null && PermissionUtils.hasLocationPermission(ctx)) {
                                                val fused = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(ctx)
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

                    // MapView lifecycle binding
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
        }

        // 탭 아래 영역의 하단 절반: 실시간 랭킹 + 챌린지 나가기 버튼
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(ColorPalette.Light.containerBackground)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 왼쪽: 실시간 랭킹 + 챌린지 이름(목표거리)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "실시간 랭킹",
                            style = Typography.Subtitle,
                            color = ColorPalette.Light.secondary
                        )

                        // 챌린지 이름 + 목표 거리
                        val challengeName = challengeDetailState?.title ?: ""
                        val distanceLabel = targetKm?.let { km ->
                            when {
                                km == 21.0975 -> "하프"
                                km < 1.0 -> "${(km * 1000).toInt()}m"
                                else -> "${km.toInt()}km"
                            }
                        } ?: ""

                        if (challengeName.isNotEmpty() && distanceLabel.isNotEmpty()) {
                            Text(
                                text = "$challengeName($distanceLabel)",
                                style = Typography.Caption,
                                color = ColorPalette.Light.secondary
                            )
                        }
                    }

                    // 오른쪽: 인원수
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "참여 인원",
                            tint = ColorPalette.Light.component,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${participantsState.size}",
                            style = Typography.Caption,
                            color = ColorPalette.Light.secondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                val rankingRows = remember(participantsState) {
                    selectRankingRows(participantsState)
                }

                LaunchedEffect(participantsState) {
                    timber.log.Timber.d(
                        "[ChallengeWorkout] participantsState size=%d, rankingRows size=%d",
                        participantsState.size,
                        rankingRows.size
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    rankingRows.forEach { p ->
                        RankingRow(participant = p)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            PrimaryButton(
                text = "챌린지 나가기",
                onClick = {
                    val quitJson = "{" +
                        "\"type\":\"QUIT\"," +
                        "\"timestamp\":" + System.currentTimeMillis() +
                        "}"
                    WebSocketManager.send(quitJson)
                    WebSocketManager.close()
                    sessionViewModel.stop()
                    navController.navigate("home") {
                        // 홈 탭의 시작 화면으로 이동하면서, 중간 스택(대기방/카운트다운/운동)은 제거
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun TextMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(
            text = label,
            color = ColorPalette.Light.secondary,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = ColorPalette.Light.primary,
            style = Typography.Heading,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatDistanceKm(meters: Double): String {
    val km = meters / 1000.0
    return String.format("%.2f", km)
}

// 누적 시간 포맷팅 (예: 00:41, 12:03, 1:05:20)
private fun formatElapsedCompact(ms: Long): String {
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
    return String.format("%d'%02d\"", m, s)
}

private fun formatCalories(kcal: Double): String = String.format("%.0f", kcal)

// 서버 ChallengeDistance enum(ONE, TWO, ... , HALF)을 km 숫자로 매핑
private fun mapDistanceEnumToKm(distance: String): Double = when (distance.uppercase()) {
    "ONE" -> 1.0
    "TWO" -> 2.0
    "THREE" -> 3.0
    "FOUR" -> 4.0
    "FIVE" -> 5.0
    "SIX" -> 6.0
    "SEVEN" -> 7.0
    "EIGHT" -> 8.0
    "NINE" -> 9.0
    "TEN" -> 10.0
    "FIFTEEN" -> 15.0
    // HALF 마라톤: 21.0975km
    "HALF" -> 21.0975
    "M100" -> 0.1
    "M500" -> 0.5
    else -> 0.0
}

@Composable
private fun RankingRow(participant: Participant) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 왼쪽: 순위 + 프로필 + 닉네임 + 배지
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.weight(1f)
        ) {
            // 순위
            Text(
                text = if (participant.rank > 0) "${participant.rank}위" else "--",
                style = Typography.Caption.copy(fontSize = 14.sp),
                color = when (participant.rank) {
                    1 -> Color(0xFFFFD700) // 금색
                    2 -> Color(0xFFC0C0C0) // 은색
                    3 -> Color(0xFFCD7F32) // 동색
                    else -> ColorPalette.Light.primary
                },
                modifier = Modifier.width(38.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 프로필 이미지
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ColorPalette.Light.containerBackground)
                    .border(
                        width = if (participant.isMe) 2.dp else 1.dp,
                        color = if (participant.isMe) ColorPalette.Common.accent else ColorPalette.Light.component,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!participant.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = participant.avatarUrl,
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
                        tint = if (participant.isMe) ColorPalette.Common.accent else ColorPalette.Light.component,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 닉네임 + 나 배지
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = participant.nickname,
                    style = Typography.Body.copy(fontSize = 14.sp),
                    color = ColorPalette.Light.primary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                if (participant.isMe) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = ColorPalette.Common.accent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "나",
                            style = Typography.Caption.copy(fontSize = 10.sp),
                            color = Color.White
                        )
                    }
                }

                if (participant.isRetired) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = ColorPalette.Light.secondary,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "리타이어",
                            style = Typography.Caption.copy(fontSize = 10.sp),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 오른쪽: 페이스 + 거리
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 페이스
            val paceText = participant.paceSecPerKm?.takeIf { it > 0.0 }?.let { formatPace(it) } ?: "-'--\""
            Text(
                text = paceText,
                style = Typography.Caption,
                color = ColorPalette.Light.secondary,
                textAlign = TextAlign.End
            )

            // 거리
            val distanceText = if (participant.distanceKm > 0.0) String.format("%.2fkm", participant.distanceKm) else "0.00km"
            Text(
                text = distanceText,
                style = Typography.Caption,
                color = ColorPalette.Light.secondary,
                textAlign = TextAlign.End,
                modifier = Modifier.width(45.dp)
            )
        }
    }
}

// 거리 기준 정렬된 Participant 리스트에서 최대 4개 행 선택
private fun selectRankingRows(list: List<Participant>): List<Participant> {
    if (list.isEmpty()) return emptyList()

    // 운동 랭킹에서는 "대기방에서만 나갔다가 한 번도 움직이지 않은" 참가자는 제외
    val visible = list.filterNot { it.isRetired && it.distanceKm <= 0.0 }
    if (visible.isEmpty()) return emptyList()

    val first = visible.first()
    val meIndex = visible.indexOfFirst { it.isMe }

    val base = mutableListOf<Participant>()

    // 1등은 항상 포함
    base += first

    if (meIndex >= 0) {
        // 내 앞
        if (meIndex - 1 >= 0) base += visible[meIndex - 1]
        // 나
        base += visible[meIndex]
        // 내 뒤
        if (meIndex + 1 < visible.size) base += visible[meIndex + 1]
    }

    // 중복 제거 (id 기준) + 순서 유지
    val seen = mutableSetOf<String>()
    val deduped = base.filter { p ->
        if (seen.contains(p.id)) false else {
            seen += p.id
            true
        }
    }.toMutableList()

    // 4명 미만이면 위에서부터 채우기
    if (deduped.size < 4) {
        visible.forEach { p ->
            if (deduped.size >= 4) return@forEach
            if (!seen.contains(p.id)) {
                seen += p.id
                deduped += p
            }
        }
    }

    return deduped.take(4)
}
