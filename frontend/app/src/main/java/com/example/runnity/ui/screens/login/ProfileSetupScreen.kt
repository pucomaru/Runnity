package com.example.runnity.ui.screens.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.data.model.common.Gender
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.CustomTextField
import com.example.runnity.ui.components.PageHeader
import com.example.runnity.ui.components.PrimaryButton
import com.example.runnity.ui.components.SectionHeader
import com.example.runnity.ui.components.SmallPillButton

/**
 * 프로필 추가 정보 입력 화면
 * - 닉네임 (중복 확인)
 * - 키 (cm)
 * - 몸무게 (kg)
 * - 생년월일
 * - 성별
 * - ProfileSetupViewModel과 연동되어 실제 API 호출
 */
@Composable
fun ProfileSetupScreen(
    navController: NavController,
    viewModel: ProfileSetupViewModel = viewModel()
) {
    val context = LocalContext.current
    val nicknameCheckState by viewModel.nicknameCheckState.collectAsState()
    val submitState by viewModel.submitState.collectAsState()

    var nickname by remember { mutableStateOf("") }
    var nicknameErrorMessage by remember { mutableStateOf("") }

    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    var birthYear by remember { mutableStateOf("") }
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var birthErrorMessage by remember { mutableStateOf("") }

    var selectedGender by remember { mutableStateOf<Gender?>(null) }
    var genderErrorMessage by remember { mutableStateOf("") }

    // 닉네임 체크 상태 처리
    LaunchedEffect(nicknameCheckState) {
        when (nicknameCheckState) {
            is NicknameCheckState.Error -> {
                nicknameErrorMessage = (nicknameCheckState as NicknameCheckState.Error).message
            }
            NicknameCheckState.Unavailable -> {
                nicknameErrorMessage = "이미 사용 중인 닉네임입니다."
            }
            else -> {}
        }
    }

    // 제출 상태 처리
    LaunchedEffect(submitState) {
        when (submitState) {
            SubmitState.Success -> {
                // 성공 시 메인 화면으로 이동
                navController.navigate("main") {
                    popUpTo(0) { inclusive = true }
                }
            }
            is SubmitState.Error -> {
                Toast.makeText(
                    context,
                    (submitState as SubmitState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
                // 에러 상태 초기화
                viewModel.resetSubmitError()
            }
            else -> {}
        }
    }

    // 로딩 중일 때
    if (submitState == SubmitState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ColorPalette.Light.primary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 상단 헤더
        PageHeader(
            title = "추가 정보 입력"
        )

        // 스크롤 가능한 컨텐츠
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 닉네임 섹션
            SectionHeader(subtitle = "닉네임")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "10자 이내로 한글 영문 숫자만 입력해주세요",
                style = Typography.Body,
                color = ColorPalette.Light.component,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                CustomTextField(
                    value = nickname,
                    onValueChange = {
                        if (nicknameCheckState != NicknameCheckState.Loading) {
                            nickname = it
                            viewModel.resetNicknameCheck()
                            nicknameErrorMessage = ""
                        }
                    },
                    placeholder = "닉네임을 입력해주세요",
                    trailingContent = {
                        if (nicknameCheckState != NicknameCheckState.Loading) {
                            SmallPillButton(
                                text = "중복 확인",
                                selected = nicknameCheckState == NicknameCheckState.Available,
                                onClick = {
                                    if (nickname.isBlank()) {
                                        nicknameErrorMessage = "닉네임을 입력해주세요."
                                    } else if (nickname.length > 10) {
                                        nicknameErrorMessage = "10자 이내로 입력해주세요."
                                    } else {
                                        viewModel.checkNickname(nickname)
                                    }
                                },
                                modifier = Modifier.width(80.dp)
                            )
                        } else {
                            Text(
                                text = "확인 중...",
                                style = Typography.Caption,
                                color = ColorPalette.Light.component,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                )
            }

            // 메시지 영역 (고정 높이로 레이아웃 안정화)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(horizontal = 16.dp)
            ) {
                when {
                    nicknameErrorMessage.isNotEmpty() -> {
                        Text(
                            text = nicknameErrorMessage,
                            style = Typography.Caption,
                            color = ColorPalette.Common.stopAccent
                        )
                    }
                    nicknameCheckState == NicknameCheckState.Available -> {
                        Text(
                            text = "사용 가능한 닉네임입니다.",
                            style = Typography.Caption,
                            color = ColorPalette.Common.accent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 키 입력 섹션
            SectionHeader(subtitle = "키 (cm)")

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                CustomTextField(
                    value = height,
                    onValueChange = { height = it },
                    placeholder = "키를 입력해주세요",
                    keyboardType = KeyboardType.Number
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(32.dp))

            // 몸무게 입력 섹션
            SectionHeader(subtitle = "몸무게 (kg)")

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                CustomTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    placeholder = "몸무게를 입력해주세요",
                    keyboardType = KeyboardType.Number
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(32.dp))

            // 생년월일 입력 섹션
            SectionHeader(subtitle = "생년월일")

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 년
                CustomTextField(
                    value = birthYear,
                    onValueChange = {
                        birthYear = it
                        birthErrorMessage = ""
                    },
                    placeholder = "YYYY",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(2f)
                )

                // 월
                CustomTextField(
                    value = birthMonth,
                    onValueChange = {
                        birthMonth = it
                        birthErrorMessage = ""
                    },
                    placeholder = "MM",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )

                // 일
                CustomTextField(
                    value = birthDay,
                    onValueChange = {
                        birthDay = it
                        birthErrorMessage = ""
                    },
                    placeholder = "DD",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            // 메시지 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(horizontal = 16.dp)
            ) {
                if (birthErrorMessage.isNotEmpty()) {
                    Text(
                        text = birthErrorMessage,
                        style = Typography.Caption,
                        color = ColorPalette.Common.stopAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 성별 선택 섹션
            SectionHeader(subtitle = "성별")

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallPillButton(
                    text = "남성",
                    selected = selectedGender == Gender.MALE,
                    onClick = {
                        selectedGender = Gender.MALE
                        genderErrorMessage = ""
                    },
                    modifier = Modifier.weight(1f),
                    heightDp = 48
                )

                SmallPillButton(
                    text = "여성",
                    selected = selectedGender == Gender.FEMALE,
                    onClick = {
                        selectedGender = Gender.FEMALE
                        genderErrorMessage = ""
                    },
                    modifier = Modifier.weight(1f),
                    heightDp = 48
                )
            }

            // 메시지 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(horizontal = 16.dp)
            ) {
                if (genderErrorMessage.isNotEmpty()) {
                    Text(
                        text = genderErrorMessage,
                        style = Typography.Caption,
                        color = ColorPalette.Common.stopAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // 완료 버튼 (하단 고정)
        PrimaryButton(
            text = "완료",
            onClick = {
                var hasError = false

                // 닉네임 검증
                if (nicknameCheckState != NicknameCheckState.Available) {
                    if (nickname.isBlank()) {
                        nicknameErrorMessage = "닉네임을 입력해주세요."
                    } else {
                        nicknameErrorMessage = "닉네임 중복 확인을 해주세요."
                    }
                    hasError = true
                }

                // 생년월일 검증
                if (birthYear.isBlank() || birthMonth.isBlank() || birthDay.isBlank()) {
                    birthErrorMessage = "생년월일을 입력해주세요."
                    hasError = true
                }

                // 성별 검증
                if (selectedGender == null) {
                    genderErrorMessage = "성별을 선택해주세요."
                    hasError = true
                }

                if (!hasError) {
                    // 생년월일 포맷 (YYYY-MM-DD)
                    val birth = "$birthYear-${birthMonth.padStart(2, '0')}-${birthDay.padStart(2, '0')}"

                    // API 호출
                    viewModel.submitAdditionalInfo(
                        nickname = nickname,
                        gender = selectedGender!!,
                        height = height.toDoubleOrNull() ?: 0.0,
                        weight = weight.toDoubleOrNull() ?: 0.0,
                        birth = birth
                    )
                }
            },
            enabled = nicknameCheckState == NicknameCheckState.Available &&
                    height.isNotBlank() &&
                    weight.isNotBlank() &&
                    birthYear.isNotBlank() &&
                    birthMonth.isNotBlank() &&
                    birthDay.isNotBlank() &&
                    selectedGender != null &&
                    submitState != SubmitState.Loading
        )
    }
}
