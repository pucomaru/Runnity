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
import org.springframework.data.domain.Pageable;
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

    public ChallengeListResponse getChallenges(ChallengeListRequest request, Pageable pageable, Long memberId) {
        Boolean isPrivateFilter = request.visibility() == ChallengeVisibility.PUBLIC ? false : null;

        Page<Object[]> result = request.sort() == ChallengeSortType.POPULAR
                ? challengeRepository.findChallengesWithParticipantCountOrderByPopular(
                        request.keyword(),
                        request.distance(),
                        request.startAt(),
                        request.endAt(),
                        isPrivateFilter,
                        pageable
                )
                : challengeRepository.findChallengesWithParticipantCountOrderByLatest(
                        request.keyword(),
                        request.distance(),
                        request.startAt(),
                        request.endAt(),
                        isPrivateFilter,
                        pageable
                );

        // 챌린지 ID 추출
        List<Long> challengeIds = result.stream()
                .map(arr -> ((Challenge) arr[0]).getChallengeId())
                .toList();

        // 사용자 참가 여부 조회
        Set<Long> joinedIds = challengeIds.isEmpty()
                ? Set.of()
                : Set.copyOf(participationRepository.findJoinedChallengeIds(
                        challengeIds,
                        memberId,
                        ParticipationStatus.ACTIVE_PARTICIPATION_STATUSES
                ));

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

    public ChallengeResponse getChallenge(Long challengeId, Long memberId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.CHALLENGE_NOT_FOUND));

        if (challenge.isDeleted()) {
            throw new GlobalException(ErrorStatus.CHALLENGE_NOT_FOUND);
        }

        // 참가자 목록 조회 (LEFT 제외)
        List<ChallengeParticipation> participations = participationRepository.findByChallengeIdAndActiveStatus(
                challengeId,
                ParticipationStatus.ACTIVE_PARTICIPATION_STATUSES
        );

        int currentParticipants = participations.size();

        // 참가 여부 확인
        boolean joined = participationRepository.existsByChallengeIdAndMemberIdAndActiveStatus(
                challengeId,
                memberId,
                ParticipationStatus.ACTIVE_PARTICIPATION_STATUSES
        );

        // 참가자 DTO 변환
        List<ChallengeParticipantResponse> participants = participations.stream()
                .map(ChallengeParticipantResponse::from)
                .toList();

        return ChallengeResponse.from(
                challenge,
                currentParticipants,
                joined,
                participants
        );
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
