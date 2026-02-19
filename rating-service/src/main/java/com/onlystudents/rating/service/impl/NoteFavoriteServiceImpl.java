package com.onlystudents.rating.service.impl;

import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.Result;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.rating.client.NoteFeignClient;
import com.onlystudents.rating.dto.CreateFolderRequest;
import com.onlystudents.rating.dto.FavoriteFolderDTO;
import com.onlystudents.rating.dto.NoteFavoriteDTO;
import com.onlystudents.rating.entity.FavoriteFolder;
import com.onlystudents.rating.entity.NoteFavorite;
import com.onlystudents.rating.event.NoteFavoriteEvent;
import com.onlystudents.rating.mapper.FavoriteFolderMapper;
import com.onlystudents.rating.mapper.NoteFavoriteMapper;
import com.onlystudents.rating.service.NoteFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteFavoriteServiceImpl implements NoteFavoriteService {
    
    private final NoteFavoriteMapper favoriteMapper;
    private final FavoriteFolderMapper folderMapper;
    private final RabbitTemplate rabbitTemplate;
    private final NoteFeignClient noteFeignClient;
    
    @Override
    @Transactional
    public Result<Void> favoriteNote(Long noteId, Long userId, Long folderId) {
        // 检查是否已收藏
        NoteFavorite exist = favoriteMapper.selectByNoteAndUser(noteId, userId);
        if (exist != null) {
            // 已收藏，更新收藏夹
            if (folderId != null) {
                exist.setFolderId(folderId);
                favoriteMapper.updateById(exist);
            }
            return Result.success();
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
        
        // 获取所有笔记ID
        List<Long> noteIds = list.stream().map(NoteFavorite::getNoteId).collect(Collectors.toList());
        
        // 批量获取笔记信息
        Map<Long, Map<String, Object>> noteInfoMap = new java.util.HashMap<>();
        if (!noteIds.isEmpty()) {
            try {
                String idsStr = noteIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                Result<List<Map<String, Object>>> noteResult = noteFeignClient.getNotesByIds(idsStr);
                if (noteResult != null && noteResult.getData() != null) {
                    for (Map<String, Object> note : noteResult.getData()) {
                        Object idObj = note.get("id");
                        if (idObj instanceof Number) {
                            noteInfoMap.put(((Number) idObj).longValue(), note);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("获取笔记信息失败", e);
            }
        }
        
        final Map<Long, Map<String, Object>> finalNoteInfoMap = noteInfoMap;
        
        // 转换并填充笔记信息
        List<NoteFavoriteDTO> dtoList = list.stream().map(favorite -> {
            NoteFavoriteDTO dto = new NoteFavoriteDTO();
            BeanUtils.copyProperties(favorite, dto);
            
            // 填充笔记信息
            Map<String, Object> noteInfo = finalNoteInfoMap.get(favorite.getNoteId());
            if (noteInfo != null) {
                dto.setTitle(getStringValue(noteInfo, "title"));
                dto.setCoverImage(getStringValue(noteInfo, "coverImage"));
                dto.setAuthorNickname(getStringValue(noteInfo, "authorNickname"));
                dto.setAuthorAvatar(getStringValue(noteInfo, "authorAvatar"));
                dto.setViewCount(getIntValue(noteInfo, "viewCount"));
                dto.setFavoriteCount(getIntValue(noteInfo, "favoriteCount"));
                dto.setCommentCount(getIntValue(noteInfo, "commentCount"));
                dto.setAverageRating(getBigDecimalValue(noteInfo, "averageRating"));
                dto.setRatingCount(getIntValue(noteInfo, "ratingCount"));
                dto.setTags((List<String>) noteInfo.get("tags"));
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        return Result.success(dtoList);
    }
    
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }
    
    private java.math.BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return java.math.BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return null;
    }
    
private NoteFavoriteDTO convertToDTO(NoteFavorite favorite) {
        NoteFavoriteDTO dto = new NoteFavoriteDTO();
        BeanUtils.copyProperties(favorite, dto);
        return dto;
    }
    
    @Override
    public Result<List<NoteFavoriteDTO>> getUserFavorites(Long userId, Long folderId) {
        List<NoteFavorite> list;
        if (folderId == null) {
            list = favoriteMapper.selectListByUser(userId);
        } else {
            list = favoriteMapper.selectListByUserAndFolder(userId, folderId);
        }
        
        return convertToFavoriteDTOList(list);
    }
    
    @Override
    public Result<List<FavoriteFolderDTO>> getUserFolders(Long userId) {
        log.info("getUserFolders called, userId: {}", userId);
        List<FavoriteFolder> folders = folderMapper.selectListByUser(userId);
        log.info("Found folders: {}, data: {}", folders.size(), folders);
        
        // 构建"全部"选项
        FavoriteFolderDTO allFolder = new FavoriteFolderDTO();
        allFolder.setId(null);
        allFolder.setName("全部");
        allFolder.setIsDefault(1);
        
        Long totalCount = favoriteMapper.countByUserId(userId);
        allFolder.setCount(totalCount.intValue());
        
        List<FavoriteFolderDTO> result = new java.util.ArrayList<>();
        result.add(allFolder);
        
        for (FavoriteFolder folder : folders) {
            FavoriteFolderDTO dto = new FavoriteFolderDTO();
            BeanUtils.copyProperties(folder, dto);
            
            // 查询每个收藏夹的收藏数量
            Long count = favoriteMapper.countByUserAndFolder(userId, folder.getId());
            dto.setCount(count.intValue());
            
            result.add(dto);
        }
        
        return Result.success(result);
    }
    
    @Override
    public Result<FavoriteFolderDTO> createFolder(Long userId, CreateFolderRequest request) {
        FavoriteFolder folder = new FavoriteFolder();
        folder.setUserId(userId);
        folder.setName(request.getName());
        folder.setDescription(request.getDescription());
        folder.setCount(0);
        folder.setIsDefault(0);
        folder.setSortOrder(0);
        
        folderMapper.insert(folder);
        
        FavoriteFolderDTO dto = new FavoriteFolderDTO();
        BeanUtils.copyProperties(folder, dto);
        
        return Result.success(dto);
    }
    
    @Override
    public Result<Void> moveFavorite(Long favoriteId, Long userId, Long folderId) {
        NoteFavorite favorite = favoriteMapper.selectById(favoriteId);
        if (favorite == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "收藏记录不存在");
        }
        
        if (!favorite.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限操作");
        }
        
        favorite.setFolderId(folderId);
        favoriteMapper.updateById(favorite);
        
        return Result.success();
    }
    
    @Override
    public Result<Void> moveFavoriteByNoteId(Long noteId, Long userId, Long folderId) {
        NoteFavorite favorite = favoriteMapper.selectByNoteAndUser(noteId, userId);
        if (favorite == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "收藏记录不存在");
        }
        
        favorite.setFolderId(folderId);
        favoriteMapper.updateById(favorite);
        
        return Result.success();
    }
    
    private Result<List<NoteFavoriteDTO>> convertToFavoriteDTOList(List<NoteFavorite> list) {
        if (list == null || list.isEmpty()) {
            return Result.success(java.util.Collections.emptyList());
        }
        
        List<Long> noteIds = list.stream().map(NoteFavorite::getNoteId).collect(Collectors.toList());
        
        Map<Long, Map<String, Object>> noteInfoMap = new java.util.HashMap<>();
        if (!noteIds.isEmpty()) {
            try {
                String idsStr = noteIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                Result<List<Map<String, Object>>> noteResult = noteFeignClient.getNotesByIds(idsStr);
                if (noteResult != null && noteResult.getData() != null) {
                    for (Map<String, Object> note : noteResult.getData()) {
                        Object idObj = note.get("id");
                        if (idObj instanceof Number) {
                            noteInfoMap.put(((Number) idObj).longValue(), note);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("获取笔记信息失败", e);
            }
        }
        
        final Map<Long, Map<String, Object>> finalNoteInfoMap = noteInfoMap;
        
        List<NoteFavoriteDTO> dtoList = list.stream().map(favorite -> {
            NoteFavoriteDTO dto = new NoteFavoriteDTO();
            BeanUtils.copyProperties(favorite, dto);
            
            Map<String, Object> noteInfo = finalNoteInfoMap.get(favorite.getNoteId());
            if (noteInfo != null) {
                dto.setTitle(getStringValue(noteInfo, "title"));
                dto.setCoverImage(getStringValue(noteInfo, "coverImage"));
                dto.setAuthorNickname(getStringValue(noteInfo, "authorNickname"));
                dto.setAuthorAvatar(getStringValue(noteInfo, "authorAvatar"));
                dto.setViewCount(getIntValue(noteInfo, "viewCount"));
                dto.setFavoriteCount(getIntValue(noteInfo, "favoriteCount"));
                dto.setCommentCount(getIntValue(noteInfo, "commentCount"));
                dto.setAverageRating(getBigDecimalValue(noteInfo, "averageRating"));
                dto.setRatingCount(getIntValue(noteInfo, "ratingCount"));
                dto.setTags((List<String>) noteInfo.get("tags"));
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        return Result.success(dtoList);
    }
    
    @Override
    public Result<Void> deleteFolder(Long folderId, Long userId) {
        FavoriteFolder folder = folderMapper.selectById(folderId);
        if (folder == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "收藏夹不存在");
        }
        
        if (!folder.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限操作");
        }
        
        if (folder.getIsDefault() != null && folder.getIsDefault() == 1) {
            throw new BusinessException(ResultCode.SERVICE_ERROR, "默认收藏夹不能删除");
        }
        
        // 将该收藏夹下的笔记的folderId设为null，但不删除收藏记录
        favoriteMapper.clearFolderById(folderId, userId);
        
        // 删除收藏夹
        folderMapper.deleteById(folderId);
        
        return Result.success();
    }
}
