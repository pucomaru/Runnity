package com.runnity.history.controller;

import com.runnity.global.response.ApiResponse;
import com.runnity.global.status.SuccessStatus;
import com.runnity.history.dto.MyChallengesResponse;
import com.runnity.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/challenges")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService service;

    // TODO: 추후 jwt에서 memberid 가져오게 변경 필요
    @GetMapping
    public ResponseEntity<ApiResponse<MyChallengesResponse>> getMyChallenges(){
        long memberId = 1;
        MyChallengesResponse response = service.getMyChallenges(memberId);

        return ApiResponse.success(SuccessStatus.OK, response);
    }
}
