package com.onlystudents.search.listener;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.onlystudents.search.config.RabbitConfig;
import com.onlystudents.search.entity.NoteDocument;
import com.onlystudents.search.event.UserInfoUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 用户信息更新事件监听器
 * 监听用户信息变更事件，更新ES中相关笔记的作者信息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserInfoUpdateEventListener {
    
    private final ElasticsearchClient elasticsearchClient;
    
    /**
     * 监听用户信息更新事件
     */
     @RabbitListener(queues = RabbitConfig.USER_INFO_UPDATE_QUEUE)
    public void handleUserInfoUpdated(UserInfoUpdateEvent event) {
        log.info("收到用户信息更新事件: userId={}, nickname={}", 
                event.getUserId(), event.getNickname());
        
        try {
            // 查询该用户的所有笔记
            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("notes")
                .query(q -> q.term(t -> t.field("userId").value(event.getUserId())))
            );
            
            SearchResponse<NoteDocument> response = elasticsearchClient.search(searchRequest, NoteDocument.class);
            
            if (response.hits().hits().isEmpty()) {
                log.info("用户 {} 没有相关笔记，无需更新", event.getUserId());
                return;
            }
            
// 批量更新作者信息（暂时只记录日志，后续实现ES更新）
            int noteCount = response.hits().hits().size();
            log.info("找到 {} 条相关笔记，userId={}，需要更新作者信息", noteCount, event.getUserId());
            
            // TODO: 实现ES更新逻辑
            // for (Hit<NoteDocument> hit : response.hits().hits()) {
            //     // 更新ES文档中的作者信息
            // }
            
        } catch (Exception e) {
            log.error("处理用户信息更新事件失败: userId={}", event.getUserId(), e);
        }
    }
}