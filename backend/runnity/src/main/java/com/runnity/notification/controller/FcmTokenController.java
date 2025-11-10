package com.runnity.notification.controller;

import com.runnity.global.response.ApiResponse;
import com.runnity.global.status.SuccessStatus;
import com.runnity.notification.dto.request.FcmTokenDeleteRequest;
import com.runnity.notification.dto.request.FcmTokenRegisterRequest;
import com.runnity.notification.service.FcmTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    // TODO: 추후 jwt에서 memberid 가져오게 변경 필요
    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> registerToken(
            @RequestBody FcmTokenRegisterRequest request
    ) {
        long memberId = 2L;

        fcmTokenService.resiterToken(memberId, request);
        return ApiResponse.success(SuccessStatus.FCM_SAVED);
    }

    // TODO: 추후 jwt에서 memberid 가져오게 변경 필요
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteToken(
            @RequestBody FcmTokenDeleteRequest request
    ) {
        long memberId = 2L;
        fcmTokenService.deleteToken(memberId, request);
        return ApiResponse.success(SuccessStatus.FCM_DELETE);
    }
}
