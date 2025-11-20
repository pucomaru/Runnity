package com.example.runnity.ui.screens.mypage

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
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

    // 선택된 프로필 이미지 URI
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // 이미지 선택 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

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
                                    selectedImageUri = null
                                    viewModel.resetNicknameCheck()
                                    nicknameErrorMessage = ""
                                    heightErrorMessage = ""
                                    weightErrorMessage = ""
                                }
                            }
                    )
                }
            )

            // 컨텐츠
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp)
            ) {
                // 프로필 이미지
                ProfileImageSection(
                    profileImageUrl = profile?.profileImageUrl,
                    selectedImageUri = selectedImageUri,
                    isEditMode = isEditMode,
                    onImageClick = {
                        imagePickerLauncher.launch("image/*")
                    }
                )

                Spacer(
                    modifier = Modifier
                        .weight(0.3f)
                        .heightIn(min = 16.dp)
                )

                // 닉네임
                if (isEditMode) {
                    // 수정 모드: 헤더와 입력창이 겹치는 레이아웃
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SectionHeader(subtitle = "닉네임")
                            Spacer(modifier = Modifier.height(8.dp))

                            // 중복확인 버튼 없는 입력 필드
                            NicknameInputFieldWithoutButton(
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
                                nicknameErrorMessage = nicknameErrorMessage
                            )
                        }

                        // 중복확인 버튼을 오른쪽 상단에 절대 위치로 배치
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 12.dp, end = 16.dp)
                        ) {
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
                                        } else if (nickname != profile?.nickname) {
                                            nicknameErrorMessage = ""
                                            viewModel.checkNickname(nickname)
                                        }
                                    }
                                },
                                modifier = Modifier.width(80.dp)
                            )
                        }
                    }
                } else {
                    // 읽기 모드: 일반 헤더와 필드
                    SectionHeader(subtitle = "닉네임")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReadOnlyField(value = profile?.nickname ?: "닉네임 없음")
                }

                Spacer(
                    modifier = Modifier
                        .weight(0.2f)
                        .heightIn(min = 12.dp)
                )

                // 키
                SectionHeader(subtitle = "키")
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
                } else {
                    // 읽기 모드: 키 표시
                    ReadOnlyField(
                        value = if (profile?.height != null) "${profile.height} cm" else "정보 없음"
                    )
                }

                Spacer(
                    modifier = Modifier
                        .weight(0.2f)
                        .heightIn(min = 12.dp)
                )

                // 몸무게
                SectionHeader(subtitle = "몸무게")
                Spacer(modifier = Modifier.height(8.dp))

                if (isEditMode) {
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
                    // 읽기 모드: 몸무게 표시
                    ReadOnlyField(
                        value = if (profile?.weight != null) "${profile.weight} kg" else "정보 없음"
                    )
                }

                Spacer(
                    modifier = Modifier
                        .weight(0.2f)
                        .heightIn(min = 12.dp)
                )

                // 생년월일
                SectionHeader(subtitle = "생년월일")
                Spacer(modifier = Modifier.height(8.dp))
                ReadOnlyField(value = profile?.birth ?: "정보 없음")

                Spacer(
                    modifier = Modifier
                        .weight(0.2f)
                        .heightIn(min = 12.dp)
                )

                // 성별
                SectionHeader(subtitle = "성별")
                Spacer(modifier = Modifier.height(8.dp))
                ReadOnlyField(
                    value = when (profile?.gender) {
                        Gender.MALE -> "남성"
                        Gender.FEMALE -> "여성"
                        null -> "정보 없음"
                    }
                )

                Spacer(
                    modifier = Modifier
                        .weight(0.5f)
                        .heightIn(min = 16.dp)
                )
            }
        }

        // 하단 고정 버튼
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
        ) {
            if (isEditMode) {
                // 수정 모드: 저장 버튼
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
                                context = context,
                                nickname = nickname,
                                height = heightValue!!,
                                weight = weightValue!!,
                                profileImageUri = selectedImageUri
                            )
                        }
                    }
                )
            } else {
                // 읽기 모드: 로그아웃 버튼
                PrimaryButton(
                    text = if (logoutState == LogoutState.Loading) "로그아웃 중..." else "로그아웃",
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorPalette.Common.stopAccent,
                        contentColor = Color.White
                    )
                )
            }
        }
    }
}

/**
 * 프로필 이미지 섹션
 */
@Composable
private fun ProfileImageSection(
    profileImageUrl: String?,
    selectedImageUri: Uri?,
    isEditMode: Boolean,
    onImageClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(160.dp)
        ) {
            // 프로필 이미지
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = when {
                        // 1순위: 선택된 이미지 URI
                        selectedImageUri != null -> rememberAsyncImagePainter(selectedImageUri)
                        // 2순위: 기존 프로필 URL
                        profileImageUrl != null && profileImageUrl.isNotBlank() -> rememberAsyncImagePainter(profileImageUrl)
                        // 3순위: 기본 이미지
                        else -> painterResource(id = R.drawable.profile)
                    },
                    contentDescription = "프로필",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // 수정 모드일 때만 카메라 아이콘 표시
            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ColorPalette.Light.primary)
                        .clickable { onImageClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "이미지 업로드",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * 닉네임 입력 필드 (중복확인 버튼 없이)
 */
@Composable
private fun NicknameInputFieldWithoutButton(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    nicknameCheckState: NicknameCheckState,
    nicknameErrorMessage: String
) {
    Column {
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            CustomTextField(
                value = nickname,
                onValueChange = onNicknameChange,
                placeholder = "2-10자, 한글/영문/숫자만 가능"
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
 * 읽기 전용 필드 (값만 표시)
 * - 수정 모드의 입력 필드와 동일한 높이를 유지
 */
@Composable
private fun ReadOnlyField(value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // 값 표시
        Text(
            text = value,
            style = Typography.Body,
            color = ColorPalette.Light.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 밑줄
        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = ColorPalette.Light.component.copy(alpha = 0.3f),
            thickness = 1.dp
        )

        // 메시지 영역 (고정 높이 - 입력 섹션과 동일)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        )
    }
}
