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

    // 경로 전체가 보이도록 카메라를 맞춤 (경로 범위 기반 근사 확대 레벨)
    fun moveCameraToRoute(
        kakaoMap: KakaoMap,
        latLngs: List<LatLng>,
        fallbackZoom: Int = DEFAULT_ZOOM_LEVEL
    ) {
        if (latLngs.isEmpty()) return
        // 경로의 경계 박스 계산
        var minLat = latLngs.first().latitude
        var maxLat = latLngs.first().latitude
        var minLon = latLngs.first().longitude
        var maxLon = latLngs.first().longitude
        for (p in latLngs) {
            if (p.latitude < minLat) minLat = p.latitude
            if (p.latitude > maxLat) maxLat = p.latitude
            if (p.longitude < minLon) minLon = p.longitude
            if (p.longitude > maxLon) maxLon = p.longitude
        }
        val centerLat = (minLat + maxLat) / 2.0
        val centerLon = (minLon + maxLon) / 2.0
        val center = LatLng.from(centerLat, centerLon)

        // 위경도 범위에 따른 근사 줌 결정
        val latSpan = (maxLat - minLat).coerceAtLeast(1e-6)
        val lonSpan = (maxLon - minLon).coerceAtLeast(1e-6)
        val maxSpan = maxOf(latSpan, lonSpan)
        val zoom = when {
            maxSpan < 0.002 -> 17 // ~200m
            maxSpan < 0.005 -> 16 // ~500m
            maxSpan < 0.01 -> 15  // ~1km
            maxSpan < 0.05 -> 13  // ~5km
            maxSpan < 0.1 -> 12   // ~10km
            else -> fallbackZoom
        }
        kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(center))
        kakaoMap.moveCamera(CameraUpdateFactory.zoomTo(zoom))
    }
}
