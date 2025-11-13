package com.example.runnity.ui.screens.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.*
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 챌린지 생성 화면
 * - 챌린지 정보 입력 및 생성
 * - 제목, 정원, 공개 여부, 거리, 날짜/시간, 중계방 사용 여부 설정
 *
 * @param navController 네비게이션 컨트롤러
 */
@Composable
fun ChallengeCreateScreen(
    navController: NavController? = null,
    viewModel: ChallengeViewModel = viewModel()
) {
    // 제목 입력 상태 (최대 20자)
    var title by remember { mutableStateOf("") }

    // 챌린지 설명 입력 상태
    var description by remember { mutableStateOf("") }

    // 정원 입력 상태 (최대 100명)
    var maxParticipants by remember { mutableStateOf("") }

    // 공개 여부 선택 상태
    var selectedVisibility by remember { mutableStateOf("공개") }

    // 비공개 비밀번호 (4자리 숫자)
    var password by remember { mutableStateOf("") }

    // 거리 선택 상태 (단일 선택)
    var selectedDistance by remember { mutableStateOf<String?>(null) }

    // 날짜 선택 상태 (단일 날짜)
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    // 시간 선택 상태
    var selectedHour by remember { mutableStateOf(6) }      // 1~12
    var selectedMinute by remember { mutableStateOf(0) }    // 0~59
    var selectedAmPm by remember { mutableStateOf("pm") }   // am, pm

    // 중계방 사용 여부
    var broadcastEnabled by remember { mutableStateOf("미사용") }

    // 에러 메시지 다이얼로그
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. 상단 헤더 (뒤로가기 + 제목)
        ActionHeader(
            title = "챌린지 생성",
            onBack = { navController?.navigateUp() },
            height = 56.dp
        )

        // 2. 스크롤 가능한 컨텐츠
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ===== 챌린지 이름 섹션 =====
            SectionHeader(subtitle = "챌린지 이름")

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                CustomTextField(
                    value = title,
                    onValueChange = {
                        // 최대 20자까지만 입력 가능 (공백 포함)
                        if (it.length <= 20) {
                            title = it
                        }
                    },
                    placeholder = "챌린지 이름을 입력해 주세요 (최대 20자)"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== 챌린지 설명 섹션 =====
            SectionHeader(subtitle = "챌린지 설명")

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                CustomTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "챌린지에 대한 설명을 입력해 주세요",
                    singleLine = false
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== 정원 섹션 =====
            SectionHeader(subtitle = "정원")

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                CustomTextField(
                    value = maxParticipants,
                    onValueChange = {
                        // 숫자만 입력 가능, 최대 100명
                        if (it.all { char -> char.isDigit() }) {
                            val num = it.toIntOrNull()
                            if (num == null || num <= 100) {
                                maxParticipants = it
                            }
                        }
                    },
                    placeholder = "정원을 입력해 주세요 (최대 100명)",
                    keyboardType = KeyboardType.Number
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== 공개 여부 섹션 =====
            SectionHeader(subtitle = "공개 여부")

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallPillButton(
                    text = "공개",
                    selected = selectedVisibility == "공개",
                    onClick = {
                        selectedVisibility = "공개"
                        password = ""  // 공개로 변경 시 비밀번호 초기화
                    },
                    modifier = Modifier.weight(1f)
                )
                SmallPillButton(
                    text = "비공개",
                    selected = selectedVisibility == "비공개",
                    onClick = { selectedVisibility = "비공개" },
                    modifier = Modifier.weight(1f)
                )
            }

            // 비공개 선택 시 비밀번호 입력
            if (selectedVisibility == "비공개") {
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "비밀번호 (4자리)",
                        style = Typography.Caption,
                        color = ColorPalette.Light.secondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    CustomTextField(
                        value = password,
                        onValueChange = {
                            // 4자리 숫자만 입력 가능
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                password = it
                            }
                        },
                        placeholder = "4자리 숫자를 입력해 주세요",
                        keyboardType = KeyboardType.NumberPassword,
                        isPassword = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== 거리(km) 섹션 =====
            SectionHeader(subtitle = "거리(km)")

            Spacer(modifier = Modifier.height(12.dp))

            // 첫 번째 줄: 1km ~ 5km
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("1km", "2km", "3km", "4km", "5km").forEach { distance ->
                    SmallPillButton(
                        text = distance,
                        selected = selectedDistance == distance,
                        onClick = {
                            selectedDistance = if (selectedDistance == distance) null else distance
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 두 번째 줄: 6km ~ 10km
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("6km", "7km", "8km", "9km", "10km").forEach { distance ->
                    SmallPillButton(
                        text = distance,
                        selected = selectedDistance == distance,
                        onClick = {
                            selectedDistance = if (selectedDistance == distance) null else distance
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 세 번째 줄: 15km, 하프 (가운데 정렬)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                SmallPillButton(
                    text = "15km",
                    selected = selectedDistance == "15km",
                    onClick = {
                        selectedDistance = if (selectedDistance == "15km") null else "15km"
                    },
                    modifier = Modifier.width(80.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                SmallPillButton(
                    text = "하프",
                    selected = selectedDistance == "하프",
                    onClick = {
                        selectedDistance = if (selectedDistance == "하프") null else "하프"
                    },
                    modifier = Modifier.width(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== 시작 날짜 · 시간 섹션 =====
            SectionHeader(subtitle = "시작 날짜 · 시간")

            Spacer(modifier = Modifier.height(12.dp))

            // 날짜·시간 선택 컴포넌트
            SingleDateTimeSelector(
                modifier = Modifier.padding(horizontal = 16.dp),
                selectedDate = selectedDate,
                selectedHour = selectedHour,
                selectedMinute = selectedMinute,
                selectedAmPm = selectedAmPm,
                onDateSelected = { selectedDate = it },
                onTimeSelected = { hour, minute, amPm ->
                    selectedHour = hour
                    selectedMinute = minute
                    selectedAmPm = amPm
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ===== 중계방 사용 여부 섹션 =====
            SectionHeader(subtitle = "중계방 사용 여부")

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallPillButton(
                    text = "미사용",
                    selected = broadcastEnabled == "미사용",
                    onClick = { broadcastEnabled = "미사용" },
                    modifier = Modifier.weight(1f)
                )
                SmallPillButton(
                    text = "사용",
                    selected = broadcastEnabled == "사용",
                    onClick = { broadcastEnabled = "사용" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. 하단 생성하기 버튼
        PrimaryButton(
            text = "생성하기",
            onClick = {
                // 입력값 검증
                val validationError = validateInputs(
                    title = title,
                    description = description,
                    maxParticipants = maxParticipants,
                    selectedDistance = selectedDistance,
                    selectedDate = selectedDate,
                    selectedVisibility = selectedVisibility,
                    password = password
                )

                if (validationError != null) {
                    errorMessage = validationError
                    showErrorDialog = true
                    return@PrimaryButton
                }

                // ISO 8601 형식으로 날짜/시간 변환
                Timber.d("챌린지 생성: selectedHour=$selectedHour, selectedMinute=$selectedMinute, selectedAmPm=$selectedAmPm")
                val startAt = convertToIso8601(
                    date = selectedDate!!,
                    hour = selectedHour,
                    minute = selectedMinute,
                    amPm = selectedAmPm
                )
                Timber.d("챌린지 생성 시간: $startAt")

                // 거리 변환 (UI -> API)
                val distanceCode = convertDistanceToCode(selectedDistance!!)

                // 챌린지 생성 API 호출
                viewModel.createChallenge(
                    title = title,
                    description = description,
                    maxParticipants = maxParticipants.toInt(),
                    startAt = startAt,
                    distance = distanceCode,
                    isPrivate = selectedVisibility == "비공개",
                    password = if (selectedVisibility == "비공개") password else null,
                    isBroadcast = broadcastEnabled == "사용",
                    onSuccess = { challengeId ->
                        // 생성 성공 시 상세 화면으로 이동
                        navController?.navigate("challenge_detail/$challengeId") {
                            popUpTo("challenge") { inclusive = false }
                        }
                    },
                    onError = { error ->
                        errorMessage = error
                        showErrorDialog = true
                    }
                )
            }
        )
    }

    // 에러 다이얼로그
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("오류") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("확인")
                }
            }
        )
    }
}

/**
 * 입력값 검증
 */
private fun validateInputs(
    title: String,
    description: String,
    maxParticipants: String,
    selectedDistance: String?,
    selectedDate: LocalDate?,
    selectedVisibility: String,
    password: String
): String? {
    if (title.isBlank()) {
        return "챌린지 이름을 입력해주세요"
    }
    if (description.isBlank()) {
        return "챌린지 설명을 입력해주세요"
    }
    if (maxParticipants.isBlank()) {
        return "정원을 입력해주세요"
    }
    val participants = maxParticipants.toIntOrNull()
    if (participants == null || participants < 2) {
        return "정원은 최소 2명 이상이어야 합니다"
    }
    if (participants > 100) {
        return "정원은 최대 100명까지 가능합니다"
    }
    if (selectedDistance == null) {
        return "거리를 선택해주세요"
    }
    if (selectedDate == null) {
        return "날짜를 선택해주세요"
    }
    if (selectedVisibility == "비공개" && password.length != 4) {
        return "비밀번호는 4자리 숫자여야 합니다"
    }
    return null
}

/**
 * 날짜/시간을 ISO 8601 형식으로 변환
 * 예: 2025-11-12T21:00:00
 */
private fun convertToIso8601(
    date: LocalDate,
    hour: Int,
    minute: Int,
    amPm: String
): String {
    // 12시간 형식을 24시간 형식으로 변환
    val hour24 = when {
        amPm == "am" && hour == 12 -> 0  // 12 am = 0시
        amPm == "pm" && hour != 12 -> hour + 12  // 1 pm = 13시
        else -> hour
    }

    val dateTime = LocalDateTime.of(date.year, date.month, date.dayOfMonth, hour24, minute)
    return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}

/**
 * UI 거리 값을 API 거리 코드로 변환
 * 1km -> ONE, 5km -> FIVE, 하프 -> HALF 등
 */
private fun convertDistanceToCode(distance: String): String {
    return when (distance) {
        "1km" -> "ONE"
        "2km" -> "TWO"
        "3km" -> "THREE"
        "4km" -> "FOUR"
        "5km" -> "FIVE"
        "6km" -> "SIX"
        "7km" -> "SEVEN"
        "8km" -> "EIGHT"
        "9km" -> "NINE"
        "10km" -> "TEN"
        "15km" -> "FIFTEEN"
        "하프" -> "HALF"
        else -> "FIVE"
    }
}
