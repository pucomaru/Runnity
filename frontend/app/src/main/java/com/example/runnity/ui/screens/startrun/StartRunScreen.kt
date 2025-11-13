package com.example.runnity.ui.screens.startrun

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.theme.ColorPalette
import com.example.runnity.ui.components.PageHeader
import com.example.runnity.ui.components.PrimaryButton
import com.example.runnity.ui.components.SmallPillButton
import com.example.runnity.ui.components.LocationPermissionDialog
import com.example.runnity.ui.components.LocationDeniedPermanentlyDialog
import com.example.runnity.ui.components.NotificationPermissionDialog
import com.example.runnity.utils.PermissionUtils
import com.example.runnity.utils.rememberLocationPermissionLauncher
import com.example.runnity.utils.rememberNotificationPermissionLauncher
import com.example.runnity.utils.requestLocationPermissions
import com.example.runnity.utils.hasNotificationPermission
import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.compose.ui.viewinterop.AndroidView
import com.example.runnity.data.datalayer.sendSessionControl
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraUpdateFactory
import android.annotation.SuppressLint
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles


// 개인 러닝 시작 화면
@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartRunScreen(
    navController: NavController? = null,
    parentNavController: NavController? = null, // 러닝 화면 이동 시 사용 예정
    viewModel: StartRunViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    // 권한 다이얼로그
    var showLocationRationale by remember { mutableStateOf(false) }
    var showLocationSettings by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    // 카카오 맵 실제 객체
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    // 내 현재 좌표
    var myLatLng by remember { mutableStateOf<LatLng?>(null) }
    // 카카오 맵 ui
    var mapView by remember { mutableStateOf<MapView?>(null) }
    // 내 위치 표시
    var myLocationLabel by remember { mutableStateOf<Label?>(null) }

    // 목표 설정 관련
    var showGoalSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val activeTab by viewModel.activeTab.collectAsState()
    val distanceText by viewModel.distanceText.collectAsState()
    val timeText by viewModel.timeMinutesText.collectAsState()

    // 현재 위치 1회 획득후 카카오 지도 카메라 이동!
    // 성공 시 내 위치 업데이트 및 카메라 이동(줌 16)
    @SuppressLint("MissingPermission")
    fun moveCameraToCurrentLocation() {
        if (!PermissionUtils.hasLocationPermission(context)) return
        val fused = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()
        Log.d("Location", "request getCurrentLocation start (HIGH_ACCURACY)")
        fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    Log.d("Location", "success lat=${loc.latitude}, lng=${loc.longitude}")
                    val target = LatLng.from(loc.latitude, loc.longitude)
                    myLatLng = target
                    kakaoMap?.let { mapRef ->
                        mapRef.moveCamera(CameraUpdateFactory.newCenterPosition(target))
                        mapRef.moveCamera(CameraUpdateFactory.zoomTo(16))
                    }

                    // 내 위치 라벨 생성/업데이트
                    val ll = kakaoMap?.getLabelManager()?.getLayer()
                    if (ll != null) {
                        if (myLocationLabel == null) {
                            val styles = kakaoMap?.getLabelManager()
                                ?.addLabelStyles(LabelStyles.from(LabelStyle.from(com.example.runnity.R.drawable.ic_my_location_dot)))
                            if (styles != null) {
                                val opts = LabelOptions.from(target).setStyles(styles)
                                myLocationLabel = ll.addLabel(opts)
                                myLocationLabel?.scaleTo(0.15f, 0.15f)
                            }
                        } else {
                            myLocationLabel?.moveTo(target)
                            myLocationLabel?.scaleTo(0.15f, 0.15f)
                        }
                    }
                } else {
                    Log.d("Location", "result is NULL")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Location", "getCurrentLocation failure: ${e.message}", e)
            }
    }

    val locationLauncher = rememberLocationPermissionLauncher { granted ->
        if (granted) {
            if (Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission(context)) {
                showNotificationDialog = true
            }
            moveCameraToCurrentLocation()
        } else {
            // 거절됨: 다시 묻지 않음 여부 확인
            val shouldShow = PermissionUtils.shouldShowLocationRationale(activity)
            showLocationRationale = shouldShow
            showLocationSettings = !shouldShow
        }
    }

    val notificationLauncher = rememberNotificationPermissionLauncher { /* granted */ _ -> }

    // 진입 시: 위치 권한 없으면 즉시 요청, 필요 시 알림 권한 이어서
    LaunchedEffect(Unit) {
        if (!PermissionUtils.hasLocationPermission(context)) {
            if (PermissionUtils.shouldShowLocationRationale(activity)) {
                showLocationRationale = true
            } else {
                requestLocationPermissions(locationLauncher)
            }
        } else if (Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission(context)) {
            showNotificationDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPalette.Light.background)
    ) {
        PageHeader(
            title = "개인 러닝"
        )

        // 지도 영역
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapView = this
                        // Kakao 지도 시작: 생명주기/준비 콜백 등록
                        start(
                            object : MapLifeCycleCallback() {
                                override fun onMapDestroy() { }
                                // 인증 실패/지도 오류 시 여기로 전달됨
                                override fun onMapError(error: Exception) { }
                            },
                            object : KakaoMapReadyCallback() {
                                override fun onMapReady(map: KakaoMap) {
                                    kakaoMap = map
                                    // label은 생성 시점에 레이어를 지역에서 확보하여 사용
                                    // 권한 허용 시 현재 위치 요청 → 성공하면 카메라 이동
                                    if (PermissionUtils.hasLocationPermission(context)) {
                                        moveCameraToCurrentLocation()
                                    }
                                }
                            }
                        )
                    }
                }
            )

            // Compose에서 MapView 생명주기 연동
            // - ON_RESUME → resume(), ON_PAUSE → pause()
            DisposableEffect(lifecycleOwner, mapView) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> mapView?.resume()
                        Lifecycle.Event.ON_PAUSE -> mapView?.pause()
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }



            // 지도 위에 떠 있는 하단 액션 버튼 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryButton(
                    text = "운동 시작",
                    onClick = {
                        when {
                            !PermissionUtils.hasLocationPermission(context) -> {
                                if (PermissionUtils.shouldShowLocationRationale(activity)) {
                                    showLocationRationale = true
                                } else {
                                    requestLocationPermissions(locationLauncher)
                                }
                            }
                            Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission(context) -> {
                                showNotificationDialog = true
                            }
                            else -> {
                                val goal = viewModel.resolveGoal()
                                val route = when (goal) {
                                    is Goal.Distance -> "countdown/personal?type=distance&km=${String.format("%.1f", goal.km)}"
                                    is Goal.Time -> "countdown/personal?type=time&min=${goal.minutes}"
                                    is Goal.FreeRun -> "countdown/personal"
                                }
                                // 워치 준비 -> 카운트다운(3초)
                                sendSessionControl(context, "prepare")
                                sendSessionControl(context, "countdown", seconds = 3)
                                navController?.navigate(route)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                SmallPillButton(
                    text = "목표 설정",
                    onClick = { showGoalSheet = true }
                )
            }
        }
    }

    // 위치 권한 설명 다이얼로그
    LocationPermissionDialog(
        visible = showLocationRationale,
        onDismiss = { showLocationRationale = false },
        onRequest = {
            showLocationRationale = false
            requestLocationPermissions(locationLauncher)
        }
    )

    // 위치 권한 영구 거부 → 설정 이동 유도
    LocationDeniedPermanentlyDialog(
        visible = showLocationSettings,
        onDismiss = { showLocationSettings = false }
    )

    // 13+ 알림 권한 안내 다이얼로그
    NotificationPermissionDialog(
        visible = showNotificationDialog,
        onDismiss = { showNotificationDialog = false },
        onRequest = {
            showNotificationDialog = false
            if (Build.VERSION.SDK_INT >= 33) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    )

    // 목표 설정 다이얼로그
    if (showGoalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showGoalSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallPillButton(
                        text = "거리",
                        selected = activeTab == StartRunViewModel.GoalTab.Distance,
                        onClick = { viewModel.setActiveTab(StartRunViewModel.GoalTab.Distance) },
                        modifier = Modifier.weight(1f)
                    )
                    SmallPillButton(
                        text = "시간",
                        selected = activeTab == StartRunViewModel.GoalTab.Time,
                        onClick = { viewModel.setActiveTab(StartRunViewModel.GoalTab.Time) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 목표 설정
                when (activeTab) {
                    StartRunViewModel.GoalTab.Distance -> {
                        OutlinedTextField(
                            value = distanceText,
                            onValueChange = { new ->
                                val filtered = new.filter { it.isDigit() || it == '.' }
                                val parts = filtered.split('.')
                                val normalized = when (parts.size) {
                                    1 -> parts[0]
                                    2 -> parts[0].take(3) + "." + parts[1].take(1)
                                    else -> parts[0] + "." + parts[1]
                                }
                                viewModel.setDistanceText(normalized)
                            },
                            label = { Text("거리 (km)") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    StartRunViewModel.GoalTab.Time -> {
                        OutlinedTextField(
                            value = timeText,
                            onValueChange = { new ->
                                val filtered = new.filter { it.isDigit() }
                                viewModel.setTimeMinutesText(filtered)
                            },
                            label = { Text("시간 (분)") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(
                    text = "운동하기",
                    onClick = {
                        val goal = viewModel.resolveGoal()
                        val route = when (goal) {
                            is Goal.Distance -> "countdown/personal?type=distance&km=${String.format("%.1f", goal.km)}"
                            is Goal.Time -> "countdown/personal?type=time&min=${goal.minutes}"
                            is Goal.FreeRun -> "countdown/personal"
                        }
                        showGoalSheet = false
                        // 워치 준비 -> 카운트다운(3초)
                        sendSessionControl(context, "prepare")
                        sendSessionControl(context, "countdown", seconds = 3)
                        navController?.navigate(route)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
