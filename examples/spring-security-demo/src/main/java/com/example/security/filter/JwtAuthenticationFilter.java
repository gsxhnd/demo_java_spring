package com.example.security.filter;

import com.example.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 认证过滤器
 * 拦截请求，验证 JWT Token，设置 SecurityContext
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 从请求头提取 JWT
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // 提取用户名
                String username = jwtService.extractUsername(jwt);

                // 如果用户未认证
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 加载用户详情
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 验证 Token
                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        // 创建认证 Token
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        // 设置认证详情
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 设置 SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        log.debug("用户 {} 认证成功", username);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("JWT 认证失败: {}", e.getMessage());
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取 JWT
     * 支持两种方式：
     * 1. Authorization: Bearer <token>
     * 2. 请求参数: token=<token>
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        // 方式1: 从 Authorization 头获取
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 方式2: 从请求参数获取（备用）
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }

        return null;
    }
}
