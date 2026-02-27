package com.onlystudents.search.service;

import com.onlystudents.search.dto.NoteSearchResult;
import com.onlystudents.search.dto.SearchResult;
import com.onlystudents.search.dto.UserSearchResult;

public interface SearchService {
    
    SearchResult<NoteSearchResult> searchNotes(String keyword, Integer educationLevel, 
                                               Integer priceType, Integer sortType, Integer page, Integer size);
    
    SearchResult<UserSearchResult> searchUsers(String keyword, Integer educationLevel, 
                                              Integer isCreator, Integer page, Integer size);
    
    SearchResult<NoteSearchResult> searchNotesByTag(String tag, Integer page, Integer size);
    
    SearchResult<NoteSearchResult> searchNotesBySchool(Long schoolId, Integer page, Integer size);
    
    SearchResult<String> getHotKeywords(Integer limit);
    
    SearchResult<String> getSuggestions(String prefix, Integer limit);
}
