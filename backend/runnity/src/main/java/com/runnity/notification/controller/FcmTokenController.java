package com.runnity.notification.controller;

import com.runnity.global.response.ApiResponse;
import com.runnity.global.status.SuccessStatus;
import com.runnity.member.dto.UserPrincipal;
import com.runnity.notification.dto.request.FcmTokenDeleteRequest;
import com.runnity.notification.dto.request.FcmTokenRegisterRequest;
import com.runnity.notification.service.FcmTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    @PostMapping("/fcm-token")
    @Operation(
            summary = "fcm 토큰 저장",
            description = "로그인, 회원 가입시 발급받은 fcm 토큰을 저장합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "저장 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<com.runnity.global.response.ApiResponse<Void>> registerToken(
            @RequestBody FcmTokenRegisterRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getMemberId();

        fcmTokenService.resiterToken(memberId, request);
        return ApiResponse.success(SuccessStatus.FCM_SAVED);
    }

    @DeleteMapping("/fcm-token")
    @Operation(
            summary = "fcm 토큰 삭제",
            description = "로그 아웃 시 fcm 토큰을 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "fcm 토큰 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<com.runnity.global.response.ApiResponse<Void>> deleteToken(
            @RequestBody FcmTokenDeleteRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getMemberId();
        fcmTokenService.deleteToken(memberId, request);
        return ApiResponse.success(SuccessStatus.FCM_DELETE);
    }
}
