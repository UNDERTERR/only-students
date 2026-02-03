package com.onlystudents.note.service.impl;

import com.onlystudents.note.elasticsearch.NoteDocument;
import com.onlystudents.note.entity.Note;
import com.onlystudents.note.service.NoteSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteSearchServiceImpl implements NoteSearchService {
    
    @Override
    public void saveNote(NoteDocument noteDocument) {
        log.info("保存笔记到ES: noteId={}", noteDocument.getNoteId());
        // TODO: 集成Elasticsearch
    }
    
    @Override
    public void updateNote(Note note) {
        log.info("更新笔记到ES: noteId={}", note.getId());
        NoteDocument document = new NoteDocument();
        BeanUtils.copyProperties(note, document);
        document.setNoteId(note.getId());
        saveNote(document);
    }
    
    @Override
    public void deleteNote(Long noteId) {
        log.info("从ES删除笔记: noteId={}", noteId);
        // TODO: 集成Elasticsearch
    }
}
