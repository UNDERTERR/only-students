package com.onlystudents.search.listener;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.onlystudents.common.event.user.UserInfoUpdatedEvent;
import com.onlystudents.search.config.RabbitConfig;
import com.onlystudents.search.entity.NoteDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserInfoUpdateEventListener {

    private final ElasticsearchClient elasticsearchClient;

    @RabbitListener(queues = RabbitConfig.USER_INFO_UPDATE_QUEUE)
    public void handleUserInfoUpdated(UserInfoUpdatedEvent event) {
        log.info("收到用户信息更新事件: userId={}, nickname={}", 
                event.getUserId(), event.getNickname());

        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("notes")
                .query(q -> q.term(t -> t.field("userId").value(event.getUserId())))
            );

            var response = elasticsearchClient.search(searchRequest, NoteDocument.class);

            if (response.hits().hits().isEmpty()) {
                log.info("用户 {} 没有相关笔记，无需更新", event.getUserId());
                return;
            }

            for (Hit<NoteDocument> hit : response.hits().hits()) {
                Map<String, Object> updateFields = new HashMap<>();
                if (event.getNickname() != null) {
                    updateFields.put("nickname", event.getNickname());
                }
                if (event.getAvatar() != null) {
                    updateFields.put("avatar", event.getAvatar());
                }

                if (!updateFields.isEmpty()) {
                    UpdateRequest<NoteDocument, Object> updateRequest = UpdateRequest.of(u -> u
                        .index("notes")
                        .id(hit.id())
                        .doc(updateFields)
                    );
                    elasticsearchClient.update(updateRequest, NoteDocument.class);
                    log.info("更新ES笔记 author 信息: noteId={}, userId={}", hit.id(), event.getUserId());
                }
            }

            log.info("用户 {} 的 {} 条笔记作者信息已更新", event.getUserId(), response.hits().hits().size());

        } catch (Exception e) {
            log.error("处理用户信息更新事件失败: userId={}", event.getUserId(), e);
        }
    }
}
