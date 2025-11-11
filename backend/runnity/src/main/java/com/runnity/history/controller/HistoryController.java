package com.runnity.history.controller;

import com.runnity.global.response.ApiResponse;
import com.runnity.global.status.SuccessStatus;
import com.runnity.history.dto.request.RunRecordCreateRequest;
import com.runnity.history.dto.response.MyChallengesResponse;
import com.runnity.history.dto.response.RunRecordDetailResponse;
import com.runnity.history.dto.response.RunRecordMonthlyResponse;
import com.runnity.history.service.HistoryService;
import com.runnity.member.dto.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService service;

    @GetMapping("/challenges")
    public ResponseEntity<ApiResponse<MyChallengesResponse>> getMyChallenges(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getMemberId();
        MyChallengesResponse response = service.getMyChallenges(memberId);

        return ApiResponse.success(SuccessStatus.OK, response);
    }

    @GetMapping("/runs/{runRecordId}")
    public ResponseEntity<ApiResponse<RunRecordDetailResponse>> getRunRecordDetail(
            @PathVariable Long runRecordId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getMemberId();

        RunRecordDetailResponse response = service.getRunRecordDetail(memberId, runRecordId);
        return ApiResponse.success(SuccessStatus.OK, response);
    }

    @GetMapping("/runs")
    public ResponseEntity<ApiResponse<RunRecordMonthlyResponse>> getRunRecordsByMonth(
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getMemberId();

        RunRecordMonthlyResponse response =
                service.getRunRecordsByMonth(memberId, year, month);

        return ApiResponse.success(SuccessStatus.OK, response);
    }

    @PostMapping("/runs")
    public ResponseEntity<ApiResponse<Void>> createRunRecord(
            @Valid @RequestBody RunRecordCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getMemberId();
        service.createRunRecord(memberId, request);
        return ApiResponse.success(SuccessStatus.OK);
    }
}
