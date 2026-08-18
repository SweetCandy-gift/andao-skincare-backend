package com.andao.skincare.common.security;

import com.andao.skincare.module.user.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 每个请求执行一次的 JWT 认证入口：提取 Bearer Token、解析身份、核对用户状态，
 * 最后把认证结果写入 SecurityContext，供后续业务代码获取当前用户。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, UserService userService) {
        this.jwtTokenService = jwtTokenService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 解析本身同时完成签名、签发方和过期时间校验，只需执行一次。
                AuthenticatedUser user = jwtTokenService.parseToken(token);
                // JWT 是无状态凭证，仍需查询数据库，避免已禁用或已删除用户继续使用旧 Token。
                if (!userService.isActiveUser(user.userId(), user.username())) {
                    unauthorized(response);
                    return;
                }
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, token, List.of());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // SecurityContext 默认绑定当前请求线程，下游只依赖认证上下文，无需重复解析 Token。
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException exception) {
                // 不向客户端暴露具体解析原因，所有无效凭证使用统一的 401 响应。
                SecurityContextHolder.clearContext();
                unauthorized(response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未认证或Token无效");
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        return StringUtils.hasText(token) ? token : null;
    }
}
