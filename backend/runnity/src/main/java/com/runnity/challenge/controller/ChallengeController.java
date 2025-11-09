package com.runnity.challenge.controller;

import com.runnity.challenge.request.ChallengeCreateRequest;
import com.runnity.challenge.request.ChallengeListRequest;
import com.runnity.challenge.response.ChallengeListResponse;
import com.runnity.challenge.response.ChallengeResponse;
import com.runnity.challenge.service.ChallengeService;
import com.runnity.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
@Tag(name = "Challenge", description = "챌린지 API")
public class ChallengeController {

    private final ChallengeService challengeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "챌린지 생성", description = "새로운 챌린지를 생성합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "챌린지 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 부족"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<com.runnity.global.response.ApiResponse<ChallengeResponse>> createChallenge(
            @Valid @RequestBody ChallengeCreateRequest request
    ) {
        Long memberId = getCurrentUserId();
        ChallengeResponse response = challengeService.createChallenge(request, memberId);
        return com.runnity.global.response.ApiResponse.success(
                SuccessStatus.CHALLENGE_CREATED,
                response
        );
    }

    @GetMapping
    @Operation(
            summary = "챌린지 목록 조회",
            description = "챌린지 목록을 조회합니다. (검색, 필터링, 정렬, 페이징 모두 지원)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<com.runnity.global.response.ApiResponse<ChallengeListResponse>> getChallenges(
            @ModelAttribute ChallengeListRequest request,
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        Long memberId = getCurrentUserId();
        ChallengeListResponse response = challengeService.getChallenges(request, pageable, memberId);
        return com.runnity.global.response.ApiResponse.success(
                SuccessStatus.OK,
                response
        );
    }

    @GetMapping("/{challengeId}")
    @Operation(
            summary = "챌린지 상세 조회",
            description = "특정 챌린지의 상세 정보와 참가자 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "챌린지 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<com.runnity.global.response.ApiResponse<ChallengeResponse>> getChallenge(
            @PathVariable Long challengeId
    ) {
        Long memberId = getCurrentUserId();
        ChallengeResponse response = challengeService.getChallenge(challengeId, memberId);
        return com.runnity.global.response.ApiResponse.success(
                SuccessStatus.OK,
                response
        );
    }

    private Long getCurrentUserId() {
        // TODO: 추후 인증 로직이 추가되면 수정 필요
        return 1L;
    }
}
