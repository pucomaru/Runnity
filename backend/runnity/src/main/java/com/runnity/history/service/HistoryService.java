package com.runnity.history.service;

import com.runnity.challenge.domain.Challenge;
import com.runnity.challenge.domain.ParticipationStatus;
import com.runnity.global.exception.GlobalException;
import com.runnity.global.status.ErrorStatus;
import com.runnity.history.domain.RunRecord;
import com.runnity.history.domain.RunRecordType;
import com.runnity.history.dto.response.*;
import com.runnity.history.repository.HistoryChallengeParticipationRepository;
import com.runnity.history.repository.RunLapRepository;
import com.runnity.history.repository.RunRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoryService {

    private final HistoryChallengeParticipationRepository repository;
    private final RunRecordRepository runRecordRepository;
    private final RunLapRepository runLapRepository;

    private static final int ENTERABLE_MINUTES_BEFORE_START = 5;

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
        long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), challenge.getStartAt());
        return minutes <= ENTERABLE_MINUTES_BEFORE_START;
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

        return RunRecordDetailResponse.from(record, laps);
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
}
