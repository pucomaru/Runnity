package com.runnity.stream.scheduler.repository;

import com.runnity.stream.scheduler.domain.NotificationOutbox;
import com.runnity.stream.scheduler.domain.ScheduleOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    @Query("SELECT no FROM NotificationOutbox no WHERE no.status = :status AND no.isDeleted = false ORDER BY no.createdAt ASC")
    List<NotificationOutbox> findByStatusOrderByCreatedAtAsc(@Param("status") NotificationOutbox.OutboxStatus status);

}
