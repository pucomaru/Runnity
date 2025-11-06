package com.example.runnity.ui.components

import android.app.Activity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.runnity.utils.PermissionUtils

// 위치 권한 요청 다이얼로그
@Composable
fun LocationPermissionDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onRequest: () -> Unit,
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "위치 권한 필요") },
        text = { Text(text = "러닝 기록을 위해 앱 사용 중 위치 권한이 필요합니다.") },
        confirmButton = {
            TextButton(onClick = onRequest) { Text("권한 허용") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

// 위치 권한 영구 거부 다이얼로그
@Composable
fun LocationDeniedPermanentlyDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "설정에서 권한 허용") },
        text = { Text(text = "권한이 거부되었습니다. 설정 > 앱 권한에서 위치 권한을 허용해 주세요.") },
        confirmButton = {
            TextButton(onClick = {
                PermissionUtils.openAppSettings(context)
                onDismiss()
            }) { Text("설정 열기") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("닫기") }
        }
    )
}

@Composable
fun NotificationPermissionDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onRequest: () -> Unit,
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "알림 권한 필요") },
        text = { Text(text = "러닝 기록을 유지하기 위해, 달리는 동안 상단에 진행 알림을 표시할 수 있도록 허용해 주세요.") },
        confirmButton = {
            TextButton(onClick = onRequest) { Text("권한 허용") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}
