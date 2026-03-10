package com.onlystudents.note.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlystudents.common.event.note.NoteRatingEvent;
import com.onlystudents.common.utils.JsonSerializerUtils;
import com.onlystudents.common.event.note.NoteFavoriteEvent;
import com.onlystudents.common.event.note.NoteShareEvent;
import com.onlystudents.note.mapper.NoteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 评分事件监听器
 * 接收 rating-service 发送的事件，更新笔记统计计数
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RatingEventListener {

    private final NoteMapper noteMapper;
    private final CacheManager cacheManager;
    private final RabbitTemplate rabbitTemplate;
    private void evictCache(Long noteId) {
        log.info("开始清除缓存, noteId={}, cacheManager={}", noteId, cacheManager);
        if (noteId != null && cacheManager != null) {
            String[] cacheNames = {"notes", "latestNotes", "hotNotes"};
            for (String cacheName : cacheNames) {
                Cache cache = cacheManager.getCache(cacheName);
                log.info("获取缓存: cacheName={}, cache={}", cacheName, cache);
                if (cache != null) {
                    cache.evict(noteId);
                    log.info("清除缓存成功: cacheName={}, key={}", cacheName, noteId);
                }
            }
        } else {
            log.warn("缓存管理器或noteId为空，无法清除缓存");
        }
    }

    /**
     * 同步到 Elasticsearch
     */
    private void syncToEs(Long noteId) {
        try {
            rabbitTemplate.convertAndSend("note.exchange", "note.sync", noteId);
            log.info("发送ES同步消息成功: noteId={}", noteId);
        } catch (Exception e) {
            log.error("发送ES同步消息失败: noteId={}", noteId, e);
        }
    }

    /**
     * 监听收藏事件
     */
    @RabbitListener(queues = "favorite.created.queue")
    @Transactional
    public void handleFavoriteCreated(NoteFavoriteEvent event) {
        log.info("收到收藏事件: noteId={}, userId={}",
                event.getNoteId(),
                event.getUserId()
        );
        int rows = noteMapper.updateFavoriteCount(
                event.getNoteId()
        );
        if (rows == 0) {
            throw new RuntimeException("更新收藏数失败");
        }
        evictCache(event.getNoteId());
        syncToEs(event.getNoteId());
        log.info("更新笔记收藏数成功: noteId={}",
                event.getNoteId());
    }

    /**
     * 监听取消收藏事件
     */
    @RabbitListener(queues = "favorite.deleted.queue")
    @Transactional
    public void handleFavoriteDeleted(NoteFavoriteEvent event) {
        log.info("收到取消收藏事件: noteId={}, userId={}",
                event.getNoteId(), event.getUserId());
        int rows = noteMapper.dreaseFavoriteCount(event.getNoteId());
        log.info("数据库更新结果: noteId={}, affectedRows={}", event.getNoteId(), rows);
        evictCache(event.getNoteId());
        syncToEs(event.getNoteId());
        log.info("更新笔记收藏数成功: noteId={}", event.getNoteId());
        if (rows == 0) {
            throw new RuntimeException("更新收藏数失败");
        }

    }

    /**
     * 监听评分事件
     */
    @RabbitListener(queues = "rating.updated.queue")
    public void handleRatingUpdated(NoteRatingEvent event) {
        log.info("收到评分事件: noteId={}, avg={}, count={}",
                event.getNoteId(), event.getAverageScore(), event.getRatingCount());
        int rows = noteMapper.updateRatingStats(event.getNoteId(),
                event.getAverageScore());
        evictCache(event.getNoteId());
        syncToEs(event.getNoteId());
        log.info("更新笔记评分统计成功: noteId={}, avg={}, count={}",
                event.getNoteId(), event.getAverageScore(), event.getRatingCount());
        if (rows == 0) {
            throw new RuntimeException("更新笔记评分失败");
        }

    }

    /**
     * 监听分享事件
     */
    @RabbitListener(queues = "share.created.queue")
    public void handleShareCreated(NoteShareEvent event) {

        log.info("收到分享事件: noteId={}, userId={}",
                event.getNoteId(), event.getUserId());
        int rows = noteMapper.updateShareCount(event.getNoteId());
        evictCache(event.getNoteId());
        syncToEs(event.getNoteId());
        log.info("更新笔记分享数成功: noteId={}", event.getNoteId());
        if (rows == 0) {
            throw new RuntimeException("更新笔记分享数失败");
        }

    }
}
