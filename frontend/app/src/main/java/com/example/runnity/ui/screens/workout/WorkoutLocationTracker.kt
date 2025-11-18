package com.example.runnity.ui.screens.workout

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

// 위치 추적기
class WorkoutLocationTracker(context: Context) {
    private val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var callback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun start(onLocation: (lat: Double, lon: Double, elapsedMs: Long?, accuracy: Float?, speed: Float?) -> Unit) {
        if (callback != null) return
        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1000L)
            .setMinUpdateDistanceMeters(3f)
            .build()
        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                onLocation(loc.latitude, loc.longitude, loc.elapsedRealtimeNanos?.div(1_000_000), loc.accuracy, loc.speed)
            }
        }
        fusedClient.requestLocationUpdates(request, callback as LocationCallback, Looper.getMainLooper())
    }

    fun stop() {
        val cb = callback ?: return
        fusedClient.removeLocationUpdates(cb)
        callback = null
    }
}
