package com.onlystudents.rating.service.impl;

import cn.hutool.core.util.IdUtil;
import com.onlystudents.common.event.note.NoteShareEvent;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.Result;
import com.onlystudents.common.result.ResultCode;
import com.onlystudents.rating.dto.NoteShareDTO;
import com.onlystudents.rating.entity.NoteShare;
import com.onlystudents.rating.mapper.NoteShareMapper;
import com.onlystudents.rating.service.NoteShareService;
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
public class NoteShareServiceImpl implements NoteShareService {
    
    private final NoteShareMapper shareMapper;
    private final RabbitTemplate rabbitTemplate;
    
    @Override
    @Transactional
    public Result<NoteShareDTO> createShare(Long noteId, Long userId, Integer shareType) {
        // 生成分享码（8位随机字符串）
        String shareCode = IdUtil.simpleUUID().substring(0, 8);
        
        // 创建分享记录
        NoteShare share = new NoteShare();
        share.setNoteId(noteId);
        share.setUserId(userId);
        share.setShareType(shareType);
        share.setShareCode(shareCode);
        share.setClickCount(0);
        shareMapper.insert(share);

        // 发送事件
        NoteShareEvent event = new NoteShareEvent(noteId, userId, shareType, shareCode);
        rabbitTemplate.convertAndSend("rating.exchange", "share.created", event);
        log.info("发送分享事件: noteId={}, userId={}, shareCode={}",
                noteId, userId, shareCode);
        
        NoteShareDTO dto = new NoteShareDTO();
        BeanUtils.copyProperties(share, dto);
        return Result.success(dto);
    }
    
    @Override
    public Result<NoteShareDTO> getShareByCode(String shareCode) {
        NoteShare share = shareMapper.selectByShareCode(shareCode);
        if (share == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分享记录不存在");
        }
        NoteShareDTO dto = new NoteShareDTO();
        BeanUtils.copyProperties(share, dto);
        return Result.success(dto);
    }
    
    @Override
    @Transactional
    public Result<Void> incrementClickCount(String shareCode) {
        NoteShare share = shareMapper.selectByShareCode(shareCode);
        if (share != null) {
            shareMapper.incrementClickCount(share.getId());
            log.info("分享链接点击+1: shareCode={}", shareCode);
        }
        return Result.success();
    }
    
    @Override
    public Result<Long> getShareCount(Long noteId) {
        Long count = shareMapper.countByNoteId(noteId);
        return Result.success(count);
    }
    
    @Override
    public Result<List<NoteShareDTO>> getUserShares(Long userId) {
        List<NoteShare> list = shareMapper.selectListByUser(userId);
        List<NoteShareDTO> dtoList = list.stream().map(this::convertToDTO).collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    private NoteShareDTO convertToDTO(NoteShare share) {
        NoteShareDTO dto = new NoteShareDTO();
        BeanUtils.copyProperties(share, dto);
        return dto;
    }
}
