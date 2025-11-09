package com.runnity.challenge.repository;

import com.runnity.challenge.domain.Challenge;
import com.runnity.challenge.domain.ChallengeDistance;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

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
        AND (:keyword IS NULL OR c.title LIKE CONCAT('%', :keyword, '%'))
        AND (:distance IS NULL OR c.distance = :distance)
        AND (:startAt IS NULL OR c.startAt >= :startAt)
        AND (:endAt IS NULL OR c.endAt <= :endAt)
        AND (:isPrivate IS NULL OR c.isPrivate = :isPrivate)
        GROUP BY c.challengeId
        ORDER BY COUNT(cp) DESC, c.createdAt DESC
    """)
    Page<Object[]> findChallengesWithParticipantCountOrderByPopular(
            @Param("keyword") String keyword,
            @Param("distance") ChallengeDistance distance,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("isPrivate") Boolean isPrivate,
            Pageable pageable
    );

    /**
     * 챌린지 목록 조회 - 참가자 수 포함 (최신순: 생성일 내림차순)
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
        AND (:keyword IS NULL OR c.title LIKE CONCAT('%', :keyword, '%'))
        AND (:distance IS NULL OR c.distance = :distance)
        AND (:startAt IS NULL OR c.startAt >= :startAt)
        AND (:endAt IS NULL OR c.endAt <= :endAt)
        AND (:isPrivate IS NULL OR c.isPrivate = :isPrivate)
        GROUP BY c.challengeId
        ORDER BY c.createdAt DESC
    """)
    Page<Object[]> findChallengesWithParticipantCountOrderByLatest(
            @Param("keyword") String keyword,
            @Param("distance") ChallengeDistance distance,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("isPrivate") Boolean isPrivate,
            Pageable pageable
    );

    /**
     * 챌린지 조회 (비관적 락 적용 - SELECT FOR UPDATE)
     * 동시성 제어를 위해 사용
     * 참가 신청 시 최대 인원 체크와 저장을 원자적으로 처리
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Challenge c WHERE c.challengeId = :challengeId AND c.isDeleted = false")
    java.util.Optional<Challenge> findByIdWithLock(@Param("challengeId") Long challengeId);
}
