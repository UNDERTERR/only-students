package com.onlystudents.search.listener;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.onlystudents.search.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 笔记删除事件监听器
 * 监听笔记删除事件，从ES中删除对应的文档
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoteDeleteEventListener {

    private final ElasticsearchClient elasticsearchClient;

    /**
     * 监听笔记删除事件
     */
    @RabbitListener(queues = RabbitConfig.NOTE_DELETE_QUEUE)
    public void handleNoteDeleted(Long noteId) {
        log.info("收到笔记删除事件: noteId={}", noteId);

        try {
            elasticsearchClient.delete(d -> d
                .index("notes")
                .id(String.valueOf(noteId))
            );
            log.info("ES中笔记已删除: noteId={}", noteId);
        } catch (Exception e) {
            log.error("从ES删除笔记失败: noteId={}", noteId, e);
        }
    }
}
