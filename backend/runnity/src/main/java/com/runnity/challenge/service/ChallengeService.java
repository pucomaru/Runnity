package com.runnity.challenge.service;

import com.runnity.challenge.domain.*;
import com.runnity.challenge.request.*;
import com.runnity.challenge.response.*;
import com.runnity.challenge.repository.ChallengeParticipationRepository;
import com.runnity.challenge.repository.ChallengeRepository;
import com.runnity.global.config.WebSocketServerConfig;
import com.runnity.global.exception.GlobalException;
import com.runnity.global.service.WebSocketHealthCheckService;
import com.runnity.global.service.WebSocketTicketService;
import com.runnity.global.status.ErrorStatus;
import com.runnity.member.domain.Member;
import com.runnity.member.repository.MemberRepository;
import com.runnity.scheduler.domain.ScheduleOutbox;
import com.runnity.scheduler.repository.ScheduleOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipationRepository participationRepository;
    private final MemberRepository memberRepository;
    private final ScheduleOutboxRepository scheduleOutboxRepository;
    private final WebSocketTicketService wsTicketService;
    private final WebSocketServerConfig wsServerConfig;
    private final WebSocketHealthCheckService wsHealthCheckService;
    @Qualifier("stringRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;

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
                .eventType(ScheduleOutbox.ScheduleEventType.SCHEDULE_CREATE)
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

        List<ChallengeDistance> distances = (request.distances() == null || request.distances().isEmpty())
                ? null
                : request.distances();

        Pageable pageableWithoutSort = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        Page<Object[]> result = request.sort() == ChallengeSortType.POPULAR
                ? challengeRepository.findChallengesWithParticipantCountOrderByPopular(
                request.keyword(),
                distances,
                request.startDate(),
                request.endDate(),
                request.startTime(),
                request.endTime(),
                isPrivateFilter,
                pageableWithoutSort
        )
                : challengeRepository.findChallengesWithParticipantCountOrderByLatest(
                request.keyword(),
                distances,
                request.startDate(),
                request.endDate(),
                request.startTime(),
                request.endTime(),
                isPrivateFilter,
                pageableWithoutSort
        );

        List<Long> challengeIds = result.stream()
                .map(arr -> ((Challenge) arr[0]).getChallengeId())
                .toList();

        Set<Long> joinedIds = challengeIds.isEmpty()
                ? Set.of()
                : Set.copyOf(participationRepository.findJoinedChallengeIds(
                challengeIds,
                memberId,
                ParticipationStatus.TOTAL_APPLICANT_STATUSES
        ));

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

        List<ChallengeParticipation> participations = participationRepository.findByChallengeIdAndActiveStatus(
                challengeId,
                ParticipationStatus.TOTAL_APPLICANT_STATUSES
        );

        boolean joined = participationRepository.existsByChallengeIdAndMemberIdAndActiveStatus(
                challengeId,
                memberId,
                ParticipationStatus.TOTAL_APPLICANT_STATUSES
        );

        List<ChallengeParticipantResponse> participants = participations.stream()
                .map(ChallengeParticipantResponse::from)
                .toList();

        return ChallengeResponse.from(
                challenge,
                participations.size(),
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
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.MEMBER_NOT_FOUND));

        Challenge challenge = validateAndGetChallenge(challengeId);

        if (challenge.getStatus() != ChallengeStatus.RECRUITING) {
            throw new GlobalException(ErrorStatus.CHALLENGE_NOT_RECRUITING);
        }

        validatePassword(challenge, request);
        validateTimeOverlap(memberId, challenge.getStartAt(), challenge.getEndAt());
        validateParticipantLimit(challenge);

        ChallengeParticipation participation = participationRepository
                .findByChallengeIdAndMemberId(challengeId, memberId)
                .orElse(null);

        if (participation != null) {
            if (participation.isActive()) {
                throw new GlobalException(ErrorStatus.CHALLENGE_ALREADY_JOINED);
            }
            participation.rejoin();
            participationRepository.save(participation);
        } else {
            participation = participationRepository.save(
                    ChallengeParticipation.builder()
                            .challenge(challenge)
                            .member(member)
                            .build()
            );
        }

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
        Challenge challenge = validateAndGetChallenge(challengeId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.MEMBER_NOT_FOUND));

        ChallengeParticipation participation = participationRepository
                .findByChallengeIdAndMemberId(challengeId, memberId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.CHALLENGE_NOT_JOINED));

        participation.cancel();
        participationRepository.save(participation);

        deleteChallengeIfNoParticipants(challenge);

        return new ChallengeJoinResponse(
                participation.getParticipantId(),
                challenge.getChallengeId(),
                member.getMemberId(),
                participation.getStatus().code(),
                participation.getRanking(),
                member.getAveragePace()
        );
    }

    private Challenge validateAndGetChallenge(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.CHALLENGE_NOT_FOUND));

        if (challenge.isDeleted()) {
            throw new GlobalException(ErrorStatus.CHALLENGE_NOT_FOUND);
        }

        return challenge;
    }

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

    private void validateParticipantLimit(Challenge challenge) {
        long count = participationRepository.findByChallengeIdAndActiveStatus(
                challenge.getChallengeId(),
                ParticipationStatus.TOTAL_APPLICANT_STATUSES
        ).size();

        if (count >= challenge.getMaxParticipants()) {
            throw new GlobalException(ErrorStatus.CHALLENGE_PARTICIPANT_LIMIT_EXCEEDED);
        }
    }

    private void validateTimeOverlap(Long memberId, LocalDateTime startAt, LocalDateTime endAt) {
        boolean hasOverlap = participationRepository.existsByMemberAndTimeOverlapAndStatusIn(
                memberId, startAt, endAt, ParticipationStatus.TIME_OVERLAP_STATUSES);

        if (hasOverlap) {
            throw new GlobalException(ErrorStatus.CHALLENGE_TIME_OVERLAP);
        }
    }

    @Transactional
    public ChallengeEnterResponse enterChallenge(Long challengeId, Long memberId) {
        Challenge challenge = validateAndGetChallenge(challengeId);
        challenge.validateEnterable();

        // Member 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.MEMBER_NOT_FOUND));

        ChallengeParticipation participation = participationRepository
                .findByChallengeIdAndMemberId(challengeId, memberId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.CHALLENGE_NOT_JOINED));

        participation.startRunning();
        participationRepository.save(participation);
        participationRepository.flush();

        // Redis에 저장된 actualParticipantCount 증가
        incrementActualParticipantCount(challengeId);

        String ticket = wsTicketService.issueTicket(
                memberId,
                challengeId,
                WebSocketTicketService.TicketType.ENTER,
                member.getNickname(),
                member.getProfileImage()
        );

        String wsUrl = selectWebSocketServer(memberId, challengeId);

        return new ChallengeEnterResponse(
                ticket,
                memberId,
                challengeId,
                wsUrl,
                wsTicketService.getTicketTtl()
        );
    }

    private String selectWebSocketServer(Long memberId, Long challengeId) {
        int count = wsServerConfig.getServerCount();
        if (count == 0) {
            throw new GlobalException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }

        String hashKey = memberId + ":" + challengeId;
        int primaryIndex = Math.abs(hashKey.hashCode()) % count;
        String primaryUrl = wsServerConfig.getServerUrl(primaryIndex);

        if (wsHealthCheckService.isHealthy(primaryUrl)) {
            return primaryUrl;
        }

        wsHealthCheckService.markAsDown(primaryUrl);

        for (int i = 0; i < count; i++) {
            if (i == primaryIndex) continue;

            String fallback = wsServerConfig.getServerUrl(i);
            if (wsHealthCheckService.isHealthy(fallback)) {
                return fallback;
            }
        }

        throw new GlobalException(ErrorStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Redis에 저장된 actualParticipantCount 증가
     * 챌린지 입장 시 호출됩니다.
     * 
     * @param challengeId 챌린지 ID
     */
    private void incrementActualParticipantCount(Long challengeId) {
        try {
            String metaKey = "challenge:" + challengeId + ":meta";
            Long newCount = redisTemplate.opsForHash().increment(metaKey, "actualParticipantCount", 1);
            
            log.debug("actualParticipantCount 증가: challengeId={}, newCount={}", challengeId, newCount);
        } catch (Exception e) {
            log.error("actualParticipantCount 증가 실패: challengeId={}", challengeId, e);
            // Redis 업데이트 실패해도 입장 처리는 계속 진행
        }
    }

    private void deleteChallengeIfNoParticipants(Challenge challenge) {
        if (challenge.getStatus() != ChallengeStatus.RECRUITING) {
            return;
        }

        long activeCount = participationRepository.findByChallengeIdAndActiveStatus(
                challenge.getChallengeId(),
                ParticipationStatus.TOTAL_APPLICANT_STATUSES
        ).size();

        if (activeCount == 0) {
            challenge.delete();
            challengeRepository.save(challenge);

            String payload = String.format("{\"challengeId\":%d}", challenge.getChallengeId());
            ScheduleOutbox outbox = ScheduleOutbox.builder()
                    .challengeId(challenge.getChallengeId())
                    .eventType(ScheduleOutbox.ScheduleEventType.SCHEDULE_DELETE)
                    .payload(payload)
                    .build();
            scheduleOutboxRepository.save(outbox);

            log.info("참가자 0명으로 인한 챌린지 삭제 완료: challengeId={}", challenge.getChallengeId());
        }
    }
}
