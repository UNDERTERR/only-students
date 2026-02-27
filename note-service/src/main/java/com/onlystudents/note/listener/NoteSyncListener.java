package com.onlystudents.note.listener;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.onlystudents.common.result.Result;
import com.onlystudents.note.client.UserFeignClient;
import com.onlystudents.note.elasticsearch.NoteDocument;
import com.onlystudents.note.entity.Note;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoteSyncListener {

    private final ElasticsearchClient elasticsearchClient;
    private final UserFeignClient userFeignClient;
    
    @RabbitListener(queues = "note.delete.queue")
    public void handleNoteDelete(Long noteId) {
        log.info("收到笔记删除消息: noteId={}", noteId);
        try {
            // 从Elasticsearch删除文档
            elasticsearchClient.delete(d -> d
                    .index("notes")
                    .id(String.valueOf(noteId))
            );
            log.info("笔记从ES删除成功: noteId={}", noteId);
        } catch (Exception e) {
            log.error("从ES删除笔记失败: noteId={}", noteId, e);
        }
    }

    @RabbitListener(queues = "note.sync.queue")
    public void handleNoteSync(Note note) {
        log.info("收到笔记同步消息: noteId={}", note.getId());
        try {
            NoteDocument document = new NoteDocument();
            BeanUtils.copyProperties(note, document);
            document.setNoteId(note.getId());

            String nickname = "用户_" + note.getUserId();
            String avatar = null;
            try {
                Result<?> result = userFeignClient.getUserById(note.getUserId());
                if (result != null && result.getData() != null) {
                    Map<?, ?> userMap = (Map<?, ?>) result.getData();
                    Object nicknameObj = userMap.get("nickname");
                    if (nicknameObj != null) {
                        nickname = nicknameObj.toString();
                    }
                    Object avatarObj = userMap.get("avatar");
                    if (avatarObj != null) {
                        avatar = avatarObj.toString();
                    }
                }
            } catch (Exception e) {
                log.warn("获取用户信息失败: userId={}", note.getUserId());
            }
            document.setAuthorNickname(nickname);
            document.setAuthorAvatar(avatar);
            
            // 写入Elasticsearch
            IndexRequest<NoteDocument> indexRequest = IndexRequest.of(i -> i
                    .index("notes")
                    .id(String.valueOf(note.getId()))
                    .document(document)
            );
            elasticsearchClient.index(indexRequest);
            
            log.info("笔记同步到ES成功: noteId={}", note.getId());
        } catch (Exception e) {
            log.error("同步笔记到ES失败: noteId={}", note.getId(), e);
            // TODO: 可以发送到死信队列或记录到数据库重试
        }
    }
}
