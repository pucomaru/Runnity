package com.example.runnity.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

/**
 * 커스텀 텍스트 입력 필드 (밑줄 형태)
 * - 밑줄이 있는 입력창
 * - placeholder 지원
 * - 에러 메시지 표시
 * - 오른쪽 액션 영역 (버튼, 아이콘 등)
 *
 * @param value 현재 입력값
 * @param onValueChange 입력값 변경 콜백
 * @param placeholder placeholder 텍스트
 * @param modifier Modifier (선택사항)
 * @param keyboardType 키보드 타입 (기본: Text, Number 등)
 * @param isPassword 비밀번호 입력 여부 (기본: false)
 * @param isError 에러 상태 여부 (기본: false)
 * @param errorMessage 에러 메시지 (isError가 true일 때 표시)
 * @param leadingIcon 왼쪽 아이콘
 * @param trailingContent 오른쪽 영역 (버튼, 아이콘 등)
 * @param singleLine 한 줄 입력 여부 (기본: true)
 *
 * 사용 예시:
 * var text by remember { mutableStateOf("") }
 * CustomTextField(
 *     value = text,
 *     onValueChange = { text = it },
 *     placeholder = "챌린지 이름을 입력해 주세요"
 * )
 */
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    isError: Boolean = false,
    errorMessage: String = "",
    leadingIcon: ImageVector? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽 아이콘
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = ColorPalette.Light.component,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // 입력 필드
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                // placeholder 표시 (입력값이 비어있을 때)
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = Typography.Body,
                        color = ColorPalette.Light.component.copy(alpha = 0.6f)
                    )
                }

                // BasicTextField: 실제 입력 필드
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = Typography.Body.copy(
                        color = ColorPalette.Light.primary
                    ),
                    singleLine = singleLine,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 오른쪽 액션 영역
            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingContent()
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 밑줄
        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = if (isError) {
                Color(0xFFFF0000)  // 빨간색
            } else {
                ColorPalette.Light.component.copy(alpha = 0.3f)
            },
            thickness = 1.dp
        )

        // 에러 메시지
        if (isError && errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = Typography.Caption,
                color = Color(0xFFFF0000)  // 빨간색
            )
        }
    }
}

/**
 * 미리보기 (Preview)
 */
@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
private fun CustomTextFieldPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 빈 입력 필드 (placeholder 보임)
        CustomTextField(
            value = "",
            onValueChange = {},
            placeholder = "챌린지 이름을 입력해 주세요"
        )

        // 입력된 필드
        CustomTextField(
            value = "3km 달리기",
            onValueChange = {},
            placeholder = "챌린지 이름을 입력해 주세요"
        )

        // 숫자 입력 필드 (오른쪽 버튼 포함)
        CustomTextField(
            value = "10자 이내로 한글 영문 숫자만 입력해주세요",
            onValueChange = {},
            placeholder = "챌린지 이름을 입력해 주세요",
            trailingContent = {
                SmallPillButton(
                    text = "중복 확인",
                    selected = true,
                    onClick = {},
                    modifier = Modifier.width(80.dp)
                )
            }
        )

        // 에러 상태
        CustomTextField(
            value = "",
            onValueChange = {},
            placeholder = "챌린지 이름을 입력해 주세요",
            isError = true,
            errorMessage = "이미 존재하는 닉네임입니다."
        )
    }
}
