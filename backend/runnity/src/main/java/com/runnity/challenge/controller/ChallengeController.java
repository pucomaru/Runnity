package com.runnity.challenge.controller;

import com.runnity.challenge.request.ChallengeCreateRequest;
import com.runnity.challenge.request.ChallengeJoinRequest;
import com.runnity.challenge.request.ChallengeListRequest;
import com.runnity.challenge.response.ChallengeJoinResponse;
import com.runnity.challenge.response.ChallengeListResponse;
import com.runnity.challenge.response.ChallengeResponse;
import com.runnity.challenge.service.ChallengeService;
import com.runnity.global.status.SuccessStatus;
import com.runnity.member.dto.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @Valid @RequestBody ChallengeCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getMemberId();
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
            @ParameterObject @ModelAttribute ChallengeListRequest request,
            @ParameterObject @PageableDefault(size = 10, page = 0) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getMemberId();
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
            @PathVariable Long challengeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getMemberId();
        ChallengeResponse response = challengeService.getChallenge(challengeId, memberId);
        return com.runnity.global.response.ApiResponse.success(
                SuccessStatus.OK,
                response
        );
    }

    @PostMapping("/{challengeId}/join")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "챌린지 참가 신청",
            description = "챌린지에 참가 신청합니다. 비밀방인 경우 비밀번호가 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "참가 신청이 정상적으로 완료된 경우"),
            @ApiResponse(responseCode = "400", description = "이미 참가 중이거나, 비밀번호가 일치하지 않는 경우"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자가 요청한 경우"),
            @ApiResponse(responseCode = "403", description = "모집 중이 아닌 챌린지이거나, 참가 제한 조건에 해당하는 경우"),
            @ApiResponse(responseCode = "404", description = "해당 챌린지가 존재하지 않는 경우"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<com.runnity.global.response.ApiResponse<ChallengeJoinResponse>> joinChallenge(
            @PathVariable Long challengeId,
            @RequestBody(required = false) ChallengeJoinRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getMemberId();
        ChallengeJoinResponse response = challengeService.joinChallenge(challengeId, request, memberId);
        return com.runnity.global.response.ApiResponse.success(
                SuccessStatus.CHALLENGE_JOINED,
                response
        );
    }

    @DeleteMapping("/{challengeId}/join")
    @Operation(
            summary = "챌린지 참가 취소",
            description = "챌린지 참가를 취소합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "참가 취소가 정상적으로 완료된 경우"),
            @ApiResponse(responseCode = "400", description = "이미 참가 취소했거나, 참가하지 않은 챌린지인 경우"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자가 요청한 경우"),
            @ApiResponse(responseCode = "404", description = "해당 챌린지가 존재하지 않는 경우"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<com.runnity.global.response.ApiResponse<ChallengeJoinResponse>> cancelChallenge(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getMemberId();
        ChallengeJoinResponse response = challengeService.cancelParticipation(challengeId, memberId);
        return com.runnity.global.response.ApiResponse.success(
                SuccessStatus.CHALLENGE_LEFT,
                response
        );
    }
}
