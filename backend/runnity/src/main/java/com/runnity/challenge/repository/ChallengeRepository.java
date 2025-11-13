package com.runnity.challenge.repository;

import com.runnity.challenge.domain.Challenge;
import com.runnity.challenge.domain.ChallengeDistance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    /**
     * 챌린지 목록 조회 - 참가자 수 포함 (인기순: 참가자 수 내림차순, 생성일 내림차순)
     * Object[] 반환: [Challenge, Long participantCount]
     */
    @Query("""
        SELECT c, COUNT(cp)
        FROM Challenge c
        LEFT JOIN ChallengeParticipation cp 
            ON cp.challenge.challengeId = c.challengeId
            AND cp.isDeleted = false
            AND cp.status != 'LEFT'
        WHERE c.isDeleted = false
        AND c.status = 'RECRUITING'
        AND (:keyword IS NULL OR c.title LIKE CONCAT('%', :keyword, '%'))
        AND (:distances IS NULL OR c.distance IN :distances)
        AND (:startDate IS NULL OR CAST(c.startAt AS DATE) >= :startDate)
        AND (:endDate IS NULL OR CAST(c.startAt AS DATE) <= :endDate)
        AND (:startTime IS NULL OR CAST(c.startAt AS TIME) >= :startTime)
        AND (:endTime IS NULL OR CAST(c.startAt AS TIME) <= :endTime)
        AND (:isPrivate IS NULL OR c.isPrivate = :isPrivate)
        GROUP BY c.challengeId
        ORDER BY COUNT(cp) DESC, c.createdAt DESC
    """)
    Page<Object[]> findChallengesWithParticipantCountOrderByPopular(
            @Param("keyword") String keyword,
            @Param("distances") List<ChallengeDistance> distances,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("isPrivate") Boolean isPrivate,
            Pageable pageable
    );

    /**
     * 챌린지 목록 조회 - 참가자 수 포함 (임박순: 시작일 오름차순 - 빨리 시작하는 순)
     * Object[] 반환: [Challenge, Long participantCount]
     */
    @Query("""
        SELECT c, COUNT(cp)
        FROM Challenge c
        LEFT JOIN ChallengeParticipation cp 
            ON cp.challenge.challengeId = c.challengeId
            AND cp.isDeleted = false
            AND cp.status != 'LEFT'
        WHERE c.isDeleted = false
        AND c.status = 'RECRUITING'
        AND (:keyword IS NULL OR c.title LIKE CONCAT('%', :keyword, '%'))
        AND (:distances IS NULL OR c.distance IN :distances)
        AND (:startDate IS NULL OR CAST(c.startAt AS DATE) >= :startDate)
        AND (:endDate IS NULL OR CAST(c.startAt AS DATE) <= :endDate)
        AND (:startTime IS NULL OR CAST(c.startAt AS TIME) >= :startTime)
        AND (:endTime IS NULL OR CAST(c.startAt AS TIME) <= :endTime)
        AND (:isPrivate IS NULL OR c.isPrivate = :isPrivate)
        GROUP BY c.challengeId
        ORDER BY c.startAt ASC
    """)
    Page<Object[]> findChallengesWithParticipantCountOrderByLatest(
            @Param("keyword") String keyword,
            @Param("distances") List<ChallengeDistance> distances,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("isPrivate") Boolean isPrivate,
            Pageable pageable
    );
}
