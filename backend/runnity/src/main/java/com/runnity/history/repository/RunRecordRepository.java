package com.runnity.history.repository;

import com.runnity.history.domain.RunRecord;
import com.runnity.history.domain.RunRecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RunRecordRepository extends JpaRepository<RunRecord, Long> {

    @Query("SELECT r FROM RunRecord r " +
            "WHERE r.member.memberId = :memberId " +
            "AND r.startAt >= :startDate " +
            "AND r.startAt < :endDate " +
            "AND r.isDeleted = false " +
            "ORDER BY r.startAt DESC")
    List<RunRecord> findByMemberIdAndPeriod(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<RunRecord> findTop5ByMember_MemberIdAndRunTypeAndIsDeletedFalseOrderByStartAtDesc(Long memberId, RunRecordType runType);

    @Query("SELECT r FROM RunRecord r " +
            "JOIN FETCH r.member " +
            "WHERE r.runRecordId = :runRecordId " +
            "AND r.isDeleted = false")
    Optional<RunRecord> findDetailById(@Param("runRecordId") Long runRecordId);
}
