package com.andao.skincare.module.user.service.impl;

import com.andao.skincare.common.security.AuthenticatedUser;
import com.andao.skincare.module.user.service.CurrentUserProvider;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 从 Spring Security 的请求级上下文读取当前用户，而不是让购物车、订单等业务模块
 * 自行解析 Token。这样认证细节集中在安全层，业务层只依赖 CurrentUserProvider 抽象。
 */
@Component
public class SecurityContextCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Long getCurrentUserId() {
        // JwtAuthenticationFilter 已将 AuthenticatedUser 写入当前线程的 SecurityContext。
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new AuthenticationCredentialsNotFoundException("当前用户未认证");
        }
        return user.userId();
    }
}
