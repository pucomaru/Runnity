package com.runnity.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "신규 회원 추가정보 입력 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddInfoRequestDto {
    @Schema(description = "닉네임", example = "러너상경", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;

    @Schema(description = "키(cm)", example = "175.4", nullable = true)
    private Float height;

    @Schema(description = "몸무게(kg)", example = "68.2", nullable = true)
    private Float weight;

    @Schema(description = "성별(MALE|FEMALE)", example = "MALE", nullable = true, allowableValues = {"MALE", "FEMALE"})
    private String gender;

    @Schema(description = "생년월일(yyyy-MM-dd)", example = "1998-09-17", nullable = true)
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "생년월일은 yyyy-MM-dd 형식이어야 합니다.")
    private String birth;
}
