package com.runnity.member.service;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.runnity.member.domain.Member;
import com.runnity.member.dto.LoginResponseDto;
import com.runnity.member.dto.TokenResponse;
import com.runnity.member.repository.MemberRepository;
import com.runnity.member.util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${GOOGLE_CLIENT_ID}") private String GOOGLE_CLIENT_ID;
    @Value("${KAKAO_CLIENT_ID}")  private String KAKAO_CLIENT_ID;
    @Value("${KAKAO_ISS}")        private String KAKAO_ISS;
    @Value("${KAKAO_JWKS_URI}")   private String KAKAO_JWKS_URI;

    @Transactional
    public LoginResponseDto googleLogin(String idToken) {
        try {
            GoogleIdToken token = googleIdTokenVerifier.verify(idToken);
            if (token == null) throw new IllegalArgumentException("Invalid Google ID Token");

            GoogleIdToken.Payload payload = token.getPayload();
            String email = payload.getEmail();
            String googleId = payload.getSubject();

            return processSocialLogin(googleId, email, "GOOGLE");
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            throw new IllegalArgumentException("Google ID Token verification failed", e);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("ID Token verification failed", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Login failed", e);
        }
    }

    @Transactional
    public LoginResponseDto kakaoLogin(String idToken) {
        try {
            DecodedJWT decoded = verifyKakaoWithFallback(idToken, KAKAO_ISS, KAKAO_CLIENT_ID, KAKAO_JWKS_URI);
            String socialUid = decoded.getSubject();
            return processSocialLogin(socialUid, "", "KAKAO");
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Kakao ID Token", e);
        }
    }

    private DecodedJWT verifyKakaoWithFallback(String idToken, String iss, String aud, String jwksUri) throws Exception {
        DecodedJWT headerJwt = com.auth0.jwt.JWT.decode(idToken);
        String kid = headerJwt.getKeyId();

        if (kid != null) {
            JwkProvider provider = new JwkProviderBuilder(new URL(jwksUri))
                    .cached(10, 24, TimeUnit.HOURS)
                    .build();
            try {
                Jwk jwk = provider.get(kid);
                Algorithm alg = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
                return com.auth0.jwt.JWT.require(alg)
                        .withIssuer(iss)
                        .build()
                        .verify(idToken);
            } catch (Exception ignore) { }
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> res = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(jwksUri)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        if (res.statusCode() != 200) throw new IllegalArgumentException("JWKS fetch failed: " + res.statusCode());

        JSONArray keys = new JSONObject(res.body()).getJSONArray("keys");
        for (int i = 0; i < keys.length(); i++) {
            try {
                JSONObject k = keys.getJSONObject(i);
                RSAPublicKey pub = buildRsaPublicKey(k.getString("n"), k.getString("e"));
                Algorithm a = Algorithm.RSA256(pub, null);
                return com.auth0.jwt.JWT.require(a)
                        .withIssuer(iss)
                        .build()
                        .verify(idToken);
            } catch (Exception ignore) { }
        }
        throw new IllegalArgumentException("No matching JWK could verify token");
    }

    private RSAPublicKey buildRsaPublicKey(String nB64Url, String eB64Url) throws Exception {
        BigInteger n = new BigInteger(1, Base64.getUrlDecoder().decode(nB64Url));
        BigInteger e = new BigInteger(1, Base64.getUrlDecoder().decode(eB64Url));
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(n, e));
    }

    private LoginResponseDto processSocialLogin(String socialUid, String email, String socialType) {
        Optional<Member> existing = memberRepository.findBySocialUid(socialUid);
        Member member;
        boolean isNew;

        if (existing.isPresent()) {
            member = existing.get();
            isNew = false;
        } else {
            member = memberRepository.save(
                    Member.builder()
                            .email(email)
                            .socialUid(socialUid)
                            .socialType(socialType)
                            .build()
            );
            isNew = true;
        }

        String accessToken = jwtTokenProvider.createAccessToken(member);
        String refreshToken = jwtTokenProvider.createRefreshToken(
                (email == null || email.isBlank()) ? (socialUid + "@kakao.local") : email
        );
        return new LoginResponseDto(accessToken, refreshToken, isNew);
    }

    public TokenResponse refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }
        Claims claims = jwtTokenProvider.parseClaims(refreshToken);
        String email = claims.getSubject();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        String newAccess = jwtTokenProvider.createAccessToken(member);
        String newRefresh = jwtTokenProvider.createRefreshToken(member.getEmail());
        return new TokenResponse(newAccess, newRefresh);
    }
}