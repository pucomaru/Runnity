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

@Configuration
public class GoogleOAuthConfig {

    @Value("${google.client-id}")
    private String GOOGLE_CLIENT_ID;

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        HttpRequestInitializer httpRequestInitializer = new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) throws IOException {
                httpRequest.setConnectTimeout(5 * 1000);
                httpRequest.setReadTimeout(5 * 1000);
            }
        };

        return new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory()
        )
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                .build();
    }
}
