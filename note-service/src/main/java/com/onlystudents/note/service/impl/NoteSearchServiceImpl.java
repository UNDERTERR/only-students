package com.onlystudents.note.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.onlystudents.note.elasticsearch.NoteDocument;
import com.onlystudents.note.entity.Note;
import com.onlystudents.note.service.NoteSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 笔记搜索服务实现
 * 使用 Elasticsearch 进行笔记索引和搜索
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteSearchServiceImpl implements NoteSearchService {
    
    private final ElasticsearchClient elasticsearchClient;
    
    private static final String INDEX_NAME = "notes";
    
    @Override
    public void saveNote(NoteDocument noteDocument) {
        log.info("保存笔记到ES: noteId={}", noteDocument.getNoteId());
        try {
            IndexRequest<NoteDocument> request = IndexRequest.of(i -> i
                    .index(INDEX_NAME)
                    .id(String.valueOf(noteDocument.getNoteId()))
                    .document(noteDocument)
            );
            elasticsearchClient.index(request);
            log.info("笔记已保存到ES: noteId={}", noteDocument.getNoteId());
        } catch (IOException e) {
            log.error("保存笔记到ES失败: noteId={}", noteDocument.getNoteId(), e);
            throw new RuntimeException("ES索引失败", e);
        }
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
        try {
            DeleteRequest request = DeleteRequest.of(d -> d
                    .index(INDEX_NAME)
                    .id(String.valueOf(noteId))
            );
            elasticsearchClient.delete(request);
            log.info("笔记已从ES删除: noteId={}", noteId);
        } catch (IOException e) {
            log.error("从ES删除笔记失败: noteId={}", noteId, e);
            throw new RuntimeException("ES删除失败", e);
        }
    }
}
