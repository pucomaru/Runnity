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
    var heightErrorMessage by remember { mutableStateOf("") }

    var weight by remember { mutableStateOf("") }
    var weightErrorMessage by remember { mutableStateOf("") }

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
            NicknameCheckState.Loading -> {
                nicknameErrorMessage = ""
            }
            NicknameCheckState.Available -> {
                nicknameErrorMessage = ""
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

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                CustomTextField(
                    value = nickname,
                    onValueChange = { newValue ->
                        if (nicknameCheckState != NicknameCheckState.Loading) {
                            // 한글, 영문, 숫자만 허용 (공백, 특수문자 차단)
                            val filtered = newValue.filter { char ->
                                char.isLetterOrDigit() && char != ' '
                            }
                            if (filtered.length <= 10) {
                                nickname = filtered
                                viewModel.resetNicknameCheck()
                                nicknameErrorMessage = ""
                            }
                        }
                    },
                    placeholder = "2-10자, 한글/영문/숫자만 가능",
                    trailingContent = {
                        SmallPillButton(
                            text = if (nicknameCheckState == NicknameCheckState.Loading) "확인 중..." else "중복 확인",
                            selected = nicknameCheckState == NicknameCheckState.Available,
                            onClick = {
                                if (nicknameCheckState != NicknameCheckState.Loading) {
                                    if (nickname.isBlank()) {
                                        nicknameErrorMessage = "닉네임을 입력해주세요."
                                    } else if (nickname.length < 2) {
                                        nicknameErrorMessage = "닉네임은 2글자 이상 입력해주세요."
                                    } else if (nickname.length > 10) {
                                        nicknameErrorMessage = "10자 이내로 입력해주세요."
                                    } else {
                                        // 에러 메시지 초기화 후 API 호출
                                        nicknameErrorMessage = ""
                                        viewModel.checkNickname(nickname)
                                    }
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
                    nicknameCheckState == NicknameCheckState.Loading -> {
                        Text(
                            text = "중복 확인 중...",
                            style = Typography.Caption,
                            color = ColorPalette.Light.component
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
                    onValueChange = { newValue ->
                        // 숫자와 소수점만 허용
                        val filtered = newValue.filter { it.isDigit() || it == '.' }

                        // 소수점이 2개 이상이면 무시
                        if (filtered.count { it == '.' } <= 1) {
                            // 소수점 1자리까지만 허용
                            val parts = filtered.split(".")
                            val formatted = if (parts.size == 2 && parts[1].length > 1) {
                                "${parts[0]}.${parts[1].take(1)}"
                            } else {
                                filtered
                            }

                            height = formatted
                            heightErrorMessage = ""
                        }
                    },
                    placeholder = "100~250cm (예: 170.5)",
                    keyboardType = KeyboardType.Decimal
                )
            }

            // 메시지 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(horizontal = 16.dp)
            ) {
                if (heightErrorMessage.isNotEmpty()) {
                    Text(
                        text = heightErrorMessage,
                        style = Typography.Caption,
                        color = ColorPalette.Common.stopAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 몸무게 입력 섹션
            SectionHeader(subtitle = "몸무게 (kg)")

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                CustomTextField(
                    value = weight,
                    onValueChange = { newValue ->
                        // 숫자와 소수점만 허용
                        val filtered = newValue.filter { it.isDigit() || it == '.' }

                        // 소수점이 2개 이상이면 무시
                        if (filtered.count { it == '.' } <= 1) {
                            // 소수점 1자리까지만 허용
                            val parts = filtered.split(".")
                            val formatted = if (parts.size == 2 && parts[1].length > 1) {
                                "${parts[0]}.${parts[1].take(1)}"
                            } else {
                                filtered
                            }

                            weight = formatted
                            weightErrorMessage = ""
                        }
                    },
                    placeholder = "30~200kg (예: 65.5)",
                    keyboardType = KeyboardType.Decimal
                )
            }

            // 메시지 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(horizontal = 16.dp)
            ) {
                if (weightErrorMessage.isNotEmpty()) {
                    Text(
                        text = weightErrorMessage,
                        style = Typography.Caption,
                        color = ColorPalette.Common.stopAccent
                    )
                }
            }

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
                // 년 (4자리)
                CustomTextField(
                    value = birthYear,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }
                        if (filtered.length <= 4) {
                            birthYear = filtered
                            birthErrorMessage = ""
                        }
                    },
                    placeholder = "년 (예: 1990)",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(2f)
                )

                // 월 (2자리, 01-12)
                CustomTextField(
                    value = birthMonth,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }
                        if (filtered.length <= 2) {
                            val month = filtered.toIntOrNull() ?: 0
                            if (filtered.isEmpty() || month in 1..12) {
                                birthMonth = filtered
                                birthErrorMessage = ""
                            }
                        }
                    },
                    placeholder = "월 (01-12)",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )

                // 일 (2자리, 01-31)
                CustomTextField(
                    value = birthDay,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }
                        if (filtered.length <= 2) {
                            val day = filtered.toIntOrNull() ?: 0
                            if (filtered.isEmpty() || day in 1..31) {
                                birthDay = filtered
                                birthErrorMessage = ""
                            }
                        }
                    },
                    placeholder = "일 (01-31)",
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
                if (nickname.length < 2) {
                    nicknameErrorMessage = "닉네임은 2글자 이상 입력해주세요."
                    hasError = true
                } else if (nicknameCheckState != NicknameCheckState.Available) {
                    if (nickname.isBlank()) {
                        nicknameErrorMessage = "닉네임을 입력해주세요."
                    } else {
                        nicknameErrorMessage = "닉네임 중복 확인을 해주세요."
                    }
                    hasError = true
                }

                // 키 검증
                val heightValue = height.toDoubleOrNull()
                if (heightValue == null) {
                    heightErrorMessage = "키를 입력해주세요."
                    hasError = true
                } else if (heightValue < 100.0 || heightValue > 250.0) {
                    heightErrorMessage = "키는 100~250cm 사이로 입력해주세요."
                    hasError = true
                }

                // 몸무게 검증
                val weightValue = weight.toDoubleOrNull()
                if (weightValue == null) {
                    weightErrorMessage = "몸무게를 입력해주세요."
                    hasError = true
                } else if (weightValue < 30.0 || weightValue > 200.0) {
                    weightErrorMessage = "몸무게는 30~200kg 사이로 입력해주세요."
                    hasError = true
                }

                // 생년월일 검증
                if (birthYear.isBlank() || birthMonth.isBlank() || birthDay.isBlank()) {
                    birthErrorMessage = "생년월일을 입력해주세요."
                    hasError = true
                } else if (birthYear.length != 4) {
                    birthErrorMessage = "년도는 4자리로 입력해주세요."
                    hasError = true
                } else if (birthMonth.length < 1 || birthMonth.length > 2) {
                    birthErrorMessage = "월을 올바르게 입력해주세요."
                    hasError = true
                } else if (birthDay.length < 1 || birthDay.length > 2) {
                    birthErrorMessage = "일을 올바르게 입력해주세요."
                    hasError = true
                } else {
                    val year = birthYear.toIntOrNull() ?: 0
                    val month = birthMonth.toIntOrNull() ?: 0
                    val day = birthDay.toIntOrNull() ?: 0

                    // 년도 범위 체크
                    if (year < 1900) {
                        birthErrorMessage = "올바른 년도를 입력해주세요."
                        hasError = true
                    } else {
                        // 윤년 체크 함수
                        fun isLeapYear(y: Int): Boolean {
                            return (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)
                        }

                        // 월별 최대 일수
                        val maxDayInMonth = when (month) {
                            1, 3, 5, 7, 8, 10, 12 -> 31
                            4, 6, 9, 11 -> 30
                            2 -> if (isLeapYear(year)) 29 else 28
                            else -> 0
                        }

                        // 일수 체크
                        if (day < 1 || day > maxDayInMonth) {
                            birthErrorMessage = when (month) {
                                2 -> if (isLeapYear(year)) {
                                    "${year}년 2월은 29일까지입니다. (윤년)"
                                } else {
                                    "${year}년 2월은 28일까지입니다."
                                }
                                4, 6, 9, 11 -> "${month}월은 30일까지입니다."
                                else -> "올바른 일자를 입력해주세요."
                            }
                            hasError = true
                        } else {
                            // 현재 날짜 이후인지 체크
                            val calendar = java.util.Calendar.getInstance()
                            val currentYear = calendar.get(java.util.Calendar.YEAR)
                            val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1  // 0-based
                            val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)

                            if (year > currentYear ||
                                (year == currentYear && month > currentMonth) ||
                                (year == currentYear && month == currentMonth && day > currentDay)) {
                                birthErrorMessage = "미래 날짜는 입력할 수 없습니다."
                                hasError = true
                            }
                        }
                    }
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
                        context = context,
                        nickname = nickname,
                        gender = selectedGender!!,
                        height = heightValue!!,
                        weight = weightValue!!,
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
