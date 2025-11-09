package com.runnity.challenge.repository;

import com.runnity.challenge.domain.ChallengeParticipation;
import com.runnity.challenge.domain.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface ChallengeParticipationRepository extends JpaRepository<ChallengeParticipation, Long> {

    /**
     * 특정 회원이 특정 시간대에 특정 상태의 챌린지에 참가 중인지 확인
     */
    @Query("""
        SELECT COUNT(cp) > 0
        FROM ChallengeParticipation cp
        JOIN cp.challenge c
        WHERE cp.member.memberId = :memberId
            AND cp.isDeleted = false
            AND c.isDeleted = false
            AND cp.status IN :statuses
            AND c.startAt < :endAt
            AND c.endAt > :startAt
    """)
    boolean existsByMemberAndTimeOverlapAndStatusIn(
            @Param("memberId") Long memberId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("statuses") Set<ParticipationStatus> statuses);

    /**
     * 사용자가 참가한 챌린지 ID 목록 조회
     */
    @Query("""
        SELECT c.challengeId
        FROM ChallengeParticipation cp
        JOIN cp.challenge c
        WHERE c.challengeId IN :challengeIds
        AND cp.member.memberId = :memberId
        AND cp.isDeleted = false
        AND c.isDeleted = false
        AND cp.status IN :activeStatuses
    """)
    List<Long> findJoinedChallengeIds(
            @Param("challengeIds") List<Long> challengeIds,
            @Param("memberId") Long memberId,
            @Param("activeStatuses") Set<com.runnity.challenge.domain.ParticipationStatus> activeStatuses
    );

    /**
     * 챌린지의 참가자 목록 조회 (LEFT 제외)
     * 랭킹 순으로 정렬
     */
    @Query("""
        SELECT cp
        FROM ChallengeParticipation cp
        WHERE cp.challenge.challengeId = :challengeId
        AND cp.isDeleted = false
        AND cp.status IN :activeStatuses
        ORDER BY 
            CASE WHEN cp.ranking IS NULL THEN 1 ELSE 0 END,
            cp.ranking ASC,
            cp.createdAt ASC
    """)
    List<ChallengeParticipation> findByChallengeIdAndActiveStatus(
            @Param("challengeId") Long challengeId,
            @Param("activeStatuses") Set<com.runnity.challenge.domain.ParticipationStatus> activeStatuses
    );

    /**
     * 특정 챌린지에 특정 회원이 참가했는지 확인 (LEFT 제외)
     */
    @Query("""
        SELECT COUNT(cp) > 0
        FROM ChallengeParticipation cp
        WHERE cp.challenge.challengeId = :challengeId
        AND cp.member.memberId = :memberId
        AND cp.isDeleted = false
        AND cp.status IN :activeStatuses
    """)
    boolean existsByChallengeIdAndMemberIdAndActiveStatus(
            @Param("challengeId") Long challengeId,
            @Param("memberId") Long memberId,
            @Param("activeStatuses") Set<com.runnity.challenge.domain.ParticipationStatus> activeStatuses
    );
}
