package com.runnity.notification.controller;

import com.runnity.global.response.ApiResponse;
import com.runnity.global.status.SuccessStatus;
import com.runnity.member.dto.UserPrincipal;
import com.runnity.notification.dto.request.FcmTokenDeleteRequest;
import com.runnity.notification.dto.request.FcmTokenRegisterRequest;
import com.runnity.notification.service.FcmTokenService;
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
    public ResponseEntity<ApiResponse<Void>> registerToken(
            @RequestBody FcmTokenRegisterRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getMemberId();

        fcmTokenService.resiterToken(memberId, request);
        return ApiResponse.success(SuccessStatus.FCM_SAVED);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteToken(
            @RequestBody FcmTokenDeleteRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getMemberId();
        fcmTokenService.deleteToken(memberId, request);
        return ApiResponse.success(SuccessStatus.FCM_DELETE);
    }
}
