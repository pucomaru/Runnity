package com.runnity.stream.notification.service;

import com.google.firebase.messaging.*;
import com.runnity.stream.challenge.domain.ParticipationStatus;
import com.runnity.stream.challenge.repository.ChallengeParticipationRepository;
import com.runnity.stream.scheduler.domain.NotificationOutbox;
import com.runnity.stream.scheduler.domain.NotificationTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ChallengeParticipationRepository challengeParticipationRepository;

    public void sendNotifications(List<String> tokens, String title, String body) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            MulticastMessage multicastMessage = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(notification)
                    .build();

//            Message message = Message.builder()
//                    .setToken(token)
//                    .setNotification(notification)
//                    .build();

            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessage);
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                List<String> failedTokens = new ArrayList<>();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        failedTokens.add(tokens.get(i));
                    }
                }

                log.warn("[FCM] 일부 토큰 전송 실패 ({}건): {}", failedTokens.size(), failedTokens);
            }
        } catch (FirebaseMessagingException e) {
            log.error("[FCM API 장애] FCM 전송 자체 실패: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("[FCM 전송 중 예외 발생] {}", e.getMessage(), e);
        }
    }

    public void sendChallengeNotification(Long challengeId, NotificationOutbox.NotificationType type){
        List<ParticipationStatus> participationStatus = new ArrayList<>();
        NotificationTemplate templete = null;
        if(type == NotificationOutbox.NotificationType.CHALLENGE_READY){
            participationStatus.addAll(ParticipationStatus.CHALLENGE_START_STATUS);
            templete = NotificationTemplate.CHALLENGE_READY;
        }else if(type == NotificationOutbox.NotificationType.CHALLENGE_DONE){
            participationStatus.addAll(ParticipationStatus.CHALLENGE_END_STATUS);
            templete = NotificationTemplate.CHALLENGE_DONE;
        }else {
            log.warn("[알림 스킵] 알 수 없는 type={}", type);
            return;
        }

        List<String> tokens = challengeParticipationRepository.findTokensByChallengeIdAndStatuses(challengeId, participationStatus);
        if (tokens == null || tokens.isEmpty()) {
            log.info("[FCM] 전송 대상 없음");
            return;
        }
        sendNotifications(tokens, templete.getTitle(), templete.getBody());
    }
}
