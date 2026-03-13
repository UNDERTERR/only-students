package com.onlystudents.note.service;

import com.onlystudents.note.dto.CreateNoteRequest;
import com.onlystudents.note.dto.NoteDTO;
import com.onlystudents.note.dto.NoteStatsDTO;
import com.onlystudents.note.dto.UpdateNoteRequest;

import java.util.List;

public interface NoteService {
    
    NoteDTO createNote(CreateNoteRequest request, Long userId);
    
    NoteDTO updateNote(Long noteId, UpdateNoteRequest request, Long userId);
    
    void deleteNote(Long noteId, Long userId);
    
    NoteDTO getNoteById(Long noteId);
    
    NoteDTO getNoteDetail(Long noteId, Long userId);
    
    List<NoteDTO> getHotNotes(Integer limit, Long currentUserId);
    
    List<NoteDTO> getLatestNotes(Integer limit, Long currentUserId);
    
    List<NoteDTO> getNotesBySchoolId(Long schoolId, Integer limit);
    
    List<NoteDTO> getSubscribedNotes(List<Long> creatorIds, Integer page, Integer limit);
    
    List<NoteDTO> getUserNotes(Long userId);
    
    void incrementViewCount(Long noteId);
    
    void publishNote(Long noteId, Long userId);
    
    List<Long> getNoteIdsByUserId(Long userId);
    
    java.util.Map<String, Object> getCreatorNoteStats(Long creatorId);
    
    NoteStatsDTO getNoteStats();
}
