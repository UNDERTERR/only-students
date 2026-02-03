package com.onlystudents.search.controller;

import com.onlystudents.common.core.result.Result;
import com.onlystudents.search.dto.NoteSearchResult;
import com.onlystudents.search.dto.SearchResult;
import com.onlystudents.search.dto.UserSearchResult;
import com.onlystudents.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "搜索管理", description = "搜索笔记、搜索用户等接口")
public class SearchController {
    
    private final SearchService searchService;
    
    @GetMapping("/notes")
    @Operation(summary = "搜索笔记", description = "根据关键词、学科、教育阶段等条件搜索笔记")
    public Result<SearchResult<NoteSearchResult>> searchNotes(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) Integer educationLevel,
            @RequestParam(required = false) Integer priceType,
            @RequestParam(defaultValue = "0") Integer sortType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(searchService.searchNotes(keyword, subjectId, educationLevel, priceType, sortType, page, size));
    }
    
    @GetMapping("/users")
    @Operation(summary = "搜索用户", description = "根据关键词、教育阶段等条件搜索用户")
    public Result<SearchResult<UserSearchResult>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer educationLevel,
            @RequestParam(required = false) Integer isCreator,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(searchService.searchUsers(keyword, educationLevel, isCreator, page, size));
    }
    
    @GetMapping("/notes/by-tag")
    @Operation(summary = "按标签搜索笔记", description = "根据标签搜索相关笔记")
    public Result<SearchResult<NoteSearchResult>> searchNotesByTag(
            @RequestParam String tag,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(searchService.searchNotesByTag(tag, page, size));
    }
    
    @GetMapping("/hot-keywords")
    @Operation(summary = "获取热门关键词", description = "获取当前热门搜索关键词")
    public Result<SearchResult<String>> getHotKeywords(
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(searchService.getHotKeywords(limit));
    }
    
    @GetMapping("/suggestions")
    @Operation(summary = "获取搜索建议", description = "根据输入前缀获取搜索建议")
    public Result<SearchResult<String>> getSuggestions(
            @RequestParam String prefix,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(searchService.getSuggestions(prefix, limit));
    }
}
