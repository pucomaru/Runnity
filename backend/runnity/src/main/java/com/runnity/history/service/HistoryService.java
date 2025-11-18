package com.runnity.history.service;

import com.runnity.challenge.domain.Challenge;
import com.runnity.challenge.domain.ChallengeParticipation;
import com.runnity.challenge.domain.ParticipationStatus;
import com.runnity.global.exception.GlobalException;
import com.runnity.global.status.ErrorStatus;
import com.runnity.history.domain.RunLap;
import com.runnity.history.domain.RunRecord;
import com.runnity.history.domain.RunRecordType;
import com.runnity.history.dto.request.RunRecordCreateRequest;
import com.runnity.history.dto.response.*;
import com.runnity.history.repository.HistoryChallengeParticipationRepository;
import com.runnity.history.repository.RunLapRepository;
import com.runnity.history.repository.RunRecordRepository;
import com.runnity.member.domain.Member;
import com.runnity.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoryService {

    private final HistoryChallengeParticipationRepository repository;
    private final RunRecordRepository runRecordRepository;
    private final RunLapRepository runLapRepository;
    private final MemberRepository memberRepository;

    private static final int ENTERABLE_SECONDS_BEFORE_START = 300; // 5분

    public MyChallengesResponse getMyChallenges(Long memberId) {

        List<ParticipationStatus> targetStatuses = new java.util.ArrayList<>();
        targetStatuses.add(ParticipationStatus.WAITING);
        targetStatuses.addAll(ParticipationStatus.REJOINABLE_STATUSES);

        List<Challenge> waitingChallenges =
                repository.findChallengesByMemberIdAndStatus(
                        memberId,
                        targetStatuses
                );

        if (waitingChallenges.isEmpty()) {
            return MyChallengesResponse.builder()
                    .enterableChallenge(null)
                    .joinedChallenges(List.of())
                    .build();
        }

        List<Long> challengeIds = waitingChallenges.stream()
                .map(Challenge::getChallengeId)
                .toList();

        Map<Long, Integer> participantCountMap = repository.countByChallengeIds(challengeIds)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],           // challengeId
                        arr -> ((Long) arr[1]).intValue() // count
                ));

        Challenge first = waitingChallenges.get(0);
        ChallengeResponse enterableChallenge = null;
        List<ChallengeResponse> joinedChallenges;

        if (isEnterable(first)) {
            enterableChallenge = ChallengeResponse.from(
                    first,
                    participantCountMap.getOrDefault(first.getChallengeId(), 0)
            );

            joinedChallenges = waitingChallenges.stream()
                    .skip(1)
                    .map(c -> ChallengeResponse.from(
                            c,
                            participantCountMap.getOrDefault(c.getChallengeId(), 0)
                    ))
                    .toList();
        } else {
            joinedChallenges = waitingChallenges.stream()
                    .map(c -> ChallengeResponse.from(
                            c,
                            participantCountMap.getOrDefault(c.getChallengeId(), 0)
                    ))
                    .toList();
        }

        return MyChallengesResponse.builder()
                .enterableChallenge(enterableChallenge)
                .joinedChallenges(joinedChallenges)
                .build();
    }

    private boolean isEnterable(Challenge challenge) {
        long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), challenge.getStartAt());
        return seconds <= ENTERABLE_SECONDS_BEFORE_START;
    }

    public RunRecordDetailResponse getRunRecordDetail(Long memberId, Long runRecordId) {
        RunRecord record = runRecordRepository.findDetailById(runRecordId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.RUN_RECORD_NOT_FOUND));

        if (!record.getMember().getMemberId().equals(memberId)) {
            throw new GlobalException(ErrorStatus.RUN_RECORD_FORBIDDEN);
        }

        List<RunLapResponse> laps = runLapRepository.findByRunRecord_RunRecordIdAndIsDeletedFalseOrderBySequence(runRecordId)
                .stream()
                .map(RunLapResponse::from)
                .toList();

        // challengeId 조회 (챌린지 달리기인 경우에만 존재)
        Long challengeId = null;
        if (record.getRunType() == RunRecordType.CHALLENGE) {
            challengeId = repository.findByRunRecordId(runRecordId)
                    .map(cp -> cp.getChallenge().getChallengeId())
                    .orElse(null);
        }

        return RunRecordDetailResponse.from(record, laps, challengeId);
    }

    public RunRecordMonthlyResponse getRunRecordsByMonth(Long memberId, int year, int month) {

        LocalDateTime startDate = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime endDate = startDate.plusMonths(1);

        List<RunRecord> records = runRecordRepository.findByMemberIdAndPeriod(memberId, startDate, endDate);

        List<RunRecordResponse> personals = records.stream()
                .filter(r -> r.getRunType() == RunRecordType.PERSONAL)
                .map(RunRecordResponse::from)
                .toList();

        List<RunRecordResponse> challenges = records.stream()
                .filter(r -> r.getRunType() == RunRecordType.CHALLENGE)
                .map(RunRecordResponse::from)
                .toList();

        return RunRecordMonthlyResponse.of(personals, challenges);
    }

    @Transactional
    public void createRunRecord(Long memberId, RunRecordCreateRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.MEMBER_NOT_FOUND));

        RunRecord runRecord = RunRecord.builder()
                .member(member)
                .runType(request.runType())
                .distance(request.distance())
                .durationSec(request.durationSec())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .pace(request.pace())
                .bpm(request.bpm())
                .calories(request.calories())
                .route(request.route())
                .build();

        RunRecord savedRecord = runRecordRepository.save(runRecord);
        log.info("RunRecord saved - runRecordId={}", savedRecord.getRunRecordId());


        List<RunLap> laps = request.laps().stream()
                .map(lapReq -> RunLap.builder()
                        .runRecord(savedRecord)
                        .sequence(lapReq.sequence())
                        .distance(lapReq.distance())
                        .durationSec(lapReq.durationSec())
                        .pace(lapReq.pace())
                        .bpm(lapReq.bpm())
                        .build())
                .toList();

        runLapRepository.saveAll(laps);

        updateMemberAveragePace(member);

        if (request.runType() == RunRecordType.CHALLENGE) {
            if (request.challengeId() == null) {
                throw new GlobalException(ErrorStatus.CHALLENGE_ID_REQUIRED);
            }

            handleChallengeFinish(member, savedRecord, request.challengeId());
        }
    }

    private void updateMemberAveragePace(Member member) {
        List<RunRecord> recentRecords =
                runRecordRepository.findTop30ByMember_MemberIdAndIsDeletedFalseOrderByStartAtDesc(member.getMemberId());

        if (recentRecords.isEmpty()) {
            member.setAveragePace(null);
            return;
        }

        double averagePace = recentRecords.stream()
                .map(RunRecord::getPace)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        member.setAveragePace((int) Math.round(averagePace));
    }

    @Transactional
    public void handleChallengeFinish(Member member, RunRecord runRecord, Long challengeId) {

        log.info("handleChallengeFinish() called - memberId={}, challengeId={}, runRecordId={}", member.getMemberId(), challengeId, runRecord.getRunRecordId());
        var cp = repository.findCompletedByMemberIdAndChallengeId(member.getMemberId(), challengeId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.CHALLENGE_PARTICIPATION_NOT_FOUND));

        cp.updateRunRecord(runRecord);

        List<ChallengeParticipation> finishers =
                repository.findFinishersForRanking(challengeId);

        finishers.sort((a, b) ->
                a.getRunRecord().getDurationSec().compareTo(
                        b.getRunRecord().getDurationSec()
                )
        );

        int newRank = 1;
        for (ChallengeParticipation p : finishers) {
            if (!Objects.equals(p.getRanking(), newRank)) {
                p.updateRanking(newRank);
            }
            newRank++;
        }
    }

}
