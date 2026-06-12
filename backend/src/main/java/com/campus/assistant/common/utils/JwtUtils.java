package com.campus.assistant.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

/**
 * JWT ?? ??????JWT ????????????
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-hours:24}")
    private Long expireHours;

    public String createToken(Long userId, String username, String roleCode) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + Duration.ofHours(expireHours).toMillis());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(Map.of("username", username, "roleCode", roleCode))
                .issuedAt(now)  //issuedAt：Token 什么时候创建的
                .expiration(expireAt)  //expiration：Token 什么时候失效
                .signWith(secretKey())
                .compact();  //将所有的信息压缩成一个字符串
    }

    public Claims parse(String token) {  //输入：token - JWT 字符串  输出：Claims - JWT 的载荷数据
        return Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)//：解析并验证 Token
                .getPayload();  //获取载荷数据
    }

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
