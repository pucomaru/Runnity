package com.runnity.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "토큰 재발급 응답 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    @Schema(description = "새로 발급된 Access Token", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String accessToken;

    @Schema(description = "새로 발급된 Refresh Token", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String refreshToken;
}