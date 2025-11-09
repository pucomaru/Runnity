package com.runnity.broadcast.client;

import com.runnity.broadcast.config.FeignConfig;
import com.runnity.broadcast.dto.BroadcastDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * stream 서버 연동용 FeignClient
 * - 내부적으로 stream 서버의 /api/broadcast/active 호출
 */
@FeignClient(
    name = "broadcastClient",
    url = "${stream.server.url}",
    configuration = FeignConfig.class
)
public interface BroadcastClient {
    @GetMapping("/api/broadcast/active")
    List<BroadcastDto> getActiveBroadcasts();
}
