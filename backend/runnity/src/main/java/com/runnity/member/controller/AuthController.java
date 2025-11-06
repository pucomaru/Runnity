package com.runnity.member.controller;

import com.runnity.global.status.ErrorStatus;
import com.runnity.global.status.SuccessStatus;
import com.runnity.member.dto.LoginRequestDto;
import com.runnity.member.dto.LoginResponseDto;
import com.runnity.member.dto.TokenRequest;
import com.runnity.member.dto.TokenResponse;
import com.runnity.member.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "소셜 로그인, 토큰 재발급 API")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login/google")
    @Operation(summary = "구글 로그인", description = "구글 ID 토큰으로 로그인합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "ID 토큰 검증 실패")
    })
    public ResponseEntity<com.runnity.global.response.ApiResponse<LoginResponseDto>> googleLogin(
            @RequestBody LoginRequestDto request
    ) {
        try {
            LoginResponseDto resp = authService.googleLogin(request.getIdToken());
            return com.runnity.global.response.ApiResponse.success(
                    SuccessStatus.LOGIN_SUCCESS, resp
            );
        } catch (IllegalArgumentException e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login/kakao")
    @Operation(summary = "카카오 로그인", description = "카카오 ID 토큰으로 로그인합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "ID 토큰 검증 실패")
    })
    public ResponseEntity<com.runnity.global.response.ApiResponse<LoginResponseDto>> kakaoLogin(
            @RequestBody LoginRequestDto request
    ) {
        try {
            LoginResponseDto resp = authService.kakaoLogin(request.getIdToken());
            return com.runnity.global.response.ApiResponse.success(
                    SuccessStatus.LOGIN_SUCCESS, resp
            );
        } catch (IllegalArgumentException e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/token")
    @Operation(summary = "Access Token 재발급", description = "Refresh Token으로 새로운 Access/Refresh Token을 발급합니다")
    public ResponseEntity<com.runnity.global.response.ApiResponse<TokenResponse>> refreshToken(
            @RequestBody TokenRequest request
    ) {
        try {
            TokenResponse resp = authService.refreshAccessToken(request.getRefreshToken());
            return com.runnity.global.response.ApiResponse.success(SuccessStatus.OK, resp);
        } catch (IllegalArgumentException e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
