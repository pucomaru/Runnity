package com.runnity.history.controller;

import com.runnity.global.response.ApiResponse;
import com.runnity.global.status.SuccessStatus;
import com.runnity.history.dto.response.StatsSummaryResponse;
import com.runnity.history.service.StatsService;
import com.runnity.member.dto.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<StatsSummaryResponse>> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "week") String period,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getMemberId();
        StatsSummaryResponse response = statsService.getSummary(memberId, startDate, endDate, period);
        return ApiResponse.success(SuccessStatus.OK, response);
    }
}
