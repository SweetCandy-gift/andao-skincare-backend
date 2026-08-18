package com.andao.skincare.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

/**
 * 负责 JWT 的签发和解析。Token 只保存识别当前用户所需的最小信息，
 * 不保存密码等敏感数据；服务端通过签名保证这些声明未被客户端篡改。
 */
@Component
public class JwtTokenService {

    private static final String USERNAME_CLAIM = "username";

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = createSigningKey(properties.getSecret());
    }

    /**
     * 登录成功后签发 Token。subject 保存稳定的用户 ID，username 作为辅助身份声明，
     * 签发时间和过期时间用于限制凭证的有效窗口。
     */
    public String generateToken(Long userId, String username) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.getExpiration());
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(userId.toString())
                .claim(USERNAME_CLAIM, username)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    /**
     * 校验签名、签发方和有效期并还原认证主体。JJWT 在解析过程中完成这些校验，
     * 因此调用方无需先校验再解析，避免同一个 Token 被重复处理。
     *
     * @throws JwtException Token 过期、签名错误、签发方不匹配或声明不完整
     * @throws IllegalArgumentException Token 格式或用户 ID 非法
     */
    public AuthenticatedUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get(USERNAME_CLAIM, String.class);
        if (!StringUtils.hasText(username)) {
            throw new JwtException("JWT缺少用户名");
        }
        return new AuthenticatedUser(userId, username);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private SecretKey createSigningKey(String base64Secret) {
        // 启动时一次性构造签名密钥，可尽早暴露缺失、格式错误或强度不足的配置。
        if (!StringUtils.hasText(base64Secret)) {
            throw new IllegalStateException("JWT密钥不能为空");
        }
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        } catch (RuntimeException exception) {
            throw new IllegalStateException("JWT密钥必须是有效且长度不少于256位的Base64字符串", exception);
        }
    }
}
