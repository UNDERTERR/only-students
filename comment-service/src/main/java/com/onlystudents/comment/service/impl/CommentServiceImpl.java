package com.onlystudents.comment.service.impl;

import com.onlystudents.comment.client.NoteFeignClient;
import com.onlystudents.comment.client.UserFeignClient;
import com.onlystudents.comment.client.UserResponse;
import com.onlystudents.comment.dto.CommentDTO;
import com.onlystudents.comment.dto.CreateCommentRequest;
import com.onlystudents.comment.entity.Comment;
import com.onlystudents.comment.entity.CommentLike;
import com.onlystudents.comment.mapper.CommentLikeMapper;
import com.onlystudents.comment.mapper.CommentMapper;
import com.onlystudents.comment.service.CommentService;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.Result;
import com.onlystudents.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper likeMapper;
    private final UserFeignClient userFeignClient;
    private final NoteFeignClient noteFeignClient;
    
    @Override
    @Transactional
    public CommentDTO createComment(CreateCommentRequest request, Long userId) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(request, comment);
        comment.setUserId(userId);
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setStatus(1);
        comment.setIsTop(0);
        
        // 如果没有parentId，设为0
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }
        
        // 如果没有rootId，设为parentId
        if (comment.getRootId() == null) {
            comment.setRootId(comment.getParentId());
        }
        
        commentMapper.insert(comment);
        
        // 如果是回复，增加父评论的回复数
        if (comment.getParentId() != 0) {
            commentMapper.incrementReplyCount(comment.getParentId());
        }
        
        return convertToDTO(comment, userId);
    }
    
    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return;
        }
        
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        
        // 使用 MyBatis Plus 逻辑删除，设置 deleted=1
        commentMapper.deleteById(commentId);
    }
    
    @Override
    public List<CommentDTO> getNoteComments(Long noteId, Long currentUserId) {
        // 获取根评论
        List<Comment> rootComments = commentMapper.selectRootCommentsByNoteId(noteId);
        
        return rootComments.stream()
                .map(comment -> {
                    CommentDTO dto = convertToDTO(comment, currentUserId);
                    // 获取回复
                    if (comment.getReplyCount() > 0) {
                        List<Comment> replies = commentMapper.selectRepliesByRootId(comment.getId());
                        dto.setReplies(replies.stream()
                                .map(reply -> convertToDTO(reply, currentUserId))
                                .collect(Collectors.toList()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void likeComment(Long commentId, Long userId) {
        // 检查是否已点赞
        CommentLike exist = likeMapper.selectByCommentAndUser(commentId, userId);
        if (exist != null) {
            return;
        }
        
        CommentLike like = new CommentLike();
        like.setCommentId(commentId);
        like.setUserId(userId);
        likeMapper.insert(like);
        
        // 增加点赞数
        commentMapper.incrementLikeCount(commentId);
    }
    
    @Override
    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        CommentLike exist = likeMapper.selectByCommentAndUser(commentId, userId);
        if (exist == null) {
            return;
        }
        
        likeMapper.deleteById(exist.getId());
    }
    
    @Override
    public Integer getCommentCount(Long noteId) {
        return commentMapper.countByNoteId(noteId);
    }
    
    @Override
    public CommentDTO getCommentDetail(Long commentId, Long currentUserId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }
        
        CommentDTO dto = convertToDTO(comment, currentUserId);
        
        if (comment.getParentId() == 0 || comment.getParentId() == null) {
            List<Comment> allReplies = commentMapper.selectRepliesByRootId(commentId);
            if (allReplies != null && !allReplies.isEmpty()) {
                dto.setReplies(allReplies.stream()
                        .map(reply -> convertToDTO(reply, currentUserId))
                        .collect(Collectors.toList()));
            }
        } else {
            List<Comment> directReplies = commentMapper.selectDirectRepliesByParentId(commentId);
            if (directReplies != null && !directReplies.isEmpty()) {
                dto.setReplies(directReplies.stream()
                        .map(reply -> convertToDTO(reply, currentUserId))
                        .collect(Collectors.toList()));
            }
        }
        
        return dto;
    }
    
    @Override
    public List<CommentDTO> getReceivedComments(Long userId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        
        // 先获取用户的笔记ID列表
        java.util.Set<Long> userNoteIds = new java.util.HashSet<>();
        try {
            Result<java.util.List<Long>> noteIdsResult = noteFeignClient.getNoteIdsByUserId(userId);
            if (noteIdsResult != null && noteIdsResult.getData() != null) {
                userNoteIds.addAll(noteIdsResult.getData());
            }
        } catch (Exception e) {
            log.error("获取用户笔记ID列表失败", e);
        }
        
        if (userNoteIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        // 获取用户笔记的评论（排除自己发的）
        List<Comment> comments = commentMapper.selectReceivedComments(userId, new java.util.ArrayList<>(userNoteIds), offset, size);
        
        if (comments == null || comments.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        // 获取笔记信息
        final java.util.Map<Long, java.util.Map<String, Object>> noteInfoMap = new java.util.HashMap<>();
        List<Long> noteIds = comments.stream().map(Comment::getNoteId).distinct().collect(Collectors.toList());
        if (!noteIds.isEmpty()) {
            try {
                for (Long noteId : noteIds) {
                    Result<Map<String, Object>> result = noteFeignClient.getNoteById(noteId);
                    if (result != null && result.isSuccess() && result.getData() != null) {
                        noteInfoMap.put(noteId, result.getData());
                    }
                }
            } catch (Exception e) {
                log.error("获取笔记信息失败", e);
            }
        }
        
        final java.util.Map<Long, java.util.Map<String, Object>> finalNoteInfoMap = noteInfoMap;
        
        return comments.stream()
                .map(comment -> {
                    CommentDTO dto = convertToDTO(comment, userId);
                    // 填充笔记信息
                    java.util.Map<String, Object> noteInfo = finalNoteInfoMap.get(comment.getNoteId());
                    if (noteInfo != null) {
                        CommentDTO.NoteInfo noteDTO = new CommentDTO.NoteInfo();
                        noteDTO.setId(((Number) noteInfo.get("id")).longValue());
                        noteDTO.setTitle((String) noteInfo.get("title"));
                        noteDTO.setCoverUrl((String) noteInfo.get("coverImage"));
                        dto.setNote(noteDTO);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CommentDTO> getSentComments(Long userId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<Comment> comments = commentMapper.selectSentComments(userId, offset, size);
        
        if (comments == null || comments.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        return comments.stream()
                .map(comment -> convertToDTO(comment, userId))
                .collect(Collectors.toList());
    }
    
    @Override
    public Integer getReceivedCommentUnreadCount(Long userId) {
        // 先获取用户的笔记ID列表
        try {
            Result<List<Long>> noteIdsResult = noteFeignClient.getNoteIdsByUserId(userId);
            if (noteIdsResult != null && noteIdsResult.getData() != null && !noteIdsResult.getData().isEmpty()) {
                // 获取这些笔记的所有未读评论数（排除自己发的）
                return commentMapper.countReceivedCommentsUnread(userId, noteIdsResult.getData());
            }
        } catch (Exception e) {
            log.error("获取用户笔记ID列表失败", e);
        }
        return 0;
    }

    @Override
    public void markCommentAsRead(Long commentId) {
        commentMapper.markAsRead(commentId);
    }
    
    private CommentDTO convertToDTO(Comment comment, Long currentUserId) {
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);

        // 检查当前用户是否点赞
        if (currentUserId != null) {
            CommentLike like = likeMapper.selectByCommentAndUser(comment.getId(), currentUserId);
            dto.setIsLiked(like != null);
        } else {
            dto.setIsLiked(false);
        }

        // 通过 Feign 调用 User-Service 查询用户信息
        try {
            Result<UserResponse> result = userFeignClient.getUserById(comment.getUserId());
            if (result != null && result.isSuccess() && result.getData() != null) {
                UserResponse user = result.getData();
                dto.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
                dto.setAvatar(user.getAvatar() != null ? user.getAvatar() : "");
            } else {
                dto.setUsername("用户_" + comment.getUserId());
                dto.setAvatar("");
            }
        } catch (Exception e) {
            log.error("查询用户信息失败，commentId={}, userId={}", comment.getId(), comment.getUserId(), e);
            dto.setUsername("用户_" + comment.getUserId());
            dto.setAvatar("");
        }

        return dto;
    }
}
