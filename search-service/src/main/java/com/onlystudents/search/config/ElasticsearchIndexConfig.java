package com.onlystudents.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Elasticsearch 索引初始化配置
 * Search-Service 启动时自动创建索引（如果不存在）
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ElasticsearchIndexConfig {

    private final ElasticsearchClient elasticsearchClient;

    private static final String NOTES_INDEX = "notes";

    @PostConstruct
    public void init() {
        log.info("开始检查并初始化 ES 索引...");
        try {
            createNotesIndexIfNotExists();
            log.info("ES 索引初始化完成");
        } catch (Exception e) {
            log.error("ES 索引初始化失败", e);
            // 不影响服务启动
        }
    }

    /**
     * 创建笔记索引（如果不存在）
     */
    private void createNotesIndexIfNotExists() throws IOException {
        boolean exists = elasticsearchClient.indices()
                .exists(e -> e.index(NOTES_INDEX))
                .value();

        if (!exists) {
            log.info("索引 {} 不存在，开始创建...", NOTES_INDEX);

            CreateIndexRequest request = CreateIndexRequest.of(c -> c
                    .index(NOTES_INDEX)
                    .mappings(getNotesMapping())
            );

            elasticsearchClient.indices().create(request);
            log.info("索引 {} 创建成功", NOTES_INDEX);
        } else {
            log.info("索引 {} 已存在", NOTES_INDEX);
        }
    }

    /**
     * 笔记索引 Mapping
     * 使用 IK 中文分词器（需要先安装 elasticsearch-analysis-ik 插件）
     */
    private TypeMapping getNotesMapping() {
        return TypeMapping.of(m -> m
                // noteId
                .properties("noteId", p -> p.long_(l -> l))
                // title - 标题（使用 IK 分词器）
                .properties("title", p -> p.text(t -> t
                        .analyzer("ik_max_word")
                        .searchAnalyzer("ik_smart")
                ))
                // content - 内容（使用 IK 分词器）
                .properties("content", p -> p.text(t -> t
                        .analyzer("ik_max_word")
                        .searchAnalyzer("ik_smart")
                ))
                // tags - 标签
                .properties("tags", p -> p.keyword(k -> k))
                // userId
                .properties("userId", p -> p.long_(l -> l))
                // authorNickname（使用 IK 分词器）
                .properties("authorNickname", p -> p.text(t -> t
                        .analyzer("ik_max_word")
                ))
                // authorAvatar
                .properties("authorAvatar", p -> p.keyword(k -> k))
                // educationLevel
                .properties("educationLevel", p -> p.integer(i -> i))
                // schoolId
                .properties("schoolId", p -> p.long_(l -> l))
                // schoolName
                .properties("schoolName", p -> p.keyword(k -> k))
                // visibility
                .properties("visibility", p -> p.integer(i -> i))
                // price
                .properties("price", p -> p.scaledFloat(s -> s.scalingFactor(100.0)))
                // status
                .properties("status", p -> p.integer(i -> i))
                // deleted
                .properties("deleted", p -> p.integer(i -> i))
                // hotScore
                .properties("hotScore", p -> p.double_(d -> d))
                // viewCount
                .properties("viewCount", p -> p.integer(i -> i))
                // favoriteCount
                .properties("favoriteCount", p -> p.integer(i -> i))
                // commentCount
                .properties("commentCount", p -> p.integer(i -> i))
                // shareCount
                .properties("shareCount", p -> p.integer(i -> i))
                // ratingAvg
                .properties("ratingAvg", p -> p.float_(f -> f))
                // coverImage
                .properties("coverImage", p -> p.keyword(k -> k))
                // publishTime
                .properties("publishTime", p -> p.date(d -> d.format("yyyy-MM-dd'T'HH:mm:ss")))
                // createdAt
                .properties("createdAt", p -> p.date(d -> d.format("yyyy-MM-dd'T'HH:mm:ss")))
        );
    }
}
