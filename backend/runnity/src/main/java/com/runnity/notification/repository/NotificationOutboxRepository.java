package com.runnity.notification.repository;

import com.runnity.notification.domain.NotificationOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    @Modifying
    @Transactional
    @Query(
            value = """
            INSERT INTO notification_outbox
            (notification_outbox_id, created_at, is_deleted, updated_at,
             challenge_id, retry_count, sent_at, status, type)
            VALUES
            (1, NOW(), FALSE, NOW(),
             3,
             0,
             NULL,
             'PENDING',
             'CHALLENGE_READY')
            """,
            nativeQuery = true
    )
    int insertTestRow();
}

