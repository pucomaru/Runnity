package com.runnity.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.websocket.dto.websocket.server.ConnectedMessage;
import com.runnity.websocket.dto.websocket.server.ErrorMessage;
import com.runnity.websocket.exception.InvalidTicketException;
import com.runnity.websocket.manager.SessionManager;
import com.runnity.websocket.service.ChallengeKafkaProducer;
import com.runnity.websocket.service.RedisPubSubService;
import com.runnity.websocket.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class ChallengeWebSocketHandler extends TextWebSocketHandler {

    private final TicketService ticketService;
    private final SessionManager sessionManager;
    private final RedisPubSubService redisPubSubService;
    private final ChallengeKafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper;

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

            // 4. 다른 참가자 목록 조회 (본인 제외)
            List<ConnectedMessage.Participant> participants = sessionManager.getParticipants(challengeId, userId);

            // 5. 세션 등록 (메모리 + Redis)
            sessionManager.registerSession(challengeId, userId, nickname, profileImage, session);

            // 6. CONNECTED 메시지 전송 (본인에게)
            log.debug("Participant 생성 전: userId={}, nickname={}, profileImage={}", userId, nickname, profileImage);
            ConnectedMessage.Participant me = new ConnectedMessage.Participant(userId, nickname, profileImage, 0.0, 0.0);
            log.debug("Participant 생성 후: me={}", me);
            ConnectedMessage connectedMessage = new ConnectedMessage(challengeId, userId, me, participants);
            String connectedJson = objectMapper.writeValueAsString(connectedMessage);
            session.sendMessage(new TextMessage(connectedJson));

            // 7. Redis Pub/Sub으로 입장 이벤트 발행 (다른 WebSocket 서버에 알림)
            redisPubSubService.publishUserEntered(challengeId, userId, nickname, profileImage);

            // 8. Kafka로 START 이벤트 발행 (브로드캐스트용)
            kafkaProducer.publishStartEvent(challengeId, userId, nickname, profileImage);

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

            // TODO: 메시지 타입별 처리 (RECORD, QUIT, PING 등)
            // 일단 에코로 응답
            session.sendMessage(new TextMessage("Echo: " + payload));

        } catch (Exception e) {
            log.error("메시지 처리 실패: sessionId={}", session.getId(), e);
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

            // 세션 제거 (메모리 + Redis)
            sessionManager.removeSession(challengeId, userId);

            // TODO: Redis Pub/Sub으로 퇴장 이벤트 발행
            // TODO: Kafka로 LEAVE 이벤트 발행

            log.info("❌ 챌린지 퇴장: challengeId={}, userId={}, sessionId={}, status={}", 
                    challengeId, userId, session.getId(), status);

        } catch (Exception e) {
            log.error("퇴장 처리 실패: sessionId={}", session.getId(), e);
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
