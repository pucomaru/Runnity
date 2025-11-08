package com.runnity.challenge.service;

import com.runnity.challenge.domain.*;
import com.runnity.challenge.request.ChallengeCreateRequest;
import com.runnity.challenge.request.ChallengeListRequest;
import com.runnity.challenge.request.ChallengeSortType;
import com.runnity.challenge.request.ChallengeVisibility;
import com.runnity.challenge.response.ChallengeListItemResponse;
import com.runnity.challenge.response.ChallengeListResponse;
import com.runnity.challenge.response.ChallengeParticipantResponse;
import com.runnity.challenge.response.ChallengeResponse;
import com.runnity.challenge.repository.ChallengeParticipationRepository;
import com.runnity.challenge.repository.ChallengeRepository;
import com.runnity.global.exception.GlobalException;
import com.runnity.global.status.ErrorStatus;
import com.runnity.member.domain.Member;
import com.runnity.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipationRepository participationRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ChallengeResponse createChallenge(ChallengeCreateRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new GlobalException(ErrorStatus.MEMBER_NOT_FOUND));

        LocalDateTime startAt = request.startAt();
        LocalDateTime endAt = startAt.plusMinutes(request.distance().durationMinutes());
        validateTimeOverlap(member.getMemberId(), startAt, endAt);

        Challenge challenge = challengeRepository.save(request.toEntity());
        ChallengeParticipation hostParticipation = ChallengeParticipation.builder()
                .member(member)
                .challenge(challenge)
                .build();
        participationRepository.save(hostParticipation);

        ChallengeParticipantResponse hostResponse = ChallengeParticipantResponse.fromHost(member);
        log.info("챌린지 생성 완료: id={}, title={}, host={}",
                challenge.getChallengeId(),
                challenge.getTitle(),
                member.getNickname());

        return ChallengeResponse.from(
                challenge,
                1,
                true,
                List.of(hostResponse)
        );
    }

    public ChallengeListResponse getChallenges(ChallengeListRequest request, Long memberId) {
        // 정렬 기준
        Sort sort = request.sort() == ChallengeSortType.POPULAR
                ? Sort.by(Sort.Direction.DESC, "participantCount", "c.createdAt")
                : Sort.by(Sort.Direction.DESC, "c.createdAt");

        PageRequest pageRequest = PageRequest.of(request.page(), request.size(), sort);

        // 필터 조건
        Boolean isPrivateFilter = request.visibility() == ChallengeVisibility.PUBLIC ? false : null;

        // 챌린지 + 참가자 수 조회
        Page<Object[]> result = challengeRepository.findChallengesWithParticipantCount(
                request.keyword(),
                request.distance(),
                request.startAt(),
                request.endAt(),
                isPrivateFilter,
                pageRequest
        );

        if (result.isEmpty()) {
            return ChallengeListResponse.empty(pageRequest);
        }

        // 챌린지 ID 추출
        List<Long> challengeIds = result.stream()
                .map(arr -> ((Challenge) arr[0]).getChallengeId())
                .toList();

        // 사용자 참가 여부 조회
        Set<Long> joinedIds = Set.copyOf(participationRepository.findJoinedChallengeIds(challengeIds, memberId));

        // DTO 변환
        Page<ChallengeListItemResponse> items = result.map(arr ->
                ChallengeListItemResponse.from(
                        (Challenge) arr[0],
                        ((Long) arr[1]).intValue(),
                        joinedIds.contains(((Challenge) arr[0]).getChallengeId())
                )
        );

        return ChallengeListResponse.from(items);
    }

    private void validateTimeOverlap(Long memberId, LocalDateTime startAt, LocalDateTime endAt) {
        boolean hasOverlap = participationRepository.existsByMemberAndTimeOverlapAndStatusIn(
                memberId, startAt, endAt, ParticipationStatus.TIME_OVERLAP_STATUSES);

        if (hasOverlap) {
            log.warn("시간 중복 챌린지 생성 시도: memberId={}, startAt={}, endAt={}",
                    memberId, startAt, endAt);
            throw new GlobalException(ErrorStatus.CHALLENGE_TIME_OVERLAP);
        }
    }
}
