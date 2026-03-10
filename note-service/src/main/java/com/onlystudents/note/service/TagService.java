package com.onlystudents.note.service;

import com.onlystudents.note.entity.NoteTag;
import com.onlystudents.note.entity.NoteTagRelation;
import com.onlystudents.note.mapper.NoteTagMapper;
import com.onlystudents.note.mapper.NoteTagRelationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

    private final NoteTagMapper noteTagMapper;
    private final NoteTagRelationMapper noteTagRelationMapper;

    /**
     * 获取笔记的标签列表
     */
    public List<String> getNoteTags(Long noteId) {
        return noteTagRelationMapper.selectTagNamesByNoteId(noteId);
    }

    /**
     * 批量获取多个笔记的标签
     * @param noteIds 笔记ID列表
     * @return Map<noteId, 标签列表>
     */
    public java.util.Map<Long, List<String>> getNoteTagsBatch(List<Long> noteIds) {
        if (noteIds == null || noteIds.isEmpty()) {
            return new java.util.HashMap<>();
        }
        List<NoteTagRelationMapper.NoteTagVO> tagVOs = noteTagRelationMapper.selectTagNamesByNoteIds(noteIds);
        
        java.util.Map<Long, List<String>> result = new java.util.HashMap<>();
        for (NoteTagRelationMapper.NoteTagVO vo : tagVOs) {
            result.computeIfAbsent(vo.getNoteId(), k -> new ArrayList<>()).add(vo.getName());
        }
        return result;
    }

    /**
     * 设置笔记的标签（会先删除旧标签）
     */
    @Transactional
    public void setNoteTags(Long noteId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            // 删除所有旧标签关联
            deleteNoteTags(noteId);
            return;
        }

        // 1. 获取或创建标签
        List<Long> tagIds = new ArrayList<>();
        for (String tagName : tagNames) {
            if (tagName == null || tagName.trim().isEmpty()) {
                continue;
            }
            
            String trimmedName = tagName.trim();
            NoteTag tag = noteTagMapper.selectByName(trimmedName);
            
            if (tag == null) {
                // 创建新标签
                tag = new NoteTag();
                tag.setName(trimmedName);
                tag.setUsageCount(1);
                tag.setStatus(1);
                noteTagMapper.insert(tag);
            } else {
                // 增加使用次数
                noteTagMapper.incrementUsageCount(tag.getId());
            }
            
            tagIds.add(tag.getId());
        }

        // 2. 删除旧关联
        noteTagRelationMapper.deleteByNoteId(noteId);

        // 3. 建立新关联
        if (!tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                NoteTagRelation relation = new NoteTagRelation();
                relation.setNoteId(noteId);
                relation.setTagId(tagId);
                noteTagRelationMapper.insert(relation);
            }
        }
    }

    /**
     * 删除笔记的所有标签关联
     */
    @Transactional
    public void deleteNoteTags(Long noteId) {
        // 获取该笔记的所有标签ID
        List<Long> tagIds = noteTagRelationMapper.selectTagIdsByNoteId(noteId);
        
        // 减少标签使用次数
        for (Long tagId : tagIds) {
            noteTagMapper.decrementUsageCount(tagId);
        }
        
        // 删除关联
        noteTagRelationMapper.deleteByNoteId(noteId);
    }

    /**
     * 获取热门标签
     */
    public List<String> getHotTags(Integer limit) {
        List<NoteTag> tags = noteTagMapper.selectHotTags(limit);
        return tags.stream()
                .map(NoteTag::getName)
                .collect(Collectors.toList());
    }
}
