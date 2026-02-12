package com.onlystudents.rating.service.impl;

import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.Result;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.rating.dto.NoteFavoriteDTO;
import com.onlystudents.rating.entity.NoteFavorite;
import com.onlystudents.rating.event.NoteFavoriteEvent;
import com.onlystudents.rating.mapper.NoteFavoriteMapper;
import com.onlystudents.rating.service.NoteFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteFavoriteServiceImpl implements NoteFavoriteService {
    
    private final NoteFavoriteMapper favoriteMapper;
    private final RabbitTemplate rabbitTemplate;
    
    @Override
    @Transactional
    public Result<Void> favoriteNote(Long noteId, Long userId, Long folderId) {
        // 检查是否已收藏
        NoteFavorite exist = favoriteMapper.selectByNoteAndUser(noteId, userId);
        if (exist != null) {
            return Result.success(); // 已收藏，直接返回成功
        }
        
        // 创建收藏记录
        NoteFavorite favorite = new NoteFavorite();
        favorite.setNoteId(noteId);
        favorite.setUserId(userId);
        favorite.setFolderId(folderId);
        favoriteMapper.insert(favorite);
        
        // 获取当前收藏总数
        Long totalCount = favoriteMapper.countByNoteId(noteId);
        
        // 发送事件
        NoteFavoriteEvent event = new NoteFavoriteEvent(noteId, userId, 1, totalCount);
        rabbitTemplate.convertAndSend("rating.exchange", "favorite.created", event);
        log.info("发送收藏事件: noteId={}, userId={}, total={}", noteId, userId, totalCount);
        
        return Result.success();
    }
    
    @Override
    @Transactional
    public Result<Void> unfavoriteNote(Long noteId, Long userId) {
        // 查询收藏记录
        NoteFavorite favorite = favoriteMapper.selectByNoteAndUser(noteId, userId);
        if (favorite == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "收藏记录不存在");
        }
        
        // 删除收藏记录
        favoriteMapper.deleteById(favorite.getId());
        
        // 获取当前收藏总数
        Long totalCount = favoriteMapper.countByNoteId(noteId);
        
        // 发送事件
        NoteFavoriteEvent event = new NoteFavoriteEvent(noteId, userId, 0, totalCount);
        rabbitTemplate.convertAndSend("rating.exchange", "favorite.deleted", event);
        log.info("发送取消收藏事件: noteId={}, userId={}, total={}", noteId, userId, totalCount);
        
        return Result.success();
    }
    
    @Override
    public Result<Boolean> isFavorited(Long noteId, Long userId) {
        NoteFavorite favorite = favoriteMapper.selectByNoteAndUser(noteId, userId);
        return Result.success(favorite != null);
    }
    
    @Override
    public Result<Long> getFavoriteCount(Long noteId) {
        Long count = favoriteMapper.countByNoteId(noteId);
        return Result.success(count);
    }
    
    @Override
    public Result<List<NoteFavoriteDTO>> getUserFavorites(Long userId) {
        List<NoteFavorite> list = favoriteMapper.selectListByUser(userId);
        List<NoteFavoriteDTO> dtoList = list.stream().map(this::convertToDTO).collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    private NoteFavoriteDTO convertToDTO(NoteFavorite favorite) {
        NoteFavoriteDTO dto = new NoteFavoriteDTO();
        BeanUtils.copyProperties(favorite, dto);
        return dto;
    }
}
