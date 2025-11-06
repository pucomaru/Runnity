package com.runnity.challenge.service;

import com.runnity.challenge.domain.Challenge;
import com.runnity.challenge.domain.ChallengeParticipation;
import com.runnity.challenge.domain.ParticipationStatus;
import com.runnity.challenge.dto.ChallengeCreateRequest;
import com.runnity.challenge.dto.ChallengeParticipantResponse;
import com.runnity.challenge.dto.ChallengeResponse;
import com.runnity.challenge.repository.ChallengeParticipationRepository;
import com.runnity.challenge.repository.ChallengeRepository;
import com.runnity.global.exception.GlobalException;
import com.runnity.global.status.ErrorStatus;
import com.runnity.member.domain.Member;
import com.runnity.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
