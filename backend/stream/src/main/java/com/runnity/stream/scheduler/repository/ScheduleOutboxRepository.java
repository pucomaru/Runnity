package com.runnity.stream.scheduler.repository;

import com.runnity.stream.scheduler.domain.ScheduleOutbox;
import com.runnity.stream.scheduler.domain.ScheduleOutbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleOutboxRepository extends JpaRepository<ScheduleOutbox, Long> {

    @Query("SELECT so FROM ScheduleOutbox so WHERE so.status = :status AND so.isDeleted = false ORDER BY so.createdAt ASC")
    List<ScheduleOutbox> findByStatusOrderByCreatedAtAsc(@Param("status") OutboxStatus status);
}

