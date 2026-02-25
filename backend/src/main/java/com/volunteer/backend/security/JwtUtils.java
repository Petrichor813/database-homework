package com.volunteer.backend.security;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
    private static final String USER_ID_CLAIM = "uid";

    private final long expiration; // 单位是毫秒
    private final SecretKey key;

    // @formatter:off
    public JwtUtils(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration}") long expiration
    ) {
        // @formatter:on
        byte[] decodedKey = Base64.getUrlDecoder().decode(secret);
        this.key = Keys.hmacShaKeyFor(decodedKey);
        this.expiration = expiration;
    }

    private Claims parseClaims(String token) {
        // @formatter:off
        return Jwts.parser()
            .verifyWith(key)            // 使用密钥常量验证所有签名的 JWT
            .build()                    // 创建并返回线程安全的 JWTParser 对象
            .parseSignedClaims(token)   // 解析 token，验证签名，自动检查过期时间
            .getPayload();              // 获取 Claims
        // @formatter:on
    }

    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        // @formatter:off
        return Jwts.builder()
            .subject(username)              // 存放用户标识
            .claim(USER_ID_CLAIM, userId)   // 存放用户 ID
            .issuedAt(now)                  // 签发时间
            .expiration(expiryDate)         // 过期时间
            .signWith(key)                  // 用密钥签名
            .compact();
        // @formatter:on
    }

    public Long getUserIdFromToken(String token) throws IllegalArgumentException {
        Claims claims = parseClaims(token);
        Object userId = claims.get(USER_ID_CLAIM);

        if (userId instanceof Number) {
            Number number = (Number) userId;
            return number.longValue();
        }

        if (userId instanceof String) {
            String str = (String) userId;
            return Long.parseLong(str);
        }

        throw new IllegalArgumentException("Token 中缺少用户 ID 信息");
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    public Instant getExpirationFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration().toInstant();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
