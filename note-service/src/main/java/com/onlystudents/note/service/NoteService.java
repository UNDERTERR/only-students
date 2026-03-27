package com.onlystudents.note.service;

import com.onlystudents.note.dto.CreateNoteRequest;
import com.onlystudents.note.dto.NoteDTO;
import com.onlystudents.note.dto.NoteStatsDTO;
import com.onlystudents.note.dto.UpdateNoteRequest;

import java.util.List;
import java.util.Map;

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
    
    Map<String, Object> getCreatorNoteStats(Long creatorId);
    
    NoteStatsDTO getNoteStats();
    
    List<NoteDTO> getPendingAuditNotes(Integer page, Integer size);
    
    Long getPendingAuditCount();
    
    void auditPass(Long noteId, Long adminId);
    
    void auditReject(Long noteId, String reason, Long adminId);
    
    void setNoteToDraft(Long noteId);
    
    Map<String, Object> getNoteListForAdmin(Integer page, Integer size, Integer status, String keyword);
    
    void deleteNoteByAdmin(Long noteId);
    
    void incrementViewCountByAdmin(Long noteId, Integer count);
}
