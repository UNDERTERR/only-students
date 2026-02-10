package com.onlystudents.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import com.onlystudents.common.result.Result;
import com.onlystudents.search.client.NoteClient;
import com.onlystudents.search.client.UserClient;
import com.onlystudents.search.entity.NoteDocument;
import com.onlystudents.search.dto.NoteResponse;
import com.onlystudents.search.dto.UserResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Elasticsearch 索引管理服务
 * 集中式管理所有搜索索引
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchIndexService {
    
    private final ElasticsearchClient elasticsearchClient;
    private final NoteClient noteClient; // 需要创建
    
    private static final String NOTES_INDEX = "notes";
    
    @PostConstruct
    public void initializeAllIndexes() {
        log.info("开始初始化所有 ES 索引...");
        try {
            initializeNotesIndex();
            log.info("ES 索引初始化完成");
        } catch (Exception e) {
            log.error("ES 索引初始化失败", e);
        }
    }
    
    /**
     * 初始化笔记索引
     */
    private void initializeNotesIndex() throws IOException {
        // 1. 检查并创建索引
        if (!indexExists(NOTES_INDEX)) {
            log.info("创建笔记索引...");
            CreateIndexRequest request = CreateIndexRequest.of(c -> c
                    .index(NOTES_INDEX)
                    .mappings(getNotesMapping())
            );
            elasticsearchClient.indices().create(request);
            log.info("笔记索引创建成功");
        }
        
        // 2. 检查是否需要同步数据
        if (isIndexEmpty(NOTES_INDEX)) {
            log.info("笔记索引为空，开始同步数据...");
            syncNotesFromNoteService();
        }
    }
    
    /**
     * 获取笔记索引映射
     */
    private TypeMapping getNotesMapping() {
        return TypeMapping.of(m -> m
                .properties("noteId", p -> p.long_(l -> l))
                .properties("title", p -> p.text(t -> t
                        .analyzer("ik_max_word")
                        .searchAnalyzer("ik_smart")
                ))
                .properties("content", p -> p.text(t -> t
                        .analyzer("ik_max_word")
                        .searchAnalyzer("ik_smart")
                ))
                .properties("tags", p -> p.keyword(k -> k))
                .properties("categoryId", p -> p.long_(l -> l))
                .properties("categoryName", p -> p.keyword(k -> k))
                .properties("userId", p -> p.long_(l -> l))
                .properties("username", p -> p.keyword(k -> k))
                .properties("nickname", p -> p.text(t -> t
                        .analyzer("ik_max_word")
                ))
                .properties("educationLevel", p -> p.integer(i -> i))
                .properties("schoolId", p -> p.long_(l -> l))
                .properties("schoolName", p -> p.keyword(k -> k))
                .properties("subject", p -> p.keyword(k -> k))
                .properties("visibility", p -> p.integer(i -> i))
                .properties("price", p -> p.scaledFloat(s -> s.scalingFactor(100.0)))
                .properties("status", p -> p.integer(i -> i))
                .properties("hotScore", p -> p.double_(d -> d))
                .properties("viewCount", p -> p.integer(i -> i))
                .properties("likeCount", p -> p.integer(i -> i))
                .properties("favoriteCount", p -> p.integer(i -> i))
                .properties("commentCount", p -> p.integer(i -> i))
                .properties("shareCount", p -> p.integer(i -> i))
                .properties("ratingAvg", p -> p.float_(f -> f))
                .properties("coverImage", p -> p.keyword(k -> k))
                .properties("publishTime", p -> p.date(d -> d.format("yyyy-MM-dd'T'HH:mm:ss")))
                .properties("createdAt", p -> p.date(d -> d.format("yyyy-MM-dd'T'HH:mm:ss")))
        );
    }
    
    /**
     * 从 Note-Service 同步笔记数据
     */
    private void syncNotesFromNoteService() {
        try {
            int pageSize = 100;
            int page = 1;
            int totalSynced = 0;
            
            while (true) {
                List<NoteResponse> notes = null;
                try {
                    // 调用 Note-Service 获取已发布笔记
                    log.info("正在获取第 {} 页笔记数据...", page);
                    Result<List<NoteResponse>> result = noteClient.getPublishedNotes(page, pageSize);
                    
                    if (result == null) {
                        log.warn("Note-Service 返回 null，停止同步");
                        break;
                    }
                    
                    if (!result.isSuccess()) {
                        log.warn("Note-Service 调用失败: {}, 停止同步", result.getMessage());
                        break;
                    }
                    
                    if (result.getData() == null || result.getData().isEmpty()) {
                        log.info("第 {} 页没有数据，同步完成", page);
                        break;
                    }
                    
                    notes = result.getData();
                    log.info("成功获取第 {} 页 {} 条笔记", page, notes.size());
                    
                } catch (Exception e) {
                    log.error("调用 Note-Service 失败: page={}, error={}", page, e.getMessage());
                    break;
                }
                
                if (notes == null || notes.isEmpty()) {
                    break;
                }
                
                // 批量保存到 ES
                for (NoteResponse note : notes) {
                    try {
                        // 直接从NoteResponse获取用户信息（Note-Service已处理了跨库问题）
                        NoteDocument document = new NoteDocument();
                        BeanUtils.copyProperties(note, document);
                        document.setNoteId(note.getId());
                        
                        IndexRequest<NoteDocument> request = IndexRequest.of(i -> i
                                .index(NOTES_INDEX)
                                .id(String.valueOf(note.getId()))
                                .document(document)
                        );
                        elasticsearchClient.index(request);
                        totalSynced++;
                    } catch (Exception e) {
                        log.error("同步笔记到 ES 失败: noteId={}", note.getId(), e);
                    }
                }
                
                log.info("已同步 {} 条笔记到 ES", totalSynced);
                page++;
                
                Thread.sleep(100); // 防止请求过快
            }
            
            log.info("笔记同步完成，共同步 {} 条", totalSynced);
            
        } catch (Exception e) {
            log.error("同步笔记数据失败", e);
        }
    }


    /**
     * 检查索引是否存在
     */
    private boolean indexExists(String indexName) throws IOException {
        return elasticsearchClient.indices()
                .exists(e -> e.index(indexName))
                .value();
    }
    
    /**
     * 检查索引是否为空
     */
    private boolean isIndexEmpty(String indexName) throws IOException {
        try {
            var count = elasticsearchClient.count(c -> c.index(indexName));
            return count.count() == 0;
        } catch (Exception e) {
            log.warn("检查索引文档数失败，假设为空", e);
            return true;
        }
    }

}