package com.example.runnity

import android.app.Application
import com.example.runnity.data.util.TokenManager
import timber.log.Timber

/**
 * 앱 전역 Application 클래스
 * - 앱 시작 시 가장 먼저 실행됨
 * - 전역 설정 및 라이브러리 초기화
 */
class GlobalApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Timber 로깅 라이브러리 초기화
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("Runnity 앱 시작")

        // TokenManager 초기화
        TokenManager.init(this)
    }
}
