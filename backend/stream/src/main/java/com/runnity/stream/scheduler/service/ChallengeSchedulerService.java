package com.runnity.stream.scheduler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.stream.challenge.domain.Challenge;
import com.runnity.stream.challenge.domain.ChallengeStatus;
import com.runnity.stream.challenge.repository.ChallengeRepository;
import com.runnity.stream.scheduler.domain.ScheduleOutbox;
import com.runnity.stream.scheduler.repository.ScheduleOutboxRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ChallengeSchedulerService implements MessageListener {

    private final RedisMessageListenerContainer listenerContainer;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, String> pubsubRedisTemplate;
    private final ScheduleOutboxRepository outboxRepository;
    private final ChallengeRepository challengeRepository;
    private final ObjectMapper objectMapper;

    public ChallengeSchedulerService(
            RedisMessageListenerContainer listenerContainer,
            @Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate,
            @Qualifier("pubsubRedisTemplate") RedisTemplate<String, String> pubsubRedisTemplate,
            ScheduleOutboxRepository outboxRepository,
            ChallengeRepository challengeRepository,
            ObjectMapper objectMapper) {
        this.listenerContainer = listenerContainer;
        this.redisTemplate = redisTemplate;
        this.pubsubRedisTemplate = pubsubRedisTemplate;
        this.outboxRepository = outboxRepository;
        this.challengeRepository = challengeRepository;
        this.objectMapper = objectMapper;
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    @PostConstruct
    public void init() {
        listenerContainer.addMessageListener(this, new ChannelTopic("challenge:schedule:create"));
        log.info("ChallengeSchedulerService 초기화 완료");

        rescheduleOnStartup();
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void pollAndPublishOutbox() {
        List<ScheduleOutbox> pendingOutboxes = outboxRepository
                .findByStatusOrderByCreatedAtAsc(ScheduleOutbox.OutboxStatus.PENDING);

        for (ScheduleOutbox outbox : pendingOutboxes) {
            try {
                pubsubRedisTemplate.convertAndSend("challenge:schedule:create", outbox.getPayload());

                outbox.markAsPublished();
                outboxRepository.save(outbox);

                log.info("Outbox 이벤트 발행 완료: challengeId={}", outbox.getChallengeId());
            } catch (Exception e) {
                outbox.markAsFailed();
                outboxRepository.save(outbox);
                log.error("Outbox 이벤트 발행 실패: challengeId={}", outbox.getChallengeId(), e);
            }
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody());
            log.info("스케줄 생성 이벤트 수신: {}", payload);

            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            Long challengeId = ((Number) data.get("challengeId")).longValue();
            LocalDateTime startAt = LocalDateTime.parse((String) data.get("startAt"));
            LocalDateTime endAt = LocalDateTime.parse((String) data.get("endAt"));

            registerSchedule(challengeId, startAt, endAt);
        } catch (Exception e) {
            log.error("스케줄 등록 실패", e);
        }
    }

    private void registerSchedule(Long challengeId, LocalDateTime startAt, LocalDateTime endAt) {
        // Redis ZSET에 이미 존재하는지 확인 (중복 등록 방지)
        if (redisTemplate.opsForZSet().score("schedule:running", challengeId + ":RUNNING") != null) {
            log.warn("이미 등록된 스케줄: challengeId={}", challengeId);
            return;
        }

        LocalDateTime readyAt = startAt.minusMinutes(5);

        // Redis ZSET에 스케줄 정보 저장
        redisTemplate.opsForZSet().add("schedule:ready", challengeId + ":READY", toTimestamp(readyAt));
        redisTemplate.opsForZSet().add("schedule:running", challengeId + ":RUNNING", toTimestamp(startAt));
        redisTemplate.opsForZSet().add("schedule:done", challengeId + ":DONE", toTimestamp(endAt));

        // 내부 스케줄러에 등록
        scheduleTask(challengeId, readyAt, "READY");
        scheduleTask(challengeId, startAt, "RUNNING");
        scheduleTask(challengeId, endAt, "DONE");

        log.info("스케줄 등록 완료: challengeId={}, ready={}, start={}, end={}",
                challengeId, readyAt, startAt, endAt);
    }

    private void scheduleTask(Long challengeId, LocalDateTime executeAt, String eventType) {
        long delay = calculateDelay(executeAt);
        if (delay < 0) {
            log.warn("과거 시간은 스케줄링 불가: challengeId={}, executeAt={}", challengeId, executeAt);
            return;
        }

        scheduler.schedule(() -> {
            try {
                String channel = "challenge:" + challengeId + ":" + eventType.toLowerCase();
                String message = String.format("{\"challengeId\":%d,\"timestamp\":\"%s\"}",
                        challengeId, LocalDateTime.now().toString());
                pubsubRedisTemplate.convertAndSend(channel, message);
                log.info("스케줄 이벤트 발행: channel={}, challengeId={}", channel, challengeId);
            } catch (Exception e) {
                log.error("스케줄 이벤트 발행 실패: challengeId={}, type={}", challengeId, eventType, e);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void rescheduleOnStartup() {
        try {
            LocalDateTime now = LocalDateTime.now();

            rescheduleFromZset("schedule:ready", "READY", now);
            rescheduleFromZset("schedule:running", "RUNNING", now);
            rescheduleFromZset("schedule:done", "DONE", now);

            log.info("리스케줄링 완료");
        } catch (Exception e) {
            log.error("리스케줄링 실패", e);
        }
    }

    private void rescheduleFromZset(String zsetKey, String eventType, LocalDateTime now) {
        Set<String> events = redisTemplate.opsForZSet()
                .rangeByScore(zsetKey, toTimestamp(now), Double.MAX_VALUE);

        if (events == null || events.isEmpty()) {
            return;
        }

        for (String event : events) {
            String[] parts = event.split(":");
            Long challengeId = Long.parseLong(parts[0]);

            Challenge challenge = challengeRepository.findById(challengeId).orElse(null);
            if (challenge != null && challenge.getStatus() != ChallengeStatus.DONE) {
                // Challenge 테이블의 startAt, endAt을 기반으로 스케줄 재등록
                LocalDateTime executeAt = switch (eventType) {
                    case "READY" -> challenge.getStartAt().minusMinutes(5);
                    case "RUNNING" -> challenge.getStartAt();
                    case "DONE" -> challenge.getEndAt();
                    default -> null;
                };
                if (executeAt != null && executeAt.isAfter(now)) {
                    scheduleTask(challengeId, executeAt, eventType);
                    log.info("리스케줄링: challengeId={}, type={}, executeAt={}",
                            challengeId, eventType, executeAt);
                }
            } else {
                // 완료된 챌린지나 존재하지 않는 챌린지는 Redis에서 제거
                redisTemplate.opsForZSet().remove(zsetKey, event);
                log.info("만료된 스케줄 삭제: challengeId={}, type={}", challengeId, eventType);
            }
        }
    }

    private long calculateDelay(LocalDateTime executeAt) {
        return Date.from(executeAt.atZone(ZoneId.systemDefault()).toInstant()).getTime()
                - System.currentTimeMillis();
    }

    private double toTimestamp(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()).getTime();
    }
}

