package com.runnity.history.controller;

import com.runnity.global.response.ApiResponse;
import com.runnity.global.status.SuccessStatus;
import com.runnity.history.dto.response.StatsSummaryResponse;
import com.runnity.history.service.StatsService;
import com.runnity.member.dto.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "개인의 기간별 러닝 기록 통계 조회",
            description = "유전의 기간별(주/월/년/전체) 러닝 기록의 통계와 개인/챌린지 러닝 기록 5개씩을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
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
