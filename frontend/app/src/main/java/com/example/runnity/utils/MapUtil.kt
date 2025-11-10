package com.example.runnity.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.route.RouteLine
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle
import com.kakao.vectormap.route.RouteLineStyles
import com.example.runnity.ui.screens.workout.GeoPoint

// Kakao Map 유틸리티
object MapUtil {
    const val DEFAULT_ZOOM_LEVEL = 10

    // ViewModel의 GeoPoint -> LatLng 변환
    fun getLatLngRoutePathFromGeo(points: List<GeoPoint>): List<LatLng> =
        points.map { LatLng.from(it.latitude, it.longitude) }

    // RouteLine 스타일 (굵기/색상 지정)
    fun setRoutePathStyle(
        context: Context,
        colorResId: Int = android.R.color.holo_blue_light,
        widthDp: Float = 6f
    ): RouteLineStyles {
        val color = ContextCompat.getColor(context, colorResId)
        return RouteLineStyles.from(
            RouteLineStyle.from(widthDp, color)
        )
    }

    // RouteLine 그리기 헬퍼 (단일 세그먼트)
    fun drawRouteLine(
        kakaoMap: KakaoMap,
        latLngs: List<LatLng>,
        styles: RouteLineStyles
    ): RouteLine? {
        if (latLngs.size < 2) return null
        val layer = kakaoMap.routeLineManager?.layer ?: return null
        val segment = RouteLineSegment.from(latLngs).setStyles(styles)
        val options: RouteLineOptions = RouteLineOptions.from(segment)
        return layer.addRouteLine(options)?.also { it.show() }
    }

    // 경로 전체가 보이도록 카메라를 맞춤 (간단한 중심/줌)
    fun moveCameraToRoute(
        kakaoMap: KakaoMap,
        latLngs: List<LatLng>,
        fallbackZoom: Int = DEFAULT_ZOOM_LEVEL
    ) {
        if (latLngs.isEmpty()) return
        val center = latLngs.first()
        kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(center))
        kakaoMap.moveCamera(CameraUpdateFactory.zoomTo(fallbackZoom))
    }
}
