package com.andao.skincare.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setIssuer("andao-skincare-test");
        properties.setExpiration(Duration.ofMinutes(30));
        properties.setSecret("b3BlbmFpLWFuZGFvLXNraW5jYXJlLXRlc3Qtand0LWtleS0yMDI2");
        jwtTokenService = new JwtTokenService(properties);
    }

    @Test
    void shouldGenerateAndParseToken() {
        String token = jwtTokenService.generateToken(1001L, "andao_user");

        AuthenticatedUser user = jwtTokenService.parseToken(token);

        assertThat(jwtTokenService.validateToken(token)).isTrue();
        assertThat(user.userId()).isEqualTo(1001L);
        assertThat(user.username()).isEqualTo("andao_user");
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = jwtTokenService.generateToken(1001L, "andao_user");
        String tamperedToken = token.substring(0, token.length() - 1)
                + (token.endsWith("a") ? "b" : "a");

        assertThat(jwtTokenService.validateToken(tamperedToken)).isFalse();
    }
}
