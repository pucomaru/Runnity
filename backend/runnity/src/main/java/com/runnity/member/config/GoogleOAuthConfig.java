package com.runnity.member.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Collections;

/**
 * Google ID Token 검증기 Bean 구성.
 * - Swagger에서 /api/v1/auth/login/google 호출 시, 이 Bean으로 ID Token의 유효성(aud/서명 등)을 검증한다.
 * - 네트워크 타임아웃(연결/읽기) 5초 설정.
 */
@Configuration
public class GoogleOAuthConfig {

    @Value("${GOOGLE_CLIENT_ID}")
    private String GOOGLE_CLIENT_ID;

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        HttpRequestInitializer timeoutInitializer = new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) throws IOException {
                httpRequest.setConnectTimeout(5_000);
                httpRequest.setReadTimeout(5_000);
            }
        };

        return new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                .build();
    }
}
