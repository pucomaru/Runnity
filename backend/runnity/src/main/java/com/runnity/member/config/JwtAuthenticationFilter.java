package com.runnity.member.config;

import com.runnity.global.service.TokenBlacklistService;
import com.runnity.member.dto.UserPrincipal;
import com.runnity.member.util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 인증 필터.
 * - Swagger에서 Authorize에 Bearer 토큰을 설정하면, 이 필터가 토큰을 검증하고 SecurityContext에 인증 정보를 채워준다.
 * - 로그인/토큰/업로드 등 공개 경로는 shouldNotFilter로 우회한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (HttpMethod.OPTIONS.matches(request.getMethod())) return true;
        return uri.equals("/api/v1/auth/login/google")
                || uri.equals("/api/v1/auth/login/kakao")
                || uri.equals("/api/v1/auth/token")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {

                if (tokenBlacklistService.isBlacklisted(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token has been revoked");
                    return;
                }

                // Claims 파싱
                Claims claims = jwtTokenProvider.parseClaims(token);
                Long memberId = Long.parseLong(claims.getSubject());
                String email = claims.get("email", String.class);

                // UserPrincipal 생성
                UserPrincipal userPrincipal = new UserPrincipal(
                        memberId,
                        email,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );

                // 인증 토큰 생성
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        "",
                        userPrincipal.getAuthorities()
                );

                // SecurityContext에 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("JWT 필터 에러", e);
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
