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

    @Value("${google.client-id}")
    private String GOOGLE_CLIENT_ID;

    @Value("${kakao.client-id}")
    private String KAKAO_CLIENT_ID;

    @Value("${kakao.iss}")
    private String KAKAO_ISS;

    @Value("${kakao.jwks-uri}")
    private String KAKAO_JWKS_URI;

    @Transactional
    public LoginResponseDto googleLogin(String idToken) {
        System.out.println("✅ 백엔드 로그인 시작");
        System.out.println("idToken : "+idToken);
        System.out.println("받은 ID Token: " + idToken.substring(0, Math.min(50, idToken.length())) + "...");
        System.out.println("설정된 Google Client ID: " + GOOGLE_CLIENT_ID);

        try {
            System.out.println("🔐 ID Token 검증 시작...");
            long startTime = System.currentTimeMillis();

            // ID Token 검증 (이제 타임아웃 설정됨)
            GoogleIdToken token = googleIdTokenVerifier.verify(idToken);

            long endTime = System.currentTimeMillis();
            System.out.println("✅ ID Token 검증 성공! (소요 시간: " + (endTime - startTime) + "ms)");

            if (token == null) {
                System.err.println("❌ Token이 null입니다!");
                throw new IllegalArgumentException("Invalid ID Token");
            }

            GoogleIdToken.Payload payload = token.getPayload();
            String email = payload.getEmail();
            String googleId = payload.getSubject();

            System.out.println("✅ 이메일: " + email);
            System.out.println("✅ Google ID: " + googleId);

            return processSocialLogin(googleId, email, "GOOGLE");

        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            System.err.println("❌ Google API 에러!");
            System.err.println("HTTP 상태 코드: " + e.getStatusCode());
            System.err.println("예외 메시지: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Google ID Token verification failed", e);
        } catch (GeneralSecurityException e) {
            System.err.println("❌ 보안 예외 발생!");
            System.err.println("예외 타입: " + e.getClass().getName());
            System.err.println("예외 메시지: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("ID Token verification failed", e);
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("❌ 타임아웃 예외!");
            System.err.println("예외 메시지: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Google API timeout", e);
        } catch (Exception e) {
            System.err.println("❌ 기타 예외 발생!");
            System.err.println("예외 타입: " + e.getClass().getName());
            System.err.println("예외 메시지: " + e.getMessage());
            e.printStackTrace();
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
            System.err.println("❌ Kakao 로그인 실패: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid Kakao ID Token", e);
        }
    }

    // Kakao 검증: kid 매칭 우선 → 실패 시 JWKS keys 전체 순회 fallback
    private DecodedJWT verifyKakaoWithFallback(String idToken, String iss, String aud, String jwksUri) throws Exception {
        DecodedJWT headerJwt = com.auth0.jwt.JWT.decode(idToken);
        String kid = headerJwt.getKeyId();

        // 1) kid가 있으면 우선 시도
        if (kid != null) {
            JwkProvider provider = new JwkProviderBuilder(new URL(jwksUri))
                    .cached(10, 24, TimeUnit.HOURS)
                    .build();
            try {
                Jwk jwk = provider.get(kid);
                Algorithm alg = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
                // ✅ 임시: audience 검증 제거
                return com.auth0.jwt.JWT.require(alg)
                        .withIssuer(iss)
                        // .withAudience(aud)  // ← 주석 처리
                        .build()
                        .verify(idToken);
            } catch (Exception e) {
                System.err.println("❌ Kid 매칭 실패: " + e.getMessage());
            }
        }

        // 2) fallback
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder(URI.create(jwksUri)).GET().build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IllegalArgumentException("JWKS fetch failed: " + res.statusCode());
        }

        JSONObject jwks = new JSONObject(res.body());
        JSONArray keys = jwks.getJSONArray("keys");

        for (int i = 0; i < keys.length(); i++) {
            JSONObject k = keys.getJSONObject(i);
            try {
                String n = k.getString("n");
                String e = k.getString("e");
                RSAPublicKey pub = buildRsaPublicKey(n, e);
                Algorithm a = Algorithm.RSA256(pub, null);
                // ✅ 임시: audience 검증 제거
                DecodedJWT ok = com.auth0.jwt.JWT.require(a)
                        .withIssuer(iss)
                        // .withAudience(aud)  // ← 주석 처리
                        .build()
                        .verify(idToken);
                return ok;
            } catch (Exception ignore) { }
        }
        throw new IllegalArgumentException("No matching JWK could verify token");
    }

    private RSAPublicKey buildRsaPublicKey(String nB64Url, String eB64Url) throws Exception {
        byte[] nBytes = Base64.getUrlDecoder().decode(nB64Url);
        byte[] eBytes = Base64.getUrlDecoder().decode(eB64Url);
        BigInteger n = new BigInteger(1, nBytes);
        BigInteger e = new BigInteger(1, eBytes);
        RSAPublicKeySpec spec = new RSAPublicKeySpec(n, e);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private LoginResponseDto processSocialLogin(String socialUid, String email, String socialType) {
        Optional<Member> existingMember = memberRepository.findBySocialUid(socialUid);
        Member member;
        boolean isNewUser;

        if (existingMember.isPresent()) {
            member = existingMember.get();
            isNewUser = false;
        } else {
            Member newMember = Member.builder()
                    .email(email)
                    .socialUid(socialUid)
                    .socialType(socialType)
                    .build();
            member = memberRepository.save(newMember);
            isNewUser = true;
        }

        String accessToken = jwtTokenProvider.createAccessToken(member);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        return new LoginResponseDto(accessToken, refreshToken, isNewUser);
    }

    public TokenResponse refreshAccessToken(String refreshToken) {
        // Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // Refresh Token에서 이메일 추출
        Claims claims = jwtTokenProvider.parseClaims(refreshToken);
        String email = claims.getSubject();

        // 이메일로 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 새로운 Access Token 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(member);

        // 새로운 Refresh Token도 함께 발급 (Refresh Token Rotation 전략)
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
