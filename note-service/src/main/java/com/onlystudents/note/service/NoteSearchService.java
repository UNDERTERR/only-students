package com.onlystudents.note.service;

import com.onlystudents.note.elasticsearch.NoteDocument;
import com.onlystudents.note.entity.Note;

public interface NoteSearchService {
    
    void saveNote(NoteDocument noteDocument);
    
    void updateNote(Note note);
    
    void deleteNote(Long noteId);
}
