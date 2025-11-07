package com.example.runnity.ui.screens.challenge

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
import com.example.runnity.ui.components.*
import java.time.LocalDate

/**
 * 챌린지 생성 화면
 * - 챌린지 정보 입력 및 생성
 * - 제목, 정원, 공개 여부, 거리, 날짜/시간, 중계방 사용 여부 설정
 *
 * @param navController 네비게이션 컨트롤러
 */
@Composable
fun ChallengeCreateScreen(
    navController: NavController? = null
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
                // TODO: 챌린지 생성 로직
                // - title (최대 20자), description, maxParticipants (최대 100명),
                //   selectedVisibility, password (비공개일 경우 4자리),
                //   selectedDistance, selectedDate, selectedHour, selectedMinute, selectedAmPm, broadcastEnabled
                // - 입력값 검증 후 API 호출
                navController?.navigateUp()
            }
        )
    }
}
