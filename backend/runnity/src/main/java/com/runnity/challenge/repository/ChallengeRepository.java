package com.runnity.challenge.repository;

import com.runnity.challenge.domain.Challenge;
import com.runnity.challenge.domain.ChallengeDistance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    /**
     * 챌린지 목록 조회 - 참가자 수 포함
     * Object[] 반환: [Challenge, Long participantCount]
     */
    @Query("""
        SELECT c, COUNT(cp)
        FROM Challenge c
        LEFT JOIN ChallengeParticipation cp 
            ON cp.challenge.challengeId = c.challengeId
            AND cp.isDeleted = false
            AND cp.status NOT IN ('QUIT', 'KICKED', 'LEFT')
        WHERE c.isDeleted = false
        AND (:keyword IS NULL OR c.title LIKE %:keyword%)
        AND (:distance IS NULL OR c.distance = :distance)
        AND (:startAt IS NULL OR c.startAt >= :startAt)
        AND (:endAt IS NULL OR c.endAt <= :endAt)
        AND (:isPrivate IS NULL OR c.isPrivate = :isPrivate)
        GROUP BY c.challengeId
    """)
    Page<Object[]> findChallengesWithParticipantCount(
            @Param("keyword") String keyword,
            @Param("distance") ChallengeDistance distance,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("isPrivate") Boolean isPrivate,
            Pageable pageable
    );
}
