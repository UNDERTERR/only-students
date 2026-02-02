package com.onlystudents.common.web.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.onlystudents.common.core.constants.CommonConstants;
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
    
    public DecodedJWT verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return null;
        }
    }
    
    public Long getUserId(String token) {
        DecodedJWT jwt = verifyToken(token);
        if (jwt != null) {
            return jwt.getClaim("userId").asLong();
        }
        return null;
    }
    
    public String getUsername(String token) {
        DecodedJWT jwt = verifyToken(token);
        if (jwt != null) {
            return jwt.getSubject();
        }
        return null;
    }
    
    public String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(CommonConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(CommonConstants.TOKEN_PREFIX.length());
        }
        return null;
    }
}
