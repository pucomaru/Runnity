package com.runnity.member.controller;

import com.runnity.global.status.ErrorStatus;
import com.runnity.global.status.SuccessStatus;
import com.runnity.member.dto.*;
import com.runnity.member.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
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

    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "현재 Access Token을 블랙리스트에 등록하여 로그아웃합니다. " +
                    "클라이언트에서도 저장된 토큰을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<com.runnity.global.response.ApiResponse<LogoutResponseDto>> logout(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request
    ) {
        try {
            if (userPrincipal == null) {
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.UNAUTHORIZED);
            }

            // Authorization 헤더에서 토큰 추출
            String accessToken = extractTokenFromRequest(request);
            if (accessToken == null) {
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.UNAUTHORIZED);
            }

            // 로그아웃 처리 (블랙리스트 등록)
            authService.logout(userPrincipal.getMemberId(), accessToken);
            return com.runnity.global.response.ApiResponse.success(SuccessStatus.OK);
        } catch (Exception e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
