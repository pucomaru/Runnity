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
    private final long accessTokenValidityInMs = 3_600_000;     // 1h
    private final long refreshTokenValidityInMs = 86_400_000;   // 24h

    public JwtTokenProvider(@Value("${JWT_SECRET}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Member member) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(member.getMemberId().toString())
                .claim("email", member.getEmail())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + accessTokenValidityInMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String createRefreshToken(String email) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshTokenValidityInMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        Collection<? extends GrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        Long memberId = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);
        UserPrincipal principal = new UserPrincipal(memberId, email, authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception ignore) {
            // 로그 필요 시 추가
        }
        return false;
    }

    public SecretKey getSecretKey() {
        return key;
    }
}
