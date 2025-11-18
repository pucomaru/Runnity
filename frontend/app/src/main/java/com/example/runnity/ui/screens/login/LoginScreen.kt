package com.example.runnity.ui.screens.login

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.BuildConfig
import com.example.runnity.R
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.PrimaryButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import timber.log.Timber

/**
 * 로그인 화면
 * - Google 소셜 로그인
 * - Kakao 소셜 로그인
 * - LoginViewModel과 연동되어 실제 API 호출
 */
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsState()

    // Google Sign-In 클라이언트 설정
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID) // local.properties의 GOOGLE_CLIENT_ID 사용
        .requestEmail()
        .build()

    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    // Google Sign-In Activity Result Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken

                if (idToken != null) {
                    Timber.d("Google Login Success: idToken=$idToken")
                    // Backend로 idToken 전송
                    viewModel.loginWithGoogle(idToken)
                } else {
                    Timber.e("Google Login Failed: idToken is null")
                    Toast.makeText(context, "Google 로그인 실패: ID Token을 가져올 수 없습니다", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Timber.e("Google Sign-In failed: ${e.statusCode} - ${e.message}")
                Toast.makeText(context, "Google 로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            val resultCode = result.resultCode
            val intent = result.data
            if (resultCode == Activity.RESULT_CANCELED) {
                var toastMessage = "Google 로그인 취소됨"
                Timber.w("Google Sign-In cancelled by user")
                if (intent != null) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                    try {
                        task.getResult(ApiException::class.java)
                    } catch (e: ApiException) {
                        val statusString = GoogleSignInStatusCodes.getStatusCodeString(e.statusCode)
                        Timber.w("Google Sign-In cancelled: status=${e.statusCode}($statusString), message=${e.message}")
                        toastMessage = "Google 로그인 취소: $statusString"
                    }
                }
                Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            } else {
                Timber.w("Google Sign-In failed with resultCode=$resultCode")
                Toast.makeText(context, "Google 로그인 실패 (code=$resultCode)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Kakao Login Callback
    val kakaoCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Timber.e("Kakao Login Failed: ${error.message}")
            Toast.makeText(context, "Kakao 로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
        } else if (token != null) {
            val idToken = token.idToken
            if (idToken != null) {
                Timber.d("Kakao Login Success: idToken received")
                // Backend로 Kakao ID Token 전송
                viewModel.loginWithKakao(idToken)
            } else {
                Timber.e("Kakao Login Failed: idToken is null (OpenID Connect가 활성화되지 않았을 수 있습니다)")
                Toast.makeText(
                    context,
                    "Kakao 로그인 실패: ID Token을 가져올 수 없습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // UI 상태에 따른 화면 전환
    LaunchedEffect(uiState) {
        when (uiState) {
            LoginUiState.Success -> {
                Timber.d("LoginScreen: navigate to main")
                // 로그인 성공 - 메인 화면으로 이동
                navController.navigate("main") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            LoginUiState.NeedAdditionalInfo -> {
                Timber.d("LoginScreen: navigate to profile_setup")
                // 추가 정보 입력 필요 - 프로필 설정 화면으로 이동
                navController.navigate("profile_setup") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is LoginUiState.Error -> {
//                Timber.e("LoginScreen: error state message=${uiState.message}")
                // 에러 메시지 표시
                Toast.makeText(
                    context,
                    (uiState as LoginUiState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
                // 에러 상태 초기화
                viewModel.resetErrorState()
            }
            else -> {}
        }
    }

    // 로딩 중일 때 ProgressBar 표시
    if (uiState == LoginUiState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorPalette.Light.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ColorPalette.Light.primary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPalette.Light.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 로고
        Image(
            painter = painterResource(id = R.drawable.runnity_logo),
            contentDescription = "Runnity Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 48.dp),
            contentScale = ContentScale.Fit
        )

        // 환영 메시지
        Text(
            text = "러니티에 오신 것을 환영합니다",
            style = Typography.Title,
            color = ColorPalette.Light.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
        )

        Text(
            text = "소셜 로그인으로 간편하게 시작하세요",
            style = Typography.Body,
            color = ColorPalette.Light.component,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 48.dp)
        )

        // Google 로그인 버튼
        PrimaryButton(
            text = "Google로 시작하기",
            onClick = {
                // Google Sign-In 시작
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.3f)),
            modifier = Modifier.offset(y = 4.dp),
            enabled = uiState != LoginUiState.Loading,
            leadingIcon = painterResource(id = R.drawable.google)
        )

        // Kakao 로그인 버튼
        PrimaryButton(
            text = "Kakao로 시작하기",
            onClick = {
                // Kakao Login 시작 (OpenID Connect를 통해 ID Token 요청)
                // prompts에 "login"을 추가하여 재로그인 강제 (선택사항)
                val prompts = listOf("login")  // 또는 빈 리스트

                // KakaoTalk 앱이 설치되어 있으면 앱으로 로그인, 아니면 웹 로그인
                if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                    // KakaoTalk 앱으로 로그인 (ID Token 요청)
                    Timber.d("Kakao Login: Using KakaoTalk App with OpenID")
                    UserApiClient.instance.loginWithKakaoTalk(
                        context = context,
                        nonce = System.currentTimeMillis().toString(),  // nonce 추가 (보안)
                        callback = kakaoCallback
                    )
                } else {
                    // 웹 브라우저로 로그인 (ID Token 요청)
                    Timber.d("Kakao Login: Using Web Browser with OpenID")
                    UserApiClient.instance.loginWithKakaoAccount(
                        context = context,
                        nonce = System.currentTimeMillis().toString(),  // nonce 추가 (보안)
                        callback = kakaoCallback
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFEE500),  // Kakao yellow
                contentColor = Color.Black
            ),
            modifier = Modifier.offset(y = (-4).dp),
            enabled = uiState != LoginUiState.Loading,
            leadingIcon = painterResource(id = R.drawable.kakao)
        )
    }
}
