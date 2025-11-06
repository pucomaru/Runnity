package com.runnity.history.service;

import com.runnity.challenge.domain.Challenge;
import com.runnity.challenge.domain.ParticipationStatus;
import com.runnity.history.dto.ChallengeResponse;
import com.runnity.history.dto.MyChallengesResponse;
import com.runnity.history.repository.ChallengeParticipationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final ChallengeParticipationRepository repository;

    private static final int ENTERABLE_MINUTES_BEFORE_START = 5;

    public MyChallengesResponse getMyChallenges(Long memberId) {
        List<Challenge> waitingChallenges =
                repository.findChallengesByMemberIdAndStatus(
                        memberId,
                        ParticipationStatus.WAITING
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

        // 3. 입장 가능한 챌린지 찾기
        Challenge first = waitingChallenges.get(0);
        ChallengeResponse enterableChallenge = null;
        List<ChallengeResponse> joinedChallenges;

        if (isEnterable(first)) {
            // 첫 번째가 입장 가능
            enterableChallenge = ChallengeResponse.from(
                    first,
                    participantCountMap.getOrDefault(first.getChallengeId(), 0)
            );

            // 나머지 챌린지들
            joinedChallenges = waitingChallenges.stream()
                    .skip(1)
                    .map(c -> ChallengeResponse.from(
                            c,
                            participantCountMap.getOrDefault(c.getChallengeId(), 0)
                    ))
                    .toList();
        } else {
            // 입장 가능한 챌린지 없음
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
}
