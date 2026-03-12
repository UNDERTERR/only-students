package com.onlystudents.note.listener;

import com.onlystudents.common.event.note.NoteRatingEvent;
import com.onlystudents.common.event.note.NoteFavoriteEvent;
import com.onlystudents.common.event.note.NoteShareEvent;
import com.onlystudents.note.mapper.NoteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RatingEventListener {

    private final NoteMapper noteMapper;
    private final CacheManager cacheManager;
    private final RabbitTemplate rabbitTemplate;

    private void evictCache(Long noteId) {
        if (noteId != null && cacheManager != null) {
            // noteDetail: 二级缓存（Caffeine + Redis）
            evictSingleCache("noteDetail", noteId);
            // hotNotes: 一级缓存（仅Caffeine）
            evictSingleCache("hotNotes", noteId);
            // latestNotes: 一级缓存（仅Caffeine）
            evictSingleCache("latestNotes", noteId);
        }
    }

    private void evictSingleCache(String cacheName, Object key) {
        try {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
                log.debug("清除缓存: cacheName={}, key={}", cacheName, key);
            }
        } catch (Exception e) {
            log.warn("清除缓存失败: cacheName={}, key={}", cacheName, key, e);
        }
    }

    private void syncToEs(Long noteId) {
        try {
            rabbitTemplate.convertAndSend("note.exchange", "note.sync", noteId);
            log.info("发送ES同步消息成功: noteId={}", noteId);
        } catch (Exception e) {
            log.error("发送ES同步消息失败: noteId={}", noteId, e);
        }
    }

    @RabbitListener(queues = "favorite.created.queue")
    @Transactional
    public void handleFavoriteCreated(NoteFavoriteEvent event) {
        log.info("收到收藏事件: noteId={}, userId={}", event.getNoteId(), event.getUserId());
        try {
            int rows = noteMapper.updateFavoriteCount(event.getNoteId());
            if (rows == 0) {
                log.warn("笔记不存在，无法更新收藏数: noteId={}", event.getNoteId());
                return;
            }
            evictCache(event.getNoteId());
            syncToEs(event.getNoteId());
            log.info("更新笔记收藏数成功: noteId={}", event.getNoteId());
        } catch (Exception e) {
            log.error("处理收藏事件失败: noteId={}", event.getNoteId(), e);
        }
    }

    @RabbitListener(queues = "favorite.deleted.queue")
    @Transactional
    public void handleFavoriteDeleted(NoteFavoriteEvent event) {
        log.info("收到取消收藏事件: noteId={}, userId={}", event.getNoteId(), event.getUserId());
        try {
            int rows = noteMapper.dreaseFavoriteCount(event.getNoteId());
            if (rows == 0) {
                log.warn("笔记不存在或收藏数已为0: noteId={}", event.getNoteId());
                return;
            }
            evictCache(event.getNoteId());
            syncToEs(event.getNoteId());
            log.info("更新笔记收藏数成功: noteId={}", event.getNoteId());
        } catch (Exception e) {
            log.error("处理取消收藏事件失败: noteId={}", event.getNoteId(), e);
        }
    }

    @RabbitListener(queues = "rating.updated.queue")
    public void handleRatingUpdated(NoteRatingEvent event) {
        log.info("收到评分事件: noteId={}, avg={}, count={}", event.getNoteId(), event.getAverageScore(), event.getRatingCount());
        try {
            int rows = noteMapper.updateRatingStats(event.getNoteId(), event.getAverageScore());
            if (rows == 0) {
                log.warn("笔记不存在，无法更新评分: noteId={}", event.getNoteId());
                return;
            }
            evictCache(event.getNoteId());
            syncToEs(event.getNoteId());
            log.info("更新笔记评分统计成功: noteId={}, avg={}, count={}", event.getNoteId(), event.getAverageScore(), event.getRatingCount());
        } catch (Exception e) {
            log.error("处理评分事件失败: noteId={}", event.getNoteId(), e);
        }
    }

    @RabbitListener(queues = "share.created.queue")
    public void handleShareCreated(NoteShareEvent event) {
        log.info("收到分享事件: noteId={}, userId={}", event.getNoteId(), event.getUserId());
        try {
            int rows = noteMapper.updateShareCount(event.getNoteId());
            if (rows == 0) {
                log.warn("笔记不存在，无法更新分享数: noteId={}", event.getNoteId());
                return;
            }
            evictCache(event.getNoteId());
            syncToEs(event.getNoteId());
            log.info("更新笔记分享数成功: noteId={}", event.getNoteId());
        } catch (Exception e) {
            log.error("处理分享事件失败: noteId={}", event.getNoteId(), e);
        }
    }
}
