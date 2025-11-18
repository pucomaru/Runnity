package com.runnity.history.controller;

import com.runnity.global.response.ApiResponse;
import com.runnity.global.status.SuccessStatus;
import com.runnity.history.dto.request.RunRecordCreateRequest;
import com.runnity.history.dto.response.MyChallengesResponse;
import com.runnity.history.dto.response.RunRecordDetailResponse;
import com.runnity.history.dto.response.RunRecordMonthlyResponse;
import com.runnity.history.service.HistoryService;
import com.runnity.member.dto.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "내가 참가 신청한 챌린지 목록 조회",
            description = "내가 참가 신청한 챌린지 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<MyChallengesResponse>> getMyChallenges(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getMemberId();
        MyChallengesResponse response = service.getMyChallenges(memberId);

        return ApiResponse.success(SuccessStatus.OK, response);
    }

    @GetMapping("/runs/{runRecordId}")
    @Operation(
            summary = "참여한 챌린지 상세조회",
            description = "내가 참가했던 챌린지의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인의 운동 기록이 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "챌린지 정보 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<RunRecordDetailResponse>> getRunRecordDetail(
            @PathVariable Long runRecordId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getMemberId();

        RunRecordDetailResponse response = service.getRunRecordDetail(memberId, runRecordId);
        return ApiResponse.success(SuccessStatus.OK, response);
    }

    @GetMapping("/runs")
    @Operation(
            summary = "월간 참여한 챌린지 조회",
            description = "해당 기간(년-월)에 참여한 챌린지 목록를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
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
    @Operation(
            summary = "개인의 러닝 기록 저장",
            description = "러닝이 끝난 후 개인의 러닝 기록을 저장합니다.(개인 달리기/챌린지 둘다)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<Void>> createRunRecord(
            @Valid @RequestBody RunRecordCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getMemberId();
        service.createRunRecord(memberId, request);
        return ApiResponse.success(SuccessStatus.OK);
    }
}
