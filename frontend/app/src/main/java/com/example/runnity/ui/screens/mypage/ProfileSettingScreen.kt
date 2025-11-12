package com.example.runnity.ui.screens.mypage

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.runnity.R
import com.example.runnity.data.model.common.Gender
import com.example.runnity.data.util.UserProfileManager
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.ActionHeader
import com.example.runnity.ui.components.CustomTextField
import com.example.runnity.ui.components.PrimaryButton
import com.example.runnity.ui.components.SectionHeader
import com.example.runnity.ui.components.SmallPillButton

/**
 * 프로필 설정 화면
 * - 내 정보 조회 및 수정
 * - 닉네임, 키, 몸무게 수정 가능
 * - 로그아웃
 */
@Composable
fun ProfileSettingScreen(
    navController: NavController,
    parentNavController: NavController? = null,
    viewModel: ProfileSettingViewModel = viewModel()
) {
    val context = LocalContext.current
    val profile = UserProfileManager.getProfile()

    val nicknameCheckState by viewModel.nicknameCheckState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val logoutState by viewModel.logoutState.collectAsState()

    // 수정 모드 상태
    var isEditMode by remember { mutableStateOf(false) }

    // 수정 가능한 필드들
    var nickname by remember { mutableStateOf(profile?.nickname ?: "") }
    var nicknameErrorMessage by remember { mutableStateOf("") }

    var height by remember { mutableStateOf(profile?.height?.toString() ?: "") }
    var heightErrorMessage by remember { mutableStateOf("") }

    var weight by remember { mutableStateOf(profile?.weight?.toString() ?: "") }
    var weightErrorMessage by remember { mutableStateOf("") }

    // 프로필이 변경되면 초기값 다시 설정
    LaunchedEffect(profile) {
        nickname = profile?.nickname ?: ""
        height = profile?.height?.toString() ?: ""
        weight = profile?.weight?.toString() ?: ""
    }

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

    // 프로필 수정 상태 처리
    LaunchedEffect(updateState) {
        when (updateState) {
            UpdateProfileState.Success -> {
                Toast.makeText(context, "프로필이 수정되었습니다", Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateState()
                isEditMode = false  // 수정 모드 종료
            }
            is UpdateProfileState.Error -> {
                Toast.makeText(
                    context,
                    (updateState as UpdateProfileState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    // 로그아웃 상태 처리
    LaunchedEffect(logoutState) {
        when (logoutState) {
            LogoutState.Success -> {
                Toast.makeText(context, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
                parentNavController?.navigate("welcome") {
                    popUpTo(0) { inclusive = true }
                }
            }
            is LogoutState.Error -> {
                Toast.makeText(
                    context,
                    (logoutState as LogoutState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {}
        }
    }

    // 로딩 중일 때
    if (updateState == UpdateProfileState.Loading) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 헤더 (연필 아이콘 포함)
            ActionHeader(
                title = "내 정보",
                onBack = { navController.navigateUp() },
                height = 56.dp,
                rightAction = {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "수정",
                        tint = ColorPalette.Light.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                isEditMode = !isEditMode
                                if (!isEditMode) {
                                    // 수정 취소 시 원래 값으로 복원
                                    nickname = profile?.nickname ?: ""
                                    height = profile?.height?.toString() ?: ""
                                    weight = profile?.weight?.toString() ?: ""
                                    viewModel.resetNicknameCheck()
                                    nicknameErrorMessage = ""
                                    heightErrorMessage = ""
                                    weightErrorMessage = ""
                                }
                            }
                    )
                }
            )

            // 스크롤 가능한 컨텐츠
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 프로필 이미지
                ProfileImageSection(profileImageUrl = profile?.profileImageUrl)

                Spacer(modifier = Modifier.height(32.dp))

                // 기본 정보 섹션
                SectionHeader(subtitle = "기본 정보")

                Spacer(modifier = Modifier.height(8.dp))

                if (isEditMode) {
                    // 수정 모드: 닉네임 입력 필드
                    NicknameInputSection(
                        nickname = nickname,
                        onNicknameChange = { newValue ->
                            if (nicknameCheckState != NicknameCheckState.Loading) {
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
                        nicknameCheckState = nicknameCheckState,
                        nicknameErrorMessage = nicknameErrorMessage,
                        onNicknameErrorChange = { nicknameErrorMessage = it },
                        onCheckNickname = { viewModel.checkNickname(nickname) },
                        originalNickname = profile?.nickname ?: ""
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    // 읽기 모드: 닉네임 표시
                    InfoItem(
                        label = "닉네임",
                        value = profile?.nickname ?: "닉네임 없음"
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 이메일 (항상 읽기 전용)
                InfoItem(
                    label = "이메일",
                    value = profile?.email ?: "이메일 없음"
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 신체 정보 섹션
                SectionHeader(subtitle = "신체 정보")

                Spacer(modifier = Modifier.height(8.dp))

                if (isEditMode) {
                    // 수정 모드: 키 입력 필드
                    HeightInputSection(
                        height = height,
                        onHeightChange = { newValue ->
                            val filtered = newValue.filter { it.isDigit() || it == '.' }
                            if (filtered.count { it == '.' } <= 1) {
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
                        heightErrorMessage = heightErrorMessage
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 수정 모드: 몸무게 입력 필드
                    WeightInputSection(
                        weight = weight,
                        onWeightChange = { newValue ->
                            val filtered = newValue.filter { it.isDigit() || it == '.' }
                            if (filtered.count { it == '.' } <= 1) {
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
                        weightErrorMessage = weightErrorMessage
                    )
                } else {
                    // 읽기 모드: 키/몸무게 표시
                    InfoItem(
                        label = "키",
                        value = if (profile?.height != null) "${profile.height} cm" else "정보 없음"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    InfoItem(
                        label = "몸무게",
                        value = if (profile?.weight != null) "${profile.weight} kg" else "정보 없음"
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 추가 정보 섹션
                SectionHeader(subtitle = "추가 정보")

                Spacer(modifier = Modifier.height(8.dp))

                // 생년월일 (읽기 전용)
                InfoItem(
                    label = "생년월일",
                    value = profile?.birth ?: "정보 없음"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 성별 (읽기 전용)
                InfoItem(
                    label = "성별",
                    value = when (profile?.gender) {
                        Gender.MALE -> "남성"
                        Gender.FEMALE -> "여성"
                        null -> "정보 없음"
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 로그아웃 버튼
                PrimaryButton(
                    text = if (logoutState == LogoutState.Loading) "로그아웃 중..." else "로그아웃",
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorPalette.Common.stopAccent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(if (isEditMode) 100.dp else 16.dp))
            }
        }

        // 하단 고정 저장 버튼 (수정 모드일 때만 표시)
        if (isEditMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color.White)
            ) {
                PrimaryButton(
                    text = "저장",
                    onClick = {
                        // 유효성 검증
                        var hasError = false

                        // 닉네임 검증
                        if (nickname.length < 2) {
                            nicknameErrorMessage = "닉네임은 2글자 이상 입력해주세요."
                            hasError = true
                        } else if (nickname != profile?.nickname && nicknameCheckState != NicknameCheckState.Available) {
                            nicknameErrorMessage = "닉네임 중복 확인을 해주세요."
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

                        if (!hasError) {
                            viewModel.updateProfile(
                                nickname = nickname,
                                height = heightValue!!,
                                weight = weightValue!!,
                                profileImageUri = null
                            )
                        }
                    }
                )
            }
        }
    }
}

/**
 * 프로필 이미지 섹션
 */
@Composable
private fun ProfileImageSection(profileImageUrl: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = if (profileImageUrl != null && profileImageUrl.isNotBlank()) {
                    rememberAsyncImagePainter(profileImageUrl)
                } else {
                    painterResource(id = R.drawable.profile)
                },
                contentDescription = "프로필",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/**
 * 닉네임 입력 섹션 (수정 모드)
 */
@Composable
private fun NicknameInputSection(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    nicknameCheckState: NicknameCheckState,
    nicknameErrorMessage: String,
    onNicknameErrorChange: (String) -> Unit,
    onCheckNickname: () -> Unit,
    originalNickname: String
) {
    Column {
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            CustomTextField(
                value = nickname,
                onValueChange = onNicknameChange,
                placeholder = "2-10자, 한글/영문/숫자만 가능",
                trailingContent = {
                    SmallPillButton(
                        text = if (nicknameCheckState == NicknameCheckState.Loading) "확인 중..." else "중복 확인",
                        selected = nicknameCheckState == NicknameCheckState.Available,
                        onClick = {
                            if (nicknameCheckState != NicknameCheckState.Loading) {
                                if (nickname.isBlank()) {
                                    onNicknameErrorChange("닉네임을 입력해주세요.")
                                } else if (nickname.length < 2) {
                                    onNicknameErrorChange("닉네임은 2글자 이상 입력해주세요.")
                                } else if (nickname.length > 10) {
                                    onNicknameErrorChange("10자 이내로 입력해주세요.")
                                } else if (nickname != originalNickname) {
                                    onNicknameErrorChange("")
                                    onCheckNickname()
                                }
                            }
                        },
                        modifier = Modifier.width(80.dp)
                    )
                }
            )
        }

        // 메시지 영역 (고정 높이)
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
    }
}

/**
 * 키 입력 섹션 (수정 모드)
 */
@Composable
private fun HeightInputSection(
    height: String,
    onHeightChange: (String) -> Unit,
    heightErrorMessage: String
) {
    Column {
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            CustomTextField(
                value = height,
                onValueChange = onHeightChange,
                placeholder = "100~250cm (예: 170.5)",
                keyboardType = KeyboardType.Decimal
            )
        }

        // 메시지 영역 (고정 높이)
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
    }
}

/**
 * 몸무게 입력 섹션 (수정 모드)
 */
@Composable
private fun WeightInputSection(
    weight: String,
    onWeightChange: (String) -> Unit,
    weightErrorMessage: String
) {
    Column {
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            CustomTextField(
                value = weight,
                onValueChange = onWeightChange,
                placeholder = "30~200kg (예: 65.5)",
                keyboardType = KeyboardType.Decimal
            )
        }

        // 메시지 영역 (고정 높이)
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
    }
}

/**
 * 정보 아이템 (읽기 전용)
 */
@Composable
private fun InfoItem(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = label,
            style = Typography.Caption,
            color = ColorPalette.Light.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = Typography.Body,
            color = ColorPalette.Light.primary
        )
    }
}
