package com.onlystudents.note.listener;

import com.onlystudents.note.event.NoteFavoriteEvent;
import com.onlystudents.note.event.NoteRatingEvent;
import com.onlystudents.note.event.NoteShareEvent;
import com.onlystudents.note.mapper.NoteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 评分事件监听器
 * 接收 rating-service 发送的事件，更新笔记统计计数
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RatingEventListener {
    
    private final NoteMapper noteMapper;
    
    /**
     * 监听收藏事件
     */
    @RabbitListener(queues = "favorite.created.queue")
    public void handleFavoriteCreated(NoteFavoriteEvent event) {
        log.info("收到收藏事件: noteId={}, userId={}, total={}", 
                event.getNoteId(), event.getUserId(), event.getTotalCount());
        try {
            // 直接更新为总数（更精确）
            noteMapper.updateFavoriteCount(event.getNoteId(), event.getTotalCount().intValue());
            log.info("更新笔记收藏数成功: noteId={}, count={}", event.getNoteId(), event.getTotalCount());
        } catch (Exception e) {
            log.error("更新笔记收藏数失败: noteId={}", event.getNoteId(), e);
        }
    }
    
    /**
     * 监听取消收藏事件
     */
    @RabbitListener(queues = "favorite.deleted.queue")
    public void handleFavoriteDeleted(NoteFavoriteEvent event) {
        log.info("收到取消收藏事件: noteId={}, userId={}, total={}", 
                event.getNoteId(), event.getUserId(), event.getTotalCount());
        try {
            noteMapper.updateFavoriteCount(event.getNoteId(), event.getTotalCount().intValue());
            log.info("更新笔记收藏数成功: noteId={}, count={}", event.getNoteId(), event.getTotalCount());
        } catch (Exception e) {
            log.error("更新笔记收藏数失败: noteId={}", event.getNoteId(), e);
        }
    }
    
    /**
     * 监听评分事件
     */
    @RabbitListener(queues = "rating.updated.queue")
    public void handleRatingUpdated(NoteRatingEvent event) {
        log.info("收到评分事件: noteId={}, userId={}, score={}, avg={}, count={}", 
                event.getNoteId(), event.getUserId(), event.getScore(), 
                event.getAverageScore(), event.getRatingCount());
        try {
            // 更新平均分和评分人数
            noteMapper.updateRatingStats(event.getNoteId(), 
                    event.getAverageScore(), event.getRatingCount().intValue());
            log.info("更新笔记评分统计成功: noteId={}, avg={}, count={}", 
                    event.getNoteId(), event.getAverageScore(), event.getRatingCount());
        } catch (Exception e) {
            log.error("更新笔记评分统计失败: noteId={}", event.getNoteId(), e);
        }
    }
    
    /**
     * 监听分享事件
     */
    @RabbitListener(queues = "share.created.queue")
    public void handleShareCreated(NoteShareEvent event) {
        log.info("收到分享事件: noteId={}, userId={}, total={}", 
                event.getNoteId(), event.getUserId(), event.getTotalCount());
        try {
            noteMapper.updateShareCount(event.getNoteId(), event.getTotalCount().intValue());
            log.info("更新笔记分享数成功: noteId={}, count={}", event.getNoteId(), event.getTotalCount());
        } catch (Exception e) {
            log.error("更新笔记分享数失败: noteId={}", event.getNoteId(), e);
        }
    }
}
