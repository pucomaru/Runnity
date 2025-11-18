package com.runnity.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "내 프로필 수정 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileUpdateRequestDto {

    @Schema(description = "닉네임(선택, 전달 시 변경)", example = "러너1")
    private String nickname;

    @Schema(description = "키(cm)", example = "175.4", nullable = true)
    private Float height;

    @Schema(description = "몸무게(kg)", example = "68.2", nullable = true)
    private Float weight;
}
