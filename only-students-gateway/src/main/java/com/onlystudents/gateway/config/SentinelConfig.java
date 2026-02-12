package com.onlystudents.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sentinel限流配置
 */
@Slf4j
@Configuration
public class SentinelConfig {

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public SentinelConfig(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                          ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    /**
     * Sentinel过滤器
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    /**
     * 限流异常处理器
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    /**
     * 自定义限流返回
     */
    @PostConstruct
    public void initBlockHandler() {
        BlockRequestHandler blockRequestHandler = (exchange, throwable) -> {
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(
                            "{\"code\":429,\"message\":\"系统繁忙，请稍后再试\"}"
                    ));
        };
        GatewayCallbackManager.setBlockHandler(blockRequestHandler);

        // 初始化限流规则
        initGatewayRules();
    }

    /**
     * 配置限流规则
     */
    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();

        // 1. 用户服务限流 - 登录注册接口
        rules.add(new GatewayFlowRule("user-service")
                .setCount(200) // QPS 10
                .setIntervalSec(1)
                .setBurst(100) // 突发流量
        );

        // 2. 笔记服务限流 - 查询接口
        rules.add(new GatewayFlowRule("note-service")
                .setCount(200) // QPS 30
                .setIntervalSec(1)
        );

        // 3. 搜索服务限流
        rules.add(new GatewayFlowRule("search-service")
                .setCount(200) // QPS 20
                .setIntervalSec(1)
        );

        // 4. 评分服务限流
        rules.add(new GatewayFlowRule("rating-service")
                .setCount(200)
                .setIntervalSec(1)
        );

        // 5. 评论服务限流
        rules.add(new GatewayFlowRule("comment-service")
                .setCount(200)
                .setIntervalSec(1)
        );

        // 6. 文件服务限流 - 上传下载
        rules.add(new GatewayFlowRule("file-service")
                .setCount(100)
                .setIntervalSec(1)
        );

        // 7. 支付服务限流 - 严格限制
        rules.add(new GatewayFlowRule("payment-service")
                .setCount(200) // QPS 5，严格限流
                .setIntervalSec(1)
        );

        GatewayRuleManager.loadRules(rules);
        log.info("Sentinel限流规则加载完成，共 {} 条规则", rules.size());
    }
}
