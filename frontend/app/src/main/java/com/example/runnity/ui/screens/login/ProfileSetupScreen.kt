package com.example.runnity.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
 *
 * @param navController 네비게이션 컨트롤러
 */
@Composable
fun ProfileSetupScreen(
    navController: NavController
) {
    var nickname by remember { mutableStateOf("") }
    var isNicknameChecked by remember { mutableStateOf(false) }
    var nicknameErrorMessage by remember { mutableStateOf("") }

    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    var birthYear by remember { mutableStateOf("") }
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var birthErrorMessage by remember { mutableStateOf("") }

    var selectedGender by remember { mutableStateOf<String?>(null) }
    var genderErrorMessage by remember { mutableStateOf("") }

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
                        nickname = it
                        isNicknameChecked = false
                        nicknameErrorMessage = ""
                    },
                    placeholder = "닉네임을 입력해주세요",
                    trailingContent = {
                        SmallPillButton(
                            text = "중복 확인",
                            selected = isNicknameChecked,
                            onClick = {
                                // TODO: 닉네임 중복 확인 API 호출
                                if (nickname.isBlank()) {
                                    nicknameErrorMessage = "닉네임을 입력해주세요."
                                    isNicknameChecked = false
                                } else if (nickname.length > 10) {
                                    nicknameErrorMessage = "10자 이내로 입력해주세요."
                                    isNicknameChecked = false
                                } else {
                                    // 임시: 중복 확인 성공
                                    isNicknameChecked = true
                                    nicknameErrorMessage = ""
                                }
                            },
                            modifier = Modifier.width(80.dp)
                        )
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
                    isNicknameChecked -> {
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

            // 여백 통일을 위한 빈 공간
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

            // 여백 통일을 위한 빈 공간
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

            // 메시지 영역 (고정 높이로 레이아웃 안정화)
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
                    selected = selectedGender == "MALE",
                    onClick = {
                        selectedGender = "MALE"
                        genderErrorMessage = ""
                    },
                    modifier = Modifier.weight(1f),
                    heightDp = 48
                )

                SmallPillButton(
                    text = "여성",
                    selected = selectedGender == "FEMALE",
                    onClick = {
                        selectedGender = "FEMALE"
                        genderErrorMessage = ""
                    },
                    modifier = Modifier.weight(1f),
                    heightDp = 48
                )
            }

            // 메시지 영역 (고정 높이로 레이아웃 안정화)
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
                if (!isNicknameChecked) {
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
                    // TODO: 프로필 정보 저장 API 호출
                    // TODO: 메인 화면으로 이동
                    navController.navigate("main") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            enabled = isNicknameChecked &&
                    height.isNotBlank() &&
                    weight.isNotBlank() &&
                    birthYear.isNotBlank() &&
                    birthMonth.isNotBlank() &&
                    birthDay.isNotBlank() &&
                    selectedGender != null
        )
    }
}
