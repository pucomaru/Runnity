import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize") // ✅ Parcelable data class 용 (Intent 등에서 사용)

}

// 추가
val properties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}

android {
    namespace = "com.example.runnity"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.runnity"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 추가: Kakao Map Key를 BuildConfig와 Manifest 모두에 주입
        // BuildConfig(String) 값에는 반드시 따옴표가 포함되어야 합니다.
        buildConfigField(
            "String",
            "KAKAO_MAP_KEY",
            "\"${properties.getProperty("KAKAO_MAP_KEY")}\""
        )
        // AndroidManifest.xml의 ${KAKAO_MAP_KEY} 치환용
        manifestPlaceholders["KAKAO_MAP_KEY"] = properties.getProperty("KAKAO_MAP_KEY")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation.layout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // 추가
    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Material Icons Extended (추가 아이콘 사용)
    implementation("androidx.compose.material:material-icons-extended")

    // Retrofit + OkHttp (API 통신)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Coroutine (비동기)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Google Play Services - Fused Location Provider (현재 위치 획득)
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Coil
    implementation(libs.coil.compose)

    // Gson (Json 파싱)
    implementation(libs.gson)

    // EncryptedSharedPreferences (보안 토큰 저장)
    implementation(libs.security.crypto)

    // Timber (로깅)
    implementation(libs.timber)

    // 카카오 지도 API
    implementation("com.kakao.maps.open:android:2.12.18")
    // Splash Screen API (Android 12+)
    implementation("androidx.core:core-splashscreen:1.0.1")

    //  테스트 라이브러리
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}


