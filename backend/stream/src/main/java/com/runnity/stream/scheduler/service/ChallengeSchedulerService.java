package com.runnity.stream.scheduler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.stream.challenge.domain.Challenge;
import com.runnity.stream.challenge.domain.ChallengeStatus;
import com.runnity.stream.challenge.domain.ParticipationStatus;
import com.runnity.stream.challenge.repository.ChallengeParticipationRepository;
import com.runnity.stream.challenge.repository.ChallengeRepository;
import com.runnity.stream.scheduler.domain.ScheduleOutbox;
import com.runnity.stream.scheduler.dto.UserLeftEvent;
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
    private final ChallengeParticipationRepository participationRepository;
    private final ObjectMapper objectMapper;

    public ChallengeSchedulerService(
            RedisMessageListenerContainer listenerContainer,
            @Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate,
            @Qualifier("pubsubRedisTemplate") RedisTemplate<String, String> pubsubRedisTemplate,
            ScheduleOutboxRepository outboxRepository,
            ChallengeRepository challengeRepository,
            ChallengeParticipationRepository participationRepository,
            ObjectMapper objectMapper) {
        this.listenerContainer = listenerContainer;
        this.redisTemplate = redisTemplate;
        this.pubsubRedisTemplate = pubsubRedisTemplate;
        this.outboxRepository = outboxRepository;
        this.challengeRepository = challengeRepository;
        this.participationRepository = participationRepository;
        this.objectMapper = objectMapper;
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    @PostConstruct
    public void init() {
        listenerContainer.addMessageListener(this, new ChannelTopic("challenge:schedule:create"));
        listenerContainer.addMessageListener(this, new ChannelTopic("challenge:leave"));
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
                // 이벤트 타입에 따라 다르게 처리
                switch (outbox.getEventType()) {
                    case SCHEDULE_CREATE:
                        // Redis pub/sub으로 스케줄 생성 이벤트 발행
                        pubsubRedisTemplate.convertAndSend("challenge:schedule:create", outbox.getPayload());
                        log.info("스케줄 생성 이벤트 발행 완료: challengeId={}", outbox.getChallengeId());
                        break;
                        
                    case SCHEDULE_DELETE:
                        // 스케줄 삭제 처리 (Redis ZSET에서 제거)
                        deleteSchedule(outbox.getChallengeId());
                        log.info("스케줄 삭제 완료: challengeId={}", outbox.getChallengeId());
                        break;
                        
                    default:
                        log.warn("알 수 없는 이벤트 타입: {}, challengeId={}", 
                                outbox.getEventType(), outbox.getChallengeId());
                }

                outbox.markAsPublished();
                outboxRepository.save(outbox);

            } catch (Exception e) {
                outbox.markAsFailed();
                outboxRepository.save(outbox);
                log.error("Outbox 이벤트 처리 실패: challengeId={}, eventType={}", 
                        outbox.getChallengeId(), outbox.getEventType(), e);
            }
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String payload = new String(message.getBody());

            if ("challenge:schedule:create".equals(channel)) {
                handleScheduleCreate(payload);
            } else if ("challenge:leave".equals(channel)) {
                handleUserLeft(payload);
            } else {
                log.warn("알 수 없는 채널: {}", channel);
            }
        } catch (Exception e) {
            log.error("메시지 처리 실패: channel={}", new String(message.getChannel()), e);
        }
    }

    /**
     * 스케줄 생성 이벤트 처리
     */
    private void handleScheduleCreate(String payload) {
        try {
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

    /**
     * USER_LEFT 이벤트 처리
     * 참가자 상태 변경 시마다 모든 참가자 종료 여부를 체크합니다.
     */
    private void handleUserLeft(String payload) {
        try {
            UserLeftEvent event = objectMapper.readValue(payload, UserLeftEvent.class);
            Long challengeId = event.challengeId();

            log.debug("USER_LEFT 이벤트 수신: challengeId={}, userId={}, reason={}", 
                    challengeId, event.userId(), event.reason());

            // 모든 참가자 종료 여부 체크
            checkAndCompleteChallengeIfAllFinished(challengeId);

        } catch (Exception e) {
            log.error("USER_LEFT 이벤트 처리 실패", e);
        }
    }

    /**
     * 모든 참가자가 종료되었는지 체크하고, 종료되었으면 challenge:*:done 발행
     * 종료 시간 스케줄이 예약되어 있으면 취소합니다.
     */
    @Transactional(readOnly = true)
    public void checkAndCompleteChallengeIfAllFinished(Long challengeId) {
        try {
            Challenge challenge = challengeRepository.findById(challengeId).orElse(null);
            if (challenge == null || challenge.isDeleted()) {
                log.debug("챌린지를 찾을 수 없거나 삭제됨: challengeId={}", challengeId);
                return;
            }

            // 챌린지가 RUNNING 상태가 아니면 무시
            if (challenge.getStatus() != ChallengeStatus.RUNNING) {
                log.debug("챌린지가 RUNNING 상태가 아님: challengeId={}, status={}", 
                        challengeId, challenge.getStatus());
                return;
            }

            // 진행 중 상태(IN_PROGRESS_STATUSES) 참가자가 있는지 확인
            boolean hasInProgressParticipants = participationRepository.existsByChallengeIdAndStatusIn(
                    challengeId, ParticipationStatus.IN_PROGRESS_STATUSES);

            if (hasInProgressParticipants) {
                log.debug("아직 진행 중인 참가자가 있음: challengeId={}", challengeId);
                return;
            }

            // 모든 참가자가 종료되었으므로 challenge:*:done 발행
            String channel = "challenge:" + challengeId + ":done";
            String message = String.format("{\"challengeId\":%d}", challengeId);
            pubsubRedisTemplate.convertAndSend(channel, message);

            // 종료 시간 스케줄 취소
            cancelDoneSchedule(challengeId);

            log.info("모든 참가자 종료 확인, challenge:*:done 발행: challengeId={}", challengeId);

        } catch (Exception e) {
            log.error("모든 참가자 종료 체크 실패: challengeId={}", challengeId, e);
        }
    }

    /**
     * 종료 시간 스케줄 취소 (Redis ZSET에서 제거)
     */
    private void cancelDoneSchedule(Long challengeId) {
        try {
            redisTemplate.opsForZSet().remove("schedule:done", challengeId + ":DONE");
            log.debug("종료 시간 스케줄 취소: challengeId={}", challengeId);
        } catch (Exception e) {
            log.error("종료 시간 스케줄 취소 실패: challengeId={}", challengeId, e);
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

        // 내부 스케줄러에 등록 (새로 생성된 챌린지이므로 타이밍 이슈 대응을 위해 즉시 실행 허용)
        scheduleTask(challengeId, readyAt, "READY", true);
        scheduleTask(challengeId, startAt, "RUNNING", true);
        scheduleTask(challengeId, endAt, "DONE", true);

        log.info("스케줄 등록 완료: challengeId={}, ready={}, start={}, end={}",
                challengeId, readyAt, startAt, endAt);
    }

    private void scheduleTask(Long challengeId, LocalDateTime executeAt, String eventType) {
        scheduleTask(challengeId, executeAt, eventType, false);
    }

    private void scheduleTask(Long challengeId, LocalDateTime executeAt, String eventType, boolean allowImmediateExecution) {
        long delay = calculateDelay(executeAt);
        if (delay < 0) {
            if (allowImmediateExecution) {
                // 새로 생성된 챌린지의 타이밍 이슈 대응: 즉시 실행
                log.warn("과거 시간 감지, 즉시 실행: challengeId={}, executeAt={}, eventType={}", 
                        challengeId, executeAt, eventType);
                delay = 0;
            } else {
                // 서버 재시작 시 리스케줄링: 과거 시간은 스킵
                log.warn("과거 시간은 스케줄링 불가: challengeId={}, executeAt={}", challengeId, executeAt);
                return;
            }
        }

        scheduler.schedule(() -> {
            try {
                // DONE 스케줄 실행 시 챌린지 상태 확인
                if ("DONE".equals(eventType)) {
                    handleDoneSchedule(challengeId);
                } else {
                    String channel = "challenge:" + challengeId + ":" + eventType.toLowerCase();
                    String message = String.format("{\"challengeId\":%d,\"timestamp\":\"%s\"}",
                            challengeId, LocalDateTime.now().toString());
                    pubsubRedisTemplate.convertAndSend(channel, message);
                    log.info("스케줄 이벤트 발행: channel={}, challengeId={}", channel, challengeId);
                }
            } catch (Exception e) {
                log.error("스케줄 이벤트 발행 실패: challengeId={}, type={}", challengeId, eventType, e);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * DONE 스케줄 실행 처리
     * 챌린지가 이미 DONE 상태인지 확인하고, 아니면 challenge:*:done 발행
     */
    @Transactional(readOnly = true)
    private void handleDoneSchedule(Long challengeId) {
        try {
            Challenge challenge = challengeRepository.findById(challengeId).orElse(null);
            if (challenge == null || challenge.isDeleted()) {
                log.debug("챌린지를 찾을 수 없거나 삭제됨: challengeId={}", challengeId);
                return;
            }

            // 이미 DONE 상태이면 스케줄 무시 (중복 방지)
            if (challenge.getStatus() == ChallengeStatus.DONE) {
                log.debug("이미 DONE 상태인 챌린지, 스케줄 무시: challengeId={}", challengeId);
                return;
            }

            // DONE이 아니면 challenge:*:done 발행
            String channel = "challenge:" + challengeId + ":done";
            String message = String.format("{\"challengeId\":%d}", challengeId);
            pubsubRedisTemplate.convertAndSend(channel, message);
            log.info("종료 시간 도달, challenge:*:done 발행: challengeId={}", challengeId);

        } catch (Exception e) {
            log.error("DONE 스케줄 처리 실패: challengeId={}", challengeId, e);
        }
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

            // 챌린지가 삭제되었는지 확인
            Challenge challenge = challengeRepository.findById(challengeId).orElse(null);
            if (challenge == null || challenge.isDeleted()) {
                // 삭제된 챌린지는 Redis에서 제거
                redisTemplate.opsForZSet().remove(zsetKey, event);
                log.info("삭제된 챌린지 스케줄 제거: challengeId={}, type={}", challengeId, eventType);
                continue;
            }

            // SCHEDULE_DELETE 이벤트가 있는지 확인
            boolean hasDeleteEvent = outboxRepository
                    .findByStatusOrderByCreatedAtAsc(ScheduleOutbox.OutboxStatus.PUBLISHED)
                    .stream()
                    .anyMatch(outbox -> 
                        outbox.getChallengeId().equals(challengeId) 
                        && outbox.getEventType() == ScheduleOutbox.ScheduleEventType.SCHEDULE_DELETE
                    );

            if (hasDeleteEvent) {
                // 삭제 이벤트가 있으면 Redis에서 제거하고 재등록하지 않음
                redisTemplate.opsForZSet().remove(zsetKey, event);
                log.info("삭제 이벤트가 있는 챌린지 스케줄 제거: challengeId={}, type={}", 
                        challengeId, eventType);
                continue;
            }

            // 활성 챌린지만 재등록 (상태에 따라 적절한 이벤트만 재등록)
            if (challenge.getStatus() != ChallengeStatus.DONE) {
                // Challenge 테이블의 startAt, endAt을 기반으로 스케줄 재등록
                LocalDateTime executeAt = switch (eventType) {
                    case "READY" -> challenge.getStartAt().minusMinutes(5);
                    case "RUNNING" -> challenge.getStartAt();
                    case "DONE" -> challenge.getEndAt();
                    default -> null;
                };
                
                if (executeAt != null) {
                    // 상태에 따라 적절한 이벤트만 재등록 (중복 실행 방지)
                    boolean shouldReschedule = switch (eventType) {
                        case "READY" -> challenge.getStatus() == ChallengeStatus.RECRUITING;
                        case "RUNNING" -> challenge.getStatus() == ChallengeStatus.READY;
                        case "DONE" -> challenge.getStatus() == ChallengeStatus.RUNNING;
                        default -> false;
                    };
                    
                    if (shouldReschedule && executeAt.isAfter(now)) {
                        // 미래 시간인 경우에만 재등록 (과거 시간은 스킵)
                        scheduleTask(challengeId, executeAt, eventType, false);
                        log.info("리스케줄링: challengeId={}, type={}, executeAt={}, status={}",
                                challengeId, eventType, executeAt, challenge.getStatus());
                    } else {
                        // 이미 처리된 이벤트는 Redis에서 제거
                        redisTemplate.opsForZSet().remove(zsetKey, event);
                        log.info("이미 처리된 이벤트 스케줄 제거: challengeId={}, type={}, status={}",
                                challengeId, eventType, challenge.getStatus());
                    }
                }
            } else {
                // 완료된 챌린지는 Redis에서 제거
                redisTemplate.opsForZSet().remove(zsetKey, event);
                log.info("완료된 스케줄 삭제: challengeId={}, type={}", challengeId, eventType);
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

    /**
     * 스케줄 삭제 처리 (Redis ZSET에서 제거)
     */
    private void deleteSchedule(Long challengeId) {
        // Redis ZSET에서 해당 챌린지의 모든 스케줄 제거
        redisTemplate.opsForZSet().remove("schedule:ready", challengeId + ":READY");
        redisTemplate.opsForZSet().remove("schedule:running", challengeId + ":RUNNING");
        redisTemplate.opsForZSet().remove("schedule:done", challengeId + ":DONE");
        
        log.info("Redis에서 스케줄 삭제 완료: challengeId={}", challengeId);
    }
}

