package com.runnity.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "로그인 응답 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    @Schema(description = "발급된 Access Token", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String accessToken;

    @Schema(description = "발급된 Refresh Token", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String refreshToken;

    @Schema(description = "신규 가입 여부", example = "true")
    private boolean isNewUser;
}