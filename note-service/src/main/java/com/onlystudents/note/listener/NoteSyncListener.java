package com.onlystudents.note.listener;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.onlystudents.common.result.Result;
import com.onlystudents.note.client.UserFeignClient;
import com.onlystudents.note.elasticsearch.NoteDocument;
import com.onlystudents.note.entity.Note;
import com.onlystudents.note.mapper.NoteMapper;
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
    private final NoteMapper noteMapper;
    
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
    public void handleNoteSync(Object message) {
        log.info("收到笔记同步消息: message={}, type={}", message, message != null ? message.getClass().getName() : "null");
        
        Long noteId = null;
        
        // 支持两种消息格式：Note 对象 或 noteId
        if (message instanceof Note) {
            noteId = ((Note) message).getId();
            syncNoteToEs((Note) message);
        } else if (message instanceof Long) {
            noteId = (Long) message;
            // 通过 noteId 查询数据库获取最新数据
            syncNoteByIdToEs(noteId);
        } else if (message instanceof Number) {
            noteId = ((Number) message).longValue();
            syncNoteByIdToEs(noteId);
        } else if (message instanceof String) {
            try {
                noteId = Long.parseLong((String) message);
                syncNoteByIdToEs(noteId);
            } catch (NumberFormatException e) {
                log.error("解析noteId失败: message={}", message, e);
            }
        } else {
            log.warn("未知的消息类型: type={}", message != null ? message.getClass().getName() : "null");
        }
    }
    
    /**
     * 通过 Note 对象同步到 ES
     */
    private void syncNoteToEs(Note note) {
        log.info("通过Note对象同步到ES: noteId={}", note.getId());
        try {
            NoteDocument document = new NoteDocument();
            BeanUtils.copyProperties(note, document);
            document.setNoteId(note.getId());
            document.setRating(note.getAverageRating() != null ? note.getAverageRating().doubleValue() : null);
            document.setRatingCount(note.getRatingCount());

            // 获取用户信息
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
        }
    }
    
    /**
     * 通过 noteId 查询数据库后同步到 ES
     */
    private void syncNoteByIdToEs(Long noteId) {
        log.info("通过noteId查询数据库同步到ES: noteId={}", noteId);
        try {
            Note note = noteMapper.selectById(noteId);
            if (note == null) {
                log.warn("笔记不存在: noteId={}", noteId);
                return;
            }
            syncNoteToEs(note);
        } catch (Exception e) {
            log.error("查询并同步笔记到ES失败: noteId={}", noteId, e);
        }
    }
}
