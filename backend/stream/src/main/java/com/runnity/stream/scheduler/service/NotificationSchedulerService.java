package com.runnity.stream.scheduler.service;

import com.runnity.stream.notification.service.NotificationService;
import com.runnity.stream.scheduler.domain.NotificationOutbox;
import com.runnity.stream.scheduler.domain.ScheduleOutbox;
import com.runnity.stream.scheduler.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSchedulerService {

    private final NotificationService notificationservice;
    private final NotificationOutboxRepository notificationOutboxRepository;

    @Scheduled(fixedDelay = 1000)
    public void pollAndSendNotifications(){
        List<NotificationOutbox> pendingList = notificationOutboxRepository
                .findByStatusOrderByCreatedAtAsc(NotificationOutbox.OutboxStatus.PENDING);

        if(pendingList.isEmpty()){
            return;
        }

        for(NotificationOutbox outbox : pendingList){
            processNotificationOutbox(outbox);
        }
    }

    @Transactional
    public void processNotificationOutbox(NotificationOutbox outbox){
        try {
            notificationservice.sendChallengeNotification(outbox.getChallengeId(), outbox.getType());
            outbox.markAsSent();
            notificationOutboxRepository.save(outbox);
            log.info("notificationOutbox 이벤트 발행 완료: challengeId={}", outbox.getChallengeId());
        }catch (Exception e){
            outbox.markAsFailed();
            notificationOutboxRepository.save(outbox);
            log.error("notificationOutbox 이벤트 발행 실패: challengeId={}", outbox.getChallengeId(), e);
        }
    }
}
