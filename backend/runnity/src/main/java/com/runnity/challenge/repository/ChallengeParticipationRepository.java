package com.runnity.challenge.repository;

import com.runnity.challenge.domain.ChallengeParticipation;
import com.runnity.challenge.domain.ParticipationStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Set;

public interface ChallengeParticipationRepository extends JpaRepository<ChallengeParticipation, Long> {

    /**
     * 특정 회원이 특정 시간대에 특정 상태의 챌린지에 참가 중인지 확인
     * @param memberId 회원 ID
     * @param startAt 챌린지 시작 시간
     * @param endAt 챌린지 종료 시간
     * @param statuses 참여 상태 집합
     * @return 시간 중복되는 참가 내역이 있으면 true
     */
    @Query("""
        SELECT COUNT(cp) > 0
        FROM ChallengeParticipation cp
        JOIN cp.challenge c
        WHERE cp.member.memberId = :memberId
            AND cp.isDeleted = false
            AND cp.status IN :statuses
            AND c.startAt < :endAt
            AND c.endAt > :startAt
    """)
    boolean existsByMemberAndTimeOverlapAndStatusIn(
            @Param("memberId") Long memberId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("statuses") Set<ParticipationStatus> statuses);
}
