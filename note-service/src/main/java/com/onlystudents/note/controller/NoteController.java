package com.onlystudents.note.controller;

import com.onlystudents.common.result.Result;
import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.note.dto.CreateNoteRequest;
import com.onlystudents.note.dto.NoteDTO;
import com.onlystudents.note.dto.UpdateNoteRequest;
import com.onlystudents.note.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/note")
@RequiredArgsConstructor
@Tag(name = "笔记管理", description = "笔记的创建、编辑、发布、查询等接口")
public class NoteController {
    
    private final NoteService noteService;
    
    @PostMapping
    @Operation(summary = "创建笔记", description = "创建新笔记草稿")
    public Result<NoteDTO> createNote(@RequestBody CreateNoteRequest request,
                                       @RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.success(noteService.createNote(request, userId));
    }
    
    @PutMapping("/{noteId}")
    @Operation(summary = "更新笔记", description = "更新笔记内容")
    public Result<NoteDTO> updateNote(@PathVariable(name = "noteId") Long noteId,
                                       @RequestBody UpdateNoteRequest request,
                                       @RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.success(noteService.updateNote(noteId, request, userId));
    }
    
    @DeleteMapping("/{noteId}")
    @Operation(summary = "删除笔记", description = "逻辑删除笔记")
    public Result<Void> deleteNote(@PathVariable(name = "noteId") Long noteId,
                                    @RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        noteService.deleteNote(noteId, userId);
        return Result.success();
    }
    
    @GetMapping("/{noteId}")
    @Operation(summary = "获取笔记详情", description = "获取单个笔记的详细信息")
    public Result<NoteDTO> getNoteDetail(@PathVariable(name = "noteId") Long noteId,
                                          @RequestHeader(value = CommonConstants.USER_ID_HEADER, required = false) Long userId) {
        if (userId != null) {
            return Result.success(noteService.getNoteDetail(noteId, userId));
        }
        return Result.success(noteService.getNoteById(noteId));
    }
    
    @PostMapping("/{noteId}/publish")
    @Operation(summary = "发布笔记", description = "将草稿状态笔记发布")
    public Result<Void> publishNote(@PathVariable(name = "noteId") Long noteId,
                                     @RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        noteService.publishNote(noteId, userId);
        return Result.success();
    }
    
    @GetMapping("/hot")
    @Operation(summary = "热门笔记", description = "按热度排序获取笔记列表")
    public Result<List<NoteDTO>> getHotNotes(@RequestParam(name = "limit", defaultValue = "20") Integer limit) {
        return Result.success(noteService.getHotNotes(limit));
    }
    
    @GetMapping("/latest")
    @Operation(summary = "最新笔记", description = "按时间排序获取最新笔记")
    public Result<List<NoteDTO>> getLatestNotes(@RequestParam(name = "limit", defaultValue = "20") Integer limit) {
        return Result.success(noteService.getLatestNotes(limit));
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "用户笔记列表", description = "获取指定用户的所有笔记")
    public Result<List<NoteDTO>> getUserNotes(@PathVariable(name = "userId") Long userId) {
        return Result.success(noteService.getUserNotes(userId));
    }
    
    @GetMapping("/my-notes")
    @Operation(summary = "我的笔记", description = "获取当前登录用户的所有笔记")
    public Result<List<NoteDTO>> getMyNotes(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.success(noteService.getUserNotes(userId));
    }
}
