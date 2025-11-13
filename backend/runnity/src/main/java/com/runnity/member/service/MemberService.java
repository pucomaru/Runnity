package com.runnity.member.service;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.runnity.global.service.TokenBlacklistService;
import com.runnity.global.storage.FileStorage;
import com.runnity.member.domain.Member;
import com.runnity.member.dto.*;
import com.runnity.member.repository.MemberRepository;
import com.runnity.member.util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final FileStorage fileStorage;

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

        boolean needAdditionalInfo = member.getNickname() == null || member.getNickname().isBlank();

        return new LoginResponseDto(accessToken, refreshToken, isNew, needAdditionalInfo);
    }

    @Transactional
    public void addAdditionalInfo(
            Long memberId,
            AddInfoRequestDto request,
            MultipartFile profileImage
    ) {
        try {
            // 회원 조회
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("MEMBER_NOT_FOUND"));

            if (request == null || request.getNickname() == null || request.getNickname().isBlank()) {
                throw new IllegalArgumentException("NICKNAME_REQUIRED");
            }

            String nick = normalize(request.getNickname());
            validateFormat(nick);

            // 닉네임 중복 검사
            if (memberRepository.nicknameCheckWithMemberId(nick, null)) {
                throw new IllegalArgumentException("NICKNAME_CONFLICT");
            }

            String prefix = "profile-photos/" + memberId;

            if (profileImage != null && !profileImage.isEmpty()) {
                // 이전 파일 삭제
                if (member.getProfileImage() != null && !member.getProfileImage().isBlank()) {
                    fileStorage.delete(member.getProfileImage());
                }

                // S3 업로드 → Public URL 반환
                String publicUrl = fileStorage.upload(prefix, profileImage);

                member.updateProfileWithImage(
                        request.getNickname(),
                        publicUrl,  // Public URL 저장
                        request.getHeight(),
                        request.getWeight(),
                        request.getGender(),
                        request.getBirth()
                );
                log.info("신규 정보 입력 + 이미지저장, 경로 : {}", publicUrl);
            } else {
                member.updateProfile(
                        request.getNickname(),
                        request.getHeight(),
                        request.getWeight(),
                        request.getGender(),
                        request.getBirth()
                );
                log.info("신규 정보만 입력");
            }

            new AddInfoResponseDto("신규 회원 정보가 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            log.error("추가 정보 입력 실패 (잘못된 요청): {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("추가 정보 입력 실패 (서버 에러): {}", e.getMessage(), e);
            throw new RuntimeException("추가 정보 입력 중 오류가 발생했습니다", e);
        }
    }

    @Transactional(readOnly = true)
    public NicknameCheckResponseDto checkNicknameAvailability(String rawNickname, Long selfMemberId) {
        String normalized = normalize(rawNickname);

        // 포맷 검증(길이/허용 문자)
        validateFormat(normalized);

        // 자기 자신 닉네임 허용
        Optional<Member> owner = memberRepository.findById(selfMemberId == null ? -1L : selfMemberId);
        if (owner.isPresent() && equalsNormalized(owner.get().getNickname(), normalized)) {
            return NicknameCheckResponseDto.builder()
                    .available(true)
                    .build();
        }

        boolean exists = memberRepository.nicknameCheckWithMemberId(normalized, selfMemberId);
        if (!exists) {
            return NicknameCheckResponseDto.builder()
                    .available(true)
                    .build();
        }

        return NicknameCheckResponseDto.builder()
                .available(false)
                .build();
    }

    private String normalize(String s) {
        if (s == null) throw new IllegalArgumentException("NICKNAME_REQUIRED");
        return s.trim();
    }

    private boolean equalsNormalized(String a, String b) {
        if (a == null) return false;
        return a.trim().equalsIgnoreCase(b);
    }

    private void validateFormat(String nick) {
        if (nick.length() < 2 || nick.length() > 50) {
            throw new IllegalArgumentException("NICKNAME_FORMAT_INVALID");
        }
    }

    @Transactional(readOnly = true)
    public ProfileResponseDto getProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("MEMBER_NOT_FOUND"));
        return ProfileResponseDto.from(member);
    }

    @Transactional
    public void updateProfile(Long memberId, ProfileUpdateRequestDto request, MultipartFile profileImage) {
        try {
            // 회원 조회
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("MEMBER_NOT_FOUND"));

            if (request != null && request.getNickname() != null) {
                String nick = request.getNickname().trim();
                if (!nick.isBlank()) {
                    validateFormat(nick);
                    if (memberRepository.nicknameCheckWithMemberId(nick, memberId)) {
                        throw new IllegalArgumentException("NICKNAME_CONFLICT");
                    }
                    member.setNickname(nick);
                }
            }

            // 이미지 교체 처리
            if (profileImage != null && !profileImage.isEmpty()) {
                if (member.getProfileImage() != null && !member.getProfileImage().isBlank()) {
                    fileStorage.delete(member.getProfileImage());
                }

                String prefix = "profile-photos/" + memberId;
                String publicUrl = fileStorage.upload(prefix, profileImage);
                member.setProfileImage(publicUrl);
                log.info("내 정보 수정 + 이미지저장, 경로 : {}", publicUrl);
            }

            // 부분 업데이트(전달된 필드만 변경)
            if (request != null && request.getHeight() != null && request.getHeight() > 0) {
                member.setHeight(request.getHeight());
            }
            if (request != null && request.getWeight() != null && request.getWeight() > 0) {
                member.setWeight(request.getWeight());
            }
            log.info("내 정보 수정 완료");

        } catch (IllegalArgumentException e) {
            log.error("회원 정보 수정 실패 (잘못된 요청): {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("회원 정보 수정 실패 (서버 에러): {}", e.getMessage(), e);
            throw new RuntimeException("회원 정보 수정 중 오류가 발생했습니다", e);
        }
    }

    public TokenResponseDto refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("INVALID_TOKEN");
        }
        Claims claims = jwtTokenProvider.parseClaims(refreshToken);
        String email = claims.getSubject();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("MEMBER_NOT_FOUND"));
        String newAccess = jwtTokenProvider.createAccessToken(member);
        String newRefresh = jwtTokenProvider.createRefreshToken(member.getEmail());
        return new TokenResponseDto(newAccess, newRefresh);
    }

    @Transactional
    public void logout(Long memberId, String accessToken) {
        try {
            // Redis에 토큰 블랙리스트 등록
            tokenBlacklistService.addToBlacklist(accessToken);
            log.info("로그아웃 완료: memberId={}", memberId);
        } catch (Exception e) {
            log.error("로그아웃 처리 실패: memberId={}", memberId, e);
            throw new RuntimeException("Logout failed", e);
        }
    }
}