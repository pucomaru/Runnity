package com.runnity.challenge.service;

import com.runnity.challenge.domain.*;
import com.runnity.challenge.request.ChallengeCreateRequest;
import com.runnity.challenge.request.ChallengeListRequest;
import com.runnity.challenge.request.ChallengeSortType;
import com.runnity.challenge.request.ChallengeVisibility;
import com.runnity.challenge.request.ChallengeJoinRequest;
import com.runnity.challenge.response.ChallengeJoinResponse;
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
import com.runnity.scheduler.domain.ScheduleOutbox;
import com.runnity.scheduler.repository.ScheduleOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final ScheduleOutboxRepository scheduleOutboxRepository;

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

        String payload = createSchedulePayload(challenge.getChallengeId(), startAt, endAt);
        ScheduleOutbox outbox = ScheduleOutbox.builder()
                .challengeId(challenge.getChallengeId())
                .eventType("SCHEDULE_CREATE")
                .payload(payload)
                .build();
        scheduleOutboxRepository.save(outbox);

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

        // Pageable의 Sort를 제거하여 @Query의 ORDER BY만 사용하도록 함
        // ChallengeSortType은 ChallengeListRequest.sort로 처리되므로 Pageable.sort와 충돌 방지
        Pageable pageableWithoutSort = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        Page<Object[]> result = request.sort() == ChallengeSortType.POPULAR
                ? challengeRepository.findChallengesWithParticipantCountOrderByPopular(
                        request.keyword(),
                        request.distance(),
                        request.startAt(),
                        request.endAt(),
                        isPrivateFilter,
                        pageableWithoutSort
                )
                : challengeRepository.findChallengesWithParticipantCountOrderByLatest(
                        request.keyword(),
                        request.distance(),
                        request.startAt(),
                        request.endAt(),
                        isPrivateFilter,
                        pageableWithoutSort
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
                        ParticipationStatus.TOTAL_APPLICANT_STATUSES
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
                ParticipationStatus.TOTAL_APPLICANT_STATUSES
        );

        int currentParticipants = participations.size();

        // 참가 여부 확인
        boolean joined = participationRepository.existsByChallengeIdAndMemberIdAndActiveStatus(
                challengeId,
                memberId,
                ParticipationStatus.TOTAL_APPLICANT_STATUSES
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

    private String createSchedulePayload(Long challengeId, LocalDateTime startAt, LocalDateTime endAt) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return String.format(
                "{\"challengeId\":%d,\"startAt\":\"%s\",\"endAt\":\"%s\"}",
                challengeId,
                startAt.format(formatter),
                endAt.format(formatter)
        );
    }

    @Transactional
    public ChallengeJoinResponse joinChallenge(Long challengeId, ChallengeJoinRequest request, Long memberId) {
        // 1. 회원 조회 (평균 페이스 필요)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.MEMBER_NOT_FOUND));

        // 2. 챌린지 검증
        Challenge challenge = validateAndGetChallenge(challengeId);
        
        if (challenge.getStatus() != ChallengeStatus.RECRUITING) {
            throw new GlobalException(ErrorStatus.CHALLENGE_NOT_RECRUITING);
        }

        // 3. 비밀번호 검증
        validatePassword(challenge, request);

        // 4. 참가 제한 조건 검증
        validateTimeOverlap(memberId, challenge.getStartAt(), challenge.getEndAt());
        validateParticipantLimit(challenge);

        // 5. 참가 정보 조회 또는 생성
        ChallengeParticipation participation = participationRepository
                .findByChallengeIdAndMemberId(challengeId, memberId)
                .orElse(null);

        if (participation != null) {
            // 기존 참가 정보가 있는 경우
            if (participation.isActive()) {
                throw new GlobalException(ErrorStatus.CHALLENGE_ALREADY_JOINED);
            }
            // LEFT 상태인 경우 재참가
            participation.rejoin();
            participation = participationRepository.save(participation);
            log.info("챌린지 재참가 신청 완료: challengeId={}, memberId={}, participantId={}",
                    challengeId, memberId, participation.getParticipantId());
        } else {
            // 새로운 참가 신청
            participation = ChallengeParticipation.builder()
                    .challenge(challenge)
                    .member(member)
                    .build();
            participation = participationRepository.save(participation);
            log.info("챌린지 참가 신청 완료: challengeId={}, memberId={}, participantId={}",
                    challengeId, memberId, participation.getParticipantId());
        }

        // 6. Response 반환
        return new ChallengeJoinResponse(
                participation.getParticipantId(),
                challenge.getChallengeId(),
                member.getMemberId(),
                participation.getStatus().code(),
                participation.getRanking(),
                member.getAveragePace()
        );
    }

    @Transactional
    public ChallengeJoinResponse cancelParticipation(Long challengeId, Long memberId) {
        // 1. 챌린지 검증
        Challenge challenge = validateAndGetChallenge(challengeId);

        // 2. 회원 조회 (평균 페이스 필요)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.MEMBER_NOT_FOUND));

        // 3. 참가 정보 조회
        ChallengeParticipation participation = participationRepository
                .findByChallengeIdAndMemberId(challengeId, memberId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.CHALLENGE_NOT_JOINED));

        // 4. 참가 취소 처리 (도메인 로직 활용)
        participation.cancel();
        participationRepository.save(participation);

        log.info("챌린지 참가 취소 완료: challengeId={}, memberId={}, participantId={}",
                challengeId, memberId, participation.getParticipantId());

        // 5. Response 반환
        return new ChallengeJoinResponse(
                participation.getParticipantId(),
                challenge.getChallengeId(),
                member.getMemberId(),
                participation.getStatus().code(),
                participation.getRanking(),
                member.getAveragePace()
        );
    }

    /**
     * 챌린지 존재 및 삭제 여부 검증
     */
    private Challenge validateAndGetChallenge(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.CHALLENGE_NOT_FOUND));
        
        if (challenge.isDeleted()) {
            throw new GlobalException(ErrorStatus.CHALLENGE_NOT_FOUND);
        }
        
        return challenge;
    }

    /**
     * 비밀번호 검증
     */
    private void validatePassword(Challenge challenge, ChallengeJoinRequest request) {
        if (!challenge.getIsPrivate()) {
            return;
        }
        
        String password = request != null ? request.password() : null;
        if (password == null || password.isBlank()) {
            throw new GlobalException(ErrorStatus.CHALLENGE_PASSWORD_MISMATCH);
        }
        if (!challenge.getPassword().equals(password)) {
            throw new GlobalException(ErrorStatus.CHALLENGE_PASSWORD_MISMATCH);
        }
    }

    /**
     * 참가 인원 제한 검증
     */
    private void validateParticipantLimit(Challenge challenge) {
        long currentCount = participationRepository.findByChallengeIdAndActiveStatus(
                challenge.getChallengeId(),
                ParticipationStatus.TOTAL_APPLICANT_STATUSES
        ).size();

        if (currentCount >= challenge.getMaxParticipants()) {
            throw new GlobalException(ErrorStatus.CHALLENGE_PARTICIPANT_LIMIT_EXCEEDED);
        }
    }

    /**
     * 시간 중복 검증
     */
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
