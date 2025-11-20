package com.runnity.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "신규회원 시 추가정보 입력 response DTO")
@Getter
@AllArgsConstructor
public class AddInfoResponseDto {
    @Schema(description = "응답 메시지", example = "추가 정보가 성공적으로 저장되었습니다")
    private String message;
}
