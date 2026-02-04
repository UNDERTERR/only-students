package com.onlystudents.common.core.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.onlystudents.common.core.constants.CommonConstants;
import com.onlystudents.common.core.exception.BusinessException; // 用你 core 里的异常
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret:OnlyStudentsSecretKey2024}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return JWT.create()
                .withSubject(username)
                .withClaim("userId", userId)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .sign(Algorithm.HMAC256(secret));
    }

    // 改为抛异常，让调用方决定如何处理
    public DecodedJWT verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            log.warn("Token验证失败: {}", e.getMessage());
            throw new BusinessException("Token无效或已过期"); // 用你的业务异常
        }
    }

    public Long getUserId(String token) {
        return verifyToken(token).getClaim("userId").asLong();
    }

    public String getUsername(String token) {
        return verifyToken(token).getSubject();
    }

    // 这个可以保留，也可以移到 web 层的工具类
    public String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(CommonConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(CommonConstants.TOKEN_PREFIX.length());
        }
        return null;
    }

    // 新增：判断是否过期（不抛异常）
    public boolean isTokenValid(String token) {
        try {
            verifyToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}