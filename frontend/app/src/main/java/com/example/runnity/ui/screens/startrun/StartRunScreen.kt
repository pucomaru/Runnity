package com.example.runnity.ui.screens.startrun

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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

/**
 * 개인 러닝 시작 화면
 * - 러닝 시작 준비 화면
 * - 목표 설정, 경로 선택 등
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun StartRunScreen(
    parentNavController: NavController? = null, // 러닝 화면 이동 시 사용 예정
    viewModel: StartRunViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity

    var showLocationRationale by remember { mutableStateOf(false) }
    var showLocationSettings by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    val locationLauncher = rememberLocationPermissionLauncher { granted ->
        if (granted) {
            if (Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission(context)) {
                showNotificationDialog = true
            }
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

        // 지도 영역 Placeholder (SDK 연동 전까지 임시 박스)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // 지도 배경 대체
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ColorPalette.Light.containerBackground)
            )

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
                                // TODO: 권한 모두 OK → 러닝 시작 처리 (서비스 시작 등)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                SmallPillButton(
                    text = "목표 설정",
                    onClick = { /* TODO: 목표 설정 화면 이동 */ }
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
}
