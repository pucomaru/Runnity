package com.runnity.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "소셜 로그인 요청 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    @Schema(description = "OAuth 공급자", example = "GOOGLE or KAKAO")
    private String provider;

    @Schema(description = "클라이언트가 전달하는 ID Token", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6...")
    private String idToken;
}
