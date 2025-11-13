package com.runnity.websocket.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.websocket.dto.redis.ParticipantUpdateEvent;
import com.runnity.websocket.dto.redis.UserEnteredEvent;
import com.runnity.websocket.dto.redis.UserLeftEvent;
import com.runnity.websocket.dto.websocket.server.ParticipantUpdateMessage;
import com.runnity.websocket.dto.websocket.server.UserEnteredMessage;
import com.runnity.websocket.dto.websocket.server.UserLeftMessage;
import com.runnity.websocket.manager.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import jakarta.annotation.PostConstruct;
import java.util.Set;

/**
 * Redis Pub/Sub 메시지 리스너
 * 
 * 다른 WebSocket 서버에서 발행한 이벤트를 수신하여
 * 해당 챌린지의 참가자들에게 전달합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessageListener implements MessageListener {

    private final RedisMessageListenerContainer listenerContainer;
    private final SessionManager sessionManager;
    private final ObjectMapper objectMapper;

    private static final String CHANNEL_ENTER = "challenge:enter";
    private static final String CHANNEL_LEAVE = "challenge:leave";
    private static final String CHANNEL_UPDATE = "challenge:update";

    /**
     * 초기화: 채널 구독 등록
     */
    @PostConstruct
    public void init() {
        listenerContainer.addMessageListener(this, new ChannelTopic(CHANNEL_ENTER));
        listenerContainer.addMessageListener(this, new ChannelTopic(CHANNEL_LEAVE));
        listenerContainer.addMessageListener(this, new ChannelTopic(CHANNEL_UPDATE));
        log.info("Redis Pub/Sub 구독 시작: {}, {}, {}", CHANNEL_ENTER, CHANNEL_LEAVE, CHANNEL_UPDATE);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String payload = new String(message.getBody());

            log.debug("Redis 메시지 수신: channel={}, payload={}", channel, payload);

            switch (channel) {
                case CHANNEL_ENTER -> handleUserEntered(payload);
                case CHANNEL_LEAVE -> handleUserLeft(payload);
                case CHANNEL_UPDATE -> handleParticipantUpdate(payload);
                default -> log.warn("알 수 없는 채널: {}", channel);
            }
        } catch (Exception e) {
            log.error("Redis 메시지 처리 실패", e);
        }
    }

    /**
     * 사용자 입장 이벤트 처리
     */
    private void handleUserEntered(String payload) {
        try {
            UserEnteredEvent event = objectMapper.readValue(payload, UserEnteredEvent.class);
            Long challengeId = event.challengeId();
            Long enteredUserId = event.userId();

            log.info("입장 이벤트 수신: challengeId={}, userId={}, nickname={}", 
                    challengeId, enteredUserId, event.nickname());

            // 해당 챌린지의 모든 참가자에게 USER_ENTERED 메시지 전송 (입장한 본인 제외)
            UserEnteredMessage wsMessage = new UserEnteredMessage(
                    enteredUserId,
                    event.nickname(),
                    event.profileImage(),
                    0.0,
                    0.0
            );

            String wsMessageJson = objectMapper.writeValueAsString(wsMessage);
            broadcastToChallenge(challengeId, enteredUserId, wsMessageJson);

        } catch (Exception e) {
            log.error("입장 이벤트 처리 실패: payload={}", payload, e);
        }
    }

    /**
     * 사용자 퇴장 이벤트 처리
     */
    private void handleUserLeft(String payload) {
        try {
            UserLeftEvent event = objectMapper.readValue(payload, UserLeftEvent.class);
            Long challengeId = event.challengeId();
            Long leftUserId = event.userId();

            log.info("퇴장 이벤트 수신: challengeId={}, userId={}, reason={}", 
                    challengeId, leftUserId, event.reason());

            // 해당 챌린지의 모든 참가자에게 USER_LEFT 메시지 전송 (퇴장한 본인 제외)
            UserLeftMessage wsMessage = new UserLeftMessage(leftUserId, event.reason());
            String wsMessageJson = objectMapper.writeValueAsString(wsMessage);
            broadcastToChallenge(challengeId, leftUserId, wsMessageJson);

        } catch (Exception e) {
            log.error("퇴장 이벤트 처리 실패: payload={}", payload, e);
        }
    }

    /**
     * 참가자 정보 업데이트 이벤트 처리
     */
    private void handleParticipantUpdate(String payload) {
        try {
            ParticipantUpdateEvent event = objectMapper.readValue(payload, ParticipantUpdateEvent.class);
            Long challengeId = event.challengeId();
            Long updatedUserId = event.userId();

            log.debug("참가자 업데이트 이벤트 수신: challengeId={}, userId={}, distance={}, pace={}", 
                    challengeId, updatedUserId, event.distance(), event.pace());

            // 해당 챌린지의 모든 참가자에게 PARTICIPANT_UPDATE 메시지 전송 (업데이트한 본인 제외)
            ParticipantUpdateMessage wsMessage = new ParticipantUpdateMessage(
                    updatedUserId,
                    event.distance(),
                    event.pace()
            );

            String wsMessageJson = objectMapper.writeValueAsString(wsMessage);
            broadcastToChallenge(challengeId, updatedUserId, wsMessageJson);

        } catch (Exception e) {
            log.error("참가자 업데이트 이벤트 처리 실패: payload={}", payload, e);
        }
    }

    /**
     * 챌린지 참가자들에게 메시지 브로드캐스트 (특정 사용자 제외)
     * 
     * @param challengeId 챌린지 ID
     * @param excludeUserId 제외할 사용자 ID (이벤트 주체)
     * @param message 전송할 메시지
     */
    private void broadcastToChallenge(Long challengeId, Long excludeUserId, String message) {
        try {
            // Redis에서 해당 챌린지의 참가자 목록 조회
            Set<String> userIds = sessionManager.getChallengeParticipantIds(challengeId);

            int successCount = 0;
            int skipCount = 0;

            for (String userIdStr : userIds) {
                Long userId = Long.parseLong(userIdStr);

                // 이벤트 주체는 제외 (본인은 이미 알고 있음)
                if (userId.equals(excludeUserId)) {
                    skipCount++;
                    continue;
                }

                // 세션 조회 및 메시지 전송
                WebSocketSession session = sessionManager.getSession(challengeId, userId);
                if (session != null && session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                    successCount++;
                }
            }

            log.debug("메시지 브로드캐스트 완료: challengeId={}, 성공={}, 제외={}, 전체={}", 
                    challengeId, successCount, skipCount, userIds.size());

        } catch (Exception e) {
            log.error("메시지 브로드캐스트 실패: challengeId={}, excludeUserId={}", 
                    challengeId, excludeUserId, e);
        }
    }
}

