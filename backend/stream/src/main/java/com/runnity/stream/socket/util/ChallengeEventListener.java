package com.runnity.stream.socket.util;

import com.runnity.stream.socket.BroadcastStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Component
@Slf4j
public class ChallengeEventListener implements MessageListener {

    private final BroadcastStateService broadcastStateService;

    @Override
    public void onMessage(Message message, byte[] pattern) {

        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);

        Long challengeId = extractChallengeId(channel);
        if (challengeId == null) return;

        if (channel.endsWith(":ready")) {
            broadcastStateService.handleReady(challengeId);
        } else if (channel.endsWith(":running")) {
            broadcastStateService.handleRunning(challengeId);
        } else if (channel.endsWith(":done")) {
            broadcastStateService.handleDone(challengeId);
        }
    }

    private Long extractChallengeId(String channel) {
        String[] parts = channel.split(":");
        if (parts.length >= 2) {
            return Long.parseLong(parts[1]);
        }
        return null;
    }
}