package com.runnity.scheduler.repository;

import com.runnity.scheduler.domain.ScheduleOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleOutboxRepository extends JpaRepository<ScheduleOutbox, Long> {
}

