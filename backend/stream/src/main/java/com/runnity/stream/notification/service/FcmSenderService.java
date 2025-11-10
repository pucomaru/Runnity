package com.runnity.stream.notification.service;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FcmSenderService {
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

                System.out.println("List of tokens that caused failures: " + failedTokens);
            }
        } catch (Exception e) {
            log.error("FCM 메시지 전송 실패", e);
        }
    }
}
