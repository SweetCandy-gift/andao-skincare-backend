package com.andao.skincare.common.security;

import com.andao.skincare.module.user.service.UserService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final JwtTokenService jwtTokenService = mock(JwtTokenService.class);
    private final UserService userService = mock(UserService.class);
    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(jwtTokenService, userService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateActiveUserWithSingleTokenParse() throws Exception {
        AuthenticatedUser user = new AuthenticatedUser(1001L, "andao_user");
        when(jwtTokenService.parseToken("valid-token")).thenReturn(user);
        when(userService.isActiveUser(1001L, "andao_user")).thenReturn(true);
        MockHttpServletRequest request = requestWithToken("valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(user);
        assertThat(filterChain.getRequest()).isSameAs(request);
        verify(jwtTokenService).parseToken("valid-token");
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenCannotBeParsed() throws Exception {
        when(jwtTokenService.parseToken("invalid-token")).thenThrow(new JwtException("invalid token"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(requestWithToken("invalid-token"), response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(filterChain.getRequest()).isNull();
    }

    @Test
    void shouldReturnUnauthorizedWhenUserIsNoLongerActive() throws Exception {
        AuthenticatedUser user = new AuthenticatedUser(1001L, "andao_user");
        when(jwtTokenService.parseToken("valid-token")).thenReturn(user);
        when(userService.isActiveUser(1001L, "andao_user")).thenReturn(false);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(requestWithToken("valid-token"), response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(filterChain.getRequest()).isNull();
    }

    private MockHttpServletRequest requestWithToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
