package com.example.runnity.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {
    // 권한 리스트
    val LocationPermissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    // 위치 권한 체크
    fun hasLocationPermission(context: Context): Boolean =
        LocationPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    // 위치 권한 설명 체크
    fun shouldShowLocationRationale(activity: Activity): Boolean =
        LocationPermissions.any { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }

    // 앱 설정으로 이동 (권한 영구적 거부될 경우)
    fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

// 위치 권한 요청
@Composable
fun rememberLocationPermissionLauncher(
    onResult: (granted: Boolean) -> Unit
): ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>> {
    return rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        onResult(result.values.all { it })
    }
}

// 알림 권한 체크
fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= 33) {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

// 알림 권한 요청
@Composable
fun rememberNotificationPermissionLauncher(
    onResult: (granted: Boolean) -> Unit
): ManagedActivityResultLauncher<String, Boolean> {
    return rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> onResult(granted) }
}

// 위치 권한 요청
fun requestLocationPermissions(
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
) {
    launcher.launch(PermissionUtils.LocationPermissions)
}

