package com.runnity.websocket.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.websocket.dto.websocket.client.KickedMessage;
import com.runnity.websocket.dto.websocket.client.PongMessage;
import com.runnity.websocket.dto.websocket.client.RecordMessage;
import com.runnity.websocket.dto.websocket.server.ErrorMessage;
import com.runnity.websocket.dto.websocket.server.ConnectedMessage;
import com.runnity.websocket.enums.LeaveReason;
import com.runnity.websocket.exception.InvalidTicketException;
import com.runnity.websocket.manager.SessionManager;
import com.runnity.websocket.service.ChallengeKafkaProducer;
import com.runnity.websocket.service.ChallengeParticipationService;
import com.runnity.websocket.service.RedisPubSubService;
import com.runnity.websocket.service.TimeoutCheckService;
import com.runnity.websocket.service.TicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;

/**
 * 챌린지 WebSocket 핸들러
 * 
 * 입장, 메시지 처리, 퇴장 로직을 담당합니다.
 */
@Slf4j
@Component
public class ChallengeWebSocketHandler extends TextWebSocketHandler {

    private final TicketService ticketService;
    private final SessionManager sessionManager;
    private final RedisPubSubService redisPubSubService;
    private final ChallengeKafkaProducer kafkaProducer;
    private final TimeoutCheckService timeoutCheckService;
    private final ChallengeParticipationService participationService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public ChallengeWebSocketHandler(
            TicketService ticketService,
            SessionManager sessionManager,
            RedisPubSubService redisPubSubService,
            ChallengeKafkaProducer kafkaProducer,
            TimeoutCheckService timeoutCheckService,
            ChallengeParticipationService participationService,
            ObjectMapper objectMapper,
            @Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.ticketService = ticketService;
        this.sessionManager = sessionManager;
        this.redisPubSubService = redisPubSubService;
        this.kafkaProducer = kafkaProducer;
        this.timeoutCheckService = timeoutCheckService;
        this.participationService = participationService;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * WebSocket 연결 수립 (입장 로직)
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            // 1. 티켓 추출
            String ticket = (String) session.getAttributes().get("ticket");
            if (ticket == null || ticket.isBlank()) {
                log.warn("티켓 없음: sessionId={}", session.getId());
                sendErrorAndClose(session, "UNAUTHORIZED", "티켓이 필요합니다.");
                return;
            }

            // 2. 티켓 검증 및 소모
            TicketService.TicketData ticketData = ticketService.verifyAndConsumeTicket(ticket);
            Long challengeId = ticketData.challengeId();
            Long userId = ticketData.userId();

            // 3. 세션 attributes에 정보 저장
            session.getAttributes().put("challengeId", challengeId);
            session.getAttributes().put("userId", userId);

            // 티켓에서 nickname, profileImage 추출
            String nickname = ticketData.nickname();
            String profileImage = ticketData.profileImage();

            // null 체크 (방어 코드)
            if (nickname == null || nickname.isBlank()) {
                nickname = "User_" + userId;
                log.warn("티켓에 nickname 없음. 기본값 사용: userId={}, nickname={}", userId, nickname);
            }

            session.getAttributes().put("nickname", nickname);
            session.getAttributes().put("profileImage", profileImage);

            // 4. 재접속 여부 확인 (이전 참가자 정보가 있는지)
            SessionManager.ParticipantInfo previousInfo = sessionManager.getParticipantInfo(challengeId, userId);
            boolean isReconnect = previousInfo != null;
            Double previousDistance = previousInfo != null ? previousInfo.distance() : null;
            Integer previousPace = previousInfo != null ? previousInfo.pace() : null;

            // 5. 다른 참가자 목록 조회 (본인 제외)
            List<ConnectedMessage.Participant> participants = sessionManager.getParticipants(challengeId, userId);

            // 6. 세션 등록 (메모리 + Redis) - 재접속인 경우 이전 정보 유지
            sessionManager.registerSession(challengeId, userId, nickname, profileImage, session, 
                    previousDistance, previousPace);

            // 6-1. 타임아웃 체크를 위한 초기 RECORD 시간 설정
            timeoutCheckService.updateLastRecordTime(challengeId, userId);

            // 7. CONNECTED 메시지 전송 (본인에게)
            // 재접속인 경우 이전 distance, pace 사용, 아니면 0.0 / 0
            Double initialDistance = isReconnect && previousInfo != null ? previousInfo.distance() : 0.0;
            Integer initialPace = isReconnect && previousInfo != null ? previousInfo.pace() : 0;
            
            log.debug("Participant 생성 전: userId={}, nickname={}, profileImage={}, isReconnect={}", 
                    userId, nickname, profileImage, isReconnect);
            ConnectedMessage.Participant me = new ConnectedMessage.Participant(
                    userId, nickname, profileImage, initialDistance, initialPace);
            log.debug("Participant 생성 후: me={}", me);
            ConnectedMessage connectedMessage = new ConnectedMessage(challengeId, userId, me, participants);
            String connectedJson = objectMapper.writeValueAsString(connectedMessage);
            session.sendMessage(new TextMessage(connectedJson));

            // 8. Redis Pub/Sub으로 입장 이벤트 발행 (다른 WebSocket 서버에 알림)
            redisPubSubService.publishUserEntered(challengeId, userId, nickname, profileImage);

            // 9. Kafka 이벤트 발행
            if (isReconnect) {
                // 재접속인 경우: RUNNING 이벤트 발행 (이전 distance, pace 사용)
                Integer ranking = sessionManager.calculateRanking(challengeId, userId);
                kafkaProducer.publishRunningEvent(challengeId, userId, nickname, profileImage, 
                        initialDistance, initialPace, ranking);
                log.info("재접속 처리: challengeId={}, userId={}, distance={}, pace={}", 
                        challengeId, userId, initialDistance, initialPace);
            } else {
                // 첫 입장인 경우: START 이벤트 발행
                kafkaProducer.publishStartEvent(challengeId, userId, nickname, profileImage);
            }

            log.info("챌린지 입장 성공: challengeId={}, userId={}, nickname={}, sessionId={}", 
                    challengeId, userId, nickname, session.getId());

        } catch (InvalidTicketException e) {
            log.warn("티켓 검증 실패: sessionId={}, error={}", session.getId(), e.getMessage());
            sendErrorAndClose(session, "INVALID_TICKET", e.getMessage());
        } catch (Exception e) {
            log.error("입장 처리 실패: sessionId={}", session.getId(), e);
            sendErrorAndClose(session, "INTERNAL_ERROR", "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 메시지 수신 처리
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            log.debug("메시지 수신: sessionId={}, payload={}", session.getId(), payload);

            // 세션 정보 확인
            Long challengeId = (Long) session.getAttributes().get("challengeId");
            Long userId = (Long) session.getAttributes().get("userId");
            
            if (challengeId == null || userId == null) {
                log.warn("세션 정보 없음: sessionId={}", session.getId());
                sendError(session, "INVALID_SESSION", "세션 정보가 없습니다.");
                return;
            }

            // JSON 파싱하여 타입 확인
            JsonNode jsonNode = objectMapper.readTree(payload);
            String type = jsonNode.get("type").asText();

            // 메시지 타입별 처리
            switch (type) {
                case "RECORD" -> handleRecord(session, challengeId, userId, payload);
                case "QUIT" -> handleQuit(session, challengeId, userId);
                case "KICKED" -> handleKick(session, challengeId, userId, payload);
                case "PING" -> handlePing(session);
                case "PONG" -> handlePong(session);
                default -> {
                    log.warn("알 수 없는 메시지 타입: type={}, sessionId={}", type, session.getId());
                    sendError(session, "INVALID_MESSAGE_TYPE", "알 수 없는 메시지 타입입니다: " + type);
                }
            }

        } catch (Exception e) {
            log.error("메시지 처리 실패: sessionId={}", session.getId(), e);
            sendError(session, "MESSAGE_PROCESSING_ERROR", "메시지 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * RECORD 메시지 처리
     */
    private void handleRecord(WebSocketSession session, Long challengeId, Long userId, String payload) {
        try {
            // JSON 역직렬화
            RecordMessage recordMessage = objectMapper.readValue(payload, RecordMessage.class);
            Double distance = recordMessage.distance();
            Integer pace = recordMessage.pace();

            // 세션에서 nickname, profileImage 가져오기
            String nickname = (String) session.getAttributes().get("nickname");
            String profileImage = (String) session.getAttributes().get("profileImage");

            // 참가자 정보 업데이트 (Redis)
            sessionManager.updateParticipantInfo(challengeId, userId, distance, pace);

            // 순위 계산
            Integer ranking = sessionManager.calculateRanking(challengeId, userId);

            // Redis Pub/Sub으로 참가자 업데이트 이벤트 발행 (다른 서버에 알림)
            redisPubSubService.publishParticipantUpdate(challengeId, userId, distance, pace);

            // Kafka로 RUNNING 이벤트 발행
            kafkaProducer.publishRunningEvent(challengeId, userId, nickname, profileImage, 
                    distance, pace, ranking);

            // 마지막 RECORD 시간 업데이트 (타임아웃 체크용)
            timeoutCheckService.updateLastRecordTime(challengeId, userId);

            log.debug("RECORD 처리 완료: challengeId={}, userId={}, distance={}, pace={}, ranking={}", 
                    challengeId, userId, distance, pace, ranking);

            // 완주 체크 (목표 거리 달성 시)
            checkAndHandleFinish(challengeId, userId, nickname, profileImage, distance, pace, ranking);

        } catch (Exception e) {
            log.error("RECORD 처리 실패: challengeId={}, userId={}", challengeId, userId, e);
            sendError(session, "RECORD_PROCESSING_ERROR", "러닝 기록 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * QUIT 메시지 처리
     */
    private void handleQuit(WebSocketSession session, Long challengeId, Long userId) {
        try {
            log.info("자발적 포기: challengeId={}, userId={}", challengeId, userId);

            // 참가자 정보 조회 (현재 distance, pace)
            SessionManager.ParticipantInfo info = sessionManager.getParticipantInfo(challengeId, userId);
            Double distance = info != null ? info.distance() : 0.0;
            Integer pace = info != null ? info.pace() : 0;
            Integer ranking = sessionManager.calculateRanking(challengeId, userId);

            String nickname = (String) session.getAttributes().get("nickname");
            String profileImage = (String) session.getAttributes().get("profileImage");

            // 퇴장 처리
            handleLeave(challengeId, userId, nickname, profileImage, distance, pace, ranking, 
                    LeaveReason.QUIT.getValue());

            // 연결 종료
            session.close(CloseStatus.NORMAL);

        } catch (Exception e) {
            log.error("QUIT 처리 실패: challengeId={}, userId={}", challengeId, userId, e);
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (Exception ex) {
                log.error("연결 종료 실패: sessionId={}", session.getId(), ex);
            }
        }
    }

    /**
     * PING 메시지 처리
     */
    private void handlePing(WebSocketSession session) {
        try {
            Long challengeId = (Long) session.getAttributes().get("challengeId");
            Long userId = (Long) session.getAttributes().get("userId");
            if (challengeId != null && userId != null) {
                timeoutCheckService.updateLastRecordTime(challengeId, userId);
            }

            // PONG 응답
            PongMessage pong = new PongMessage();
            String pongJson = objectMapper.writeValueAsString(pong);
            session.sendMessage(new TextMessage(pongJson));
            
            log.debug("PING 응답: sessionId={}", session.getId());
        } catch (Exception e) {
            log.error("PING 처리 실패: sessionId={}", session.getId(), e);
        }
    }

    /**
     * PONG 메시지 처리
     */
    private void handlePong(WebSocketSession session) {
        try {
            Long challengeId = (Long) session.getAttributes().get("challengeId");
            Long userId = (Long) session.getAttributes().get("userId");
            if (challengeId != null && userId != null) {
                timeoutCheckService.updateLastRecordTime(challengeId, userId);
            }
            log.debug("PONG 수신: sessionId={}", session.getId());
        } catch (Exception e) {
            log.error("PONG 처리 실패: sessionId={}", session.getId(), e);
        }
    }

    /**
     * KICKED 메시지 처리 (강제 퇴장)
     * 이상 사용자가 자신의 웹소켓 연결에서 강제 퇴장 메시지를 받을 때 처리
     */
    private void handleKick(WebSocketSession session, Long challengeId, Long userId, String payload) {
        try {
            // JSON 역직렬화 (검증용)
            objectMapper.readValue(payload, KickedMessage.class);
            log.info("강제 퇴장 요청 수신: challengeId={}, userId={}", challengeId, userId);

            // 참가자 정보 조회
            SessionManager.ParticipantInfo info = sessionManager.getParticipantInfo(challengeId, userId);
            Double distance = info != null ? info.distance() : 0.0;
            Integer pace = info != null ? info.pace() : 0;
            Integer ranking = sessionManager.calculateRanking(challengeId, userId);
            String nickname = (String) session.getAttributes().get("nickname");
            String profileImage = (String) session.getAttributes().get("profileImage");

            // 퇴장 처리 (Redis 정리, Pub/Sub 발행, Kafka 이벤트)
            handleLeave(challengeId, userId, nickname, profileImage, distance, pace, ranking, 
                    LeaveReason.KICKED.getValue());

            // 연결 종료
            session.close(CloseStatus.POLICY_VIOLATION);

            log.info("강제 퇴장 처리 완료: challengeId={}, userId={}", challengeId, userId);

        } catch (Exception e) {
            log.error("KICKED 처리 실패: challengeId={}, userId={}", challengeId, userId, e);
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (Exception ex) {
                log.error("세션 종료 실패: sessionId={}", session.getId(), ex);
            }
        }
    }

    /**
     * 완주 체크 및 처리
     */
    private void checkAndHandleFinish(Long challengeId, Long userId, String nickname, String profileImage,
                                      Double distance, Integer pace, Integer ranking) {
        try {
            // 목표 거리 조회 (Redis 또는 외부 API)
            Double targetDistance = getTargetDistance(challengeId);
            
            if (targetDistance != null && distance >= targetDistance) {
                log.info("완주 달성: challengeId={}, userId={}, distance={}, targetDistance={}", 
                        challengeId, userId, distance, targetDistance);

                // 참가자 상태 DB 업데이트 (COMPLETE)
                participationService.updateParticipationStatus(challengeId, userId, LeaveReason.FINISH.getValue());

                // 타임아웃 체크 서비스에서 마지막 RECORD 시간 제거
                timeoutCheckService.removeLastRecordTime(challengeId, userId);

                // Kafka로 FINISH 이벤트 발행
                kafkaProducer.publishFinishEvent(challengeId, userId, nickname, profileImage, 
                        distance, pace, ranking);

                // Redis Pub/Sub으로 퇴장 이벤트 발행 (reason: FINISH)
                redisPubSubService.publishUserLeft(challengeId, userId, LeaveReason.FINISH.getValue());

                // 세션 제거
                sessionManager.removeSession(challengeId, userId);
            }
        } catch (Exception e) {
            log.error("완주 체크 실패: challengeId={}, userId={}", challengeId, userId, e);
        }
    }

    /**
     * 목표 거리 조회 (Redis 챌린지 meta에서)
     */
    private Double getTargetDistance(Long challengeId) {
        try {
            // Redis에서 챌린지 meta 정보 조회
            // 키 형식: challenge:{challengeId}:meta
            String metaKey = "challenge:" + challengeId + ":meta";
            Object distanceObj = redisTemplate.opsForHash().get(metaKey, "distance");
            
            if (distanceObj == null) {
                log.debug("목표 거리 정보 없음: challengeId={}", challengeId);
                return null;
            }
            
            // String 또는 Number로 변환
            Double targetDistance;
            if (distanceObj instanceof Number) {
                targetDistance = ((Number) distanceObj).doubleValue();
            } else {
                targetDistance = Double.parseDouble(distanceObj.toString());
            }
            
            log.debug("목표 거리 조회: challengeId={}, targetDistance={}", challengeId, targetDistance);
            return targetDistance;
        } catch (Exception e) {
            log.error("목표 거리 조회 실패: challengeId={}", challengeId, e);
            return null;
        }
    }

    /**
     * WebSocket 연결 종료 (퇴장 로직)
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            Long challengeId = (Long) session.getAttributes().get("challengeId");
            Long userId = (Long) session.getAttributes().get("userId");

            if (challengeId == null || userId == null) {
                log.warn("세션 정보 없음: sessionId={}", session.getId());
                return;
            }

            // 참가자 정보 조회 (현재 distance, pace)
            SessionManager.ParticipantInfo info = sessionManager.getParticipantInfo(challengeId, userId);
            Double distance = info != null ? info.distance() : 0.0;
            Integer pace = info != null ? info.pace() : 0;
            Integer ranking = sessionManager.calculateRanking(challengeId, userId);

            String nickname = (String) session.getAttributes().get("nickname");
            String profileImage = (String) session.getAttributes().get("profileImage");

            // 퇴장 사유 결정
            String reason = determineLeaveReason(status);

            // 퇴장 처리
            handleLeave(challengeId, userId, nickname, profileImage, distance, pace, ranking, reason);

            log.info("❌ 챌린지 퇴장: challengeId={}, userId={}, sessionId={}, status={}, reason={}", 
                    challengeId, userId, session.getId(), status, reason);

        } catch (Exception e) {
            log.error("퇴장 처리 실패: sessionId={}", session.getId(), e);
        }
    }

    /**
     * 퇴장 처리 공통 로직
     */
    private void handleLeave(Long challengeId, Long userId, String nickname, String profileImage,
                             Double distance, Integer pace, Integer ranking, String reason) {
        try {
            // 1. 참가자 상태 DB 업데이트 (웹소켓 서버에서 직접 처리)
            participationService.updateParticipationStatus(challengeId, userId, reason);

            // 2. 타임아웃 체크 서비스에서 마지막 RECORD 시간 제거
            timeoutCheckService.removeLastRecordTime(challengeId, userId);

            // 3. 세션 제거 (메모리 + Redis)
            sessionManager.removeSession(challengeId, userId);

            // 4. Redis Pub/Sub으로 퇴장 이벤트 발행 (다른 서버에 알림)
            redisPubSubService.publishUserLeft(challengeId, userId, reason);

            // 5. Kafka로 LEAVE 이벤트 발행
            kafkaProducer.publishLeaveEvent(challengeId, userId, nickname, profileImage, 
                    distance, pace, ranking, reason);

        } catch (Exception e) {
            log.error("퇴장 처리 실패: challengeId={}, userId={}, reason={}", challengeId, userId, reason, e);
        }
    }

    /**
     * CloseStatus에 따라 퇴장 사유 결정
     */
    private String determineLeaveReason(CloseStatus status) {
        if (status == null) {
            return LeaveReason.DISCONNECTED.getValue();
        }

        // CloseStatus 코드에 따라 사유 결정
        if (status.equals(CloseStatus.NORMAL)) {
            // 정상 종료는 QUIT로 간주 (이미 QUIT 메시지로 처리됨)
            return LeaveReason.QUIT.getValue();
        } else if (status.equals(CloseStatus.SERVER_ERROR) || status.equals(CloseStatus.POLICY_VIOLATION)) {
            return LeaveReason.ERROR.getValue();
        } else {
            // 기타는 연결 끊김으로 간주
            return LeaveReason.DISCONNECTED.getValue();
        }
    }

    /**
     * 에러 메시지 전송 (연결 종료 없이)
     */
    private void sendError(WebSocketSession session, String errorCode, String errorMessage) {
        try {
            ErrorMessage error = new ErrorMessage(errorCode, errorMessage);
            String errorJson = objectMapper.writeValueAsString(error);
            session.sendMessage(new TextMessage(errorJson));
        } catch (Exception e) {
            log.error("에러 메시지 전송 실패: sessionId={}", session.getId(), e);
        }
    }

    /**
     * 에러 메시지 전송 및 연결 종료
     */
    private void sendErrorAndClose(WebSocketSession session, String errorCode, String errorMessage) {
        try {
            ErrorMessage error = new ErrorMessage(errorCode, errorMessage);
            String errorJson = objectMapper.writeValueAsString(error);
            session.sendMessage(new TextMessage(errorJson));
            session.close(CloseStatus.POLICY_VIOLATION);
        } catch (Exception e) {
            log.error("에러 메시지 전송 실패: sessionId={}", session.getId(), e);
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (Exception ex) {
                log.error("연결 종료 실패: sessionId={}", session.getId(), ex);
            }
        }
    }
}
