package com.runnity.member.util;

import com.runnity.member.domain.Member;
import com.runnity.member.dto.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenValidityInMilliseconds = 3600000; // 1시간
    private final long refreshTokenValidityInMilliseconds = 1209600000; // 2주


    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // Access Token 생성
    public String createAccessToken(Member member) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(member.getMemberId().toString())
                .claim("email", member.getEmail())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 3600000)) // 1시간
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(String email) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 86400000)) // 24시간
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        // 여기서는 권한 정보를 "ROLE_USER"로 단순화합니다.
        Collection<? extends GrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        // claims에서 memberId를 가져옵니다. Long 타입으로 변환합니다.
        Long memberId = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);

        // UserPrincipal 객체를 생성합니다.
        UserPrincipal principal = new UserPrincipal(memberId, email, authorities);

        // Authentication 객체를 생성하여 반환합니다.
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // validateToken 메소드 (이 코드가 없으면 추가해주세요)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 토큰 유효성 검사 실패 시 로깅을 추가하면 디버깅에 도움이 됩니다.
            // 예: log.error("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    public SecretKey getSecretKey() {
        return key;
    }
}
