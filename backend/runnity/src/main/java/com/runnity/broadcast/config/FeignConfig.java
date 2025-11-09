package com.runnity.broadcast.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

public class FeignConfig {
    
    @Bean
    public Request.Options options() {
        return new Request.Options(
            5000,      // connect timeout (5초)
            3000       // read timeout (3초)
        );
    }
    
    @Bean
    public Retryer retryer() {
        // 최대 3번 재시도, 첫 재시도는 1초 후, 두 번째는 2초 후
        return new Retryer.Default(1000, 2000, 3);
    }
    
    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            HttpStatus status = HttpStatus.valueOf(response.status());
            
            if (status.is5xxServerError()) {
                // 서버 에러인 경우 재시도
                return new RuntimeException("Stream server error: " + status.value());
            }
            
            if (status == HttpStatus.NOT_FOUND) {
                return new RuntimeException("Broadcast not found: " + response.request().url());
            }
            
            return new RuntimeException("Failed to call broadcast service: " + response.reason());
        };
    }
    
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;  // 로깅 레벨 설정 (NONE, BASIC, HEADERS, FULL)
    }
}
