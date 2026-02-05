package com.onlystudents.note.listener;

import com.onlystudents.common.result.Result;
import com.onlystudents.note.client.UserFeignClient;
import com.onlystudents.note.elasticsearch.NoteDocument;
import com.onlystudents.note.entity.Note;
import com.onlystudents.note.mapper.NoteCategoryMapper;
import com.onlystudents.note.service.NoteSearchService;
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
    
    private final NoteSearchService noteSearchService;
    private final UserFeignClient userFeignClient;
    private final NoteCategoryMapper noteCategoryMapper;
    
    @RabbitListener(queues = "note.sync.queue")
    public void handleNoteSync(Note note) {
        log.info("收到笔记同步消息: noteId={}", note.getId());
        try {
            NoteDocument document = new NoteDocument();
            BeanUtils.copyProperties(note, document);
            document.setNoteId(note.getId());
            
            // 通过Feign查询用户信息
            String username = "用户_" + note.getUserId();
            String nickname = "用户_" + note.getUserId();
            String avatar = "";
            
            try {
                Result<Map<String, Object>> userResult = userFeignClient.getUserById(note.getUserId());
                if (userResult.getData() != null) {
                    Map<String, Object> userData = userResult.getData();
                    if (userData.get("username") != null) {
                        username = userData.get("username").toString();
                    }
                    if (userData.get("nickname") != null) {
                        nickname = userData.get("nickname").toString();
                    }
                    if (userData.get("avatar") != null) {
                        avatar = userData.get("avatar").toString();
                    }
                }
            } catch (Exception e) {
                log.warn("获取用户信息失败，使用默认信息: userId={}", note.getUserId(), e);
            }
            
            document.setUsername(username);
            document.setNickname(nickname);
            
            // 查询分类名称
            String categoryName = "未分类";
            if (note.getCategoryId() != null) {
                try {
                    String name = noteCategoryMapper.selectNameById(note.getCategoryId());
                    if (name != null && !name.isEmpty()) {
                        categoryName = name;
                    }
                } catch (Exception e) {
                    log.warn("获取分类名称失败，使用默认名称: categoryId={}", note.getCategoryId(), e);
                }
            }
            document.setCategoryName(categoryName);
            
            noteSearchService.saveNote(document);
            log.info("笔记同步到ES成功: noteId={}", note.getId());
        } catch (Exception e) {
            log.error("同步笔记到ES失败: noteId={}", note.getId(), e);
        }
    }
}
