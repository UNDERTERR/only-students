package com.onlystudents.gateway.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.onlystudents.common.constants.CommonConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("#{'${whitelist.paths}'.split(',')}")
    private List<String> whitelistPaths;
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // 检查是否是白名单路径
        if (isWhitelistPath(path)) {
            // 即使是白名单，如果前端传了 CommonConstants.USER_ID_HEADER，也要保留
            String userIdFromHeader = request.getHeaders().getFirst(CommonConstants.USER_ID_HEADER);
            if (userIdFromHeader != null && !userIdFromHeader.isEmpty()) {
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header(CommonConstants.USER_ID_HEADER, userIdFromHeader)
                        .build();
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            }
            return chain.filter(exchange);
        }
        
        // 获取Token
        String token = request.getHeaders().getFirst(CommonConstants.TOKEN_HEADER);
        if (token == null || !token.startsWith(CommonConstants.TOKEN_PREFIX)) {
            return unauthorized(exchange.getResponse(), "缺少认证信息");
        }
        
        token = token.substring(7);
        
        try {
            // 验证Token
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            
            // 提取用户信息
            String userId = jwt.getClaim("userId").asString();
            
            // 如果JWT中没有userId，尝试从前端header获取
            if (userId == null || userId.isEmpty()) {
                userId = request.getHeaders().getFirst(CommonConstants.USER_ID_HEADER);
            }
            
            // 将用户信息添加到请求头
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(CommonConstants.USER_ID_HEADER, userId != null ? userId : "")
                    .build();
            
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
            
        } catch (JWTVerificationException e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return unauthorized(exchange.getResponse(), "Token无效或已过期");
        }
    }
    
    private boolean isWhitelistPath(String path) {
        if (CollectionUtils.isEmpty(whitelistPaths)) {
            return false;
        }
        return whitelistPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern.trim(), path));
    }
    
    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"code\":401,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}", 
                message, System.currentTimeMillis());
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
    
    @Override
    public int getOrder() {
        return -100;  // 最高优先级
    }
}
