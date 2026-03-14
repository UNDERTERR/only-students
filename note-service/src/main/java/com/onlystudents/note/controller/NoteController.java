package com.onlystudents.note.controller;

import com.onlystudents.common.constants.CommonConstants;
import com.onlystudents.common.exception.BusinessException;
import com.onlystudents.common.result.Result;
import com.onlystudents.note.client.SubscriptionFeignClient;
import com.onlystudents.note.dto.CreateNoteRequest;
import com.onlystudents.note.dto.NoteDTO;
import com.onlystudents.note.dto.NoteStatsDTO;
import com.onlystudents.note.dto.UpdateNoteRequest;
import com.onlystudents.note.entity.Note;
import com.onlystudents.note.mapper.NoteMapper;
import com.onlystudents.note.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/note")
@RequiredArgsConstructor
@Tag(name = "笔记管理", description = "笔记的创建、编辑、发布、查询等接口")
public class NoteController {

    private final NoteService noteService;
    private final NoteMapper noteMapper;
    private final SubscriptionFeignClient subscriptionFeignClient;

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
    
    @GetMapping("/ids/user/{userId}")
    @Operation(summary = "获取用户笔记ID列表", description = "获取指定用户的所有笔记ID")
    public Result<List<Long>> getNoteIdsByUserId(@PathVariable("userId") Long userId) {
        return Result.success(noteService.getNoteIdsByUserId(userId));
    }

    @PostMapping("/{noteId}/publish")
    @Operation(summary = "发布笔记", description = "将草稿状态笔记发布")
    public Result<Void> publishNote(@PathVariable("noteId") Long noteId,
                                    @RequestHeader(value = CommonConstants.USER_ID_HEADER, required = false) String userIdStr) {
        log.info("收到发布笔记请求: noteId={}, userIdStr={}", noteId, userIdStr);
        
        // 手动转换 userId
        Long userId = null;
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                log.warn("发布笔记失败: userId格式错误: {}", userIdStr);
                return Result.error("用户ID格式错误");
            }
        }

        if (userId == null) {
            log.warn("发布笔记失败: userId为空");
            return Result.error("请先登录");
        }

        if (noteId == null) {
            log.warn("发布笔记失败: noteId为空");
            return Result.error("笔记ID不能为空");
        }

        try {
            noteService.publishNote(noteId, userId);
            log.info("发布笔记成功: noteId={}", noteId);
            return Result.success();
        } catch (BusinessException e) {
            log.error("发布笔记业务错误: noteId={}, error={}", noteId, e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("发布笔记系统错误: noteId={}", noteId, e);
            return Result.error("发布失败: " + e.getMessage());
        }
    }

    @GetMapping("/hot")
    @Operation(summary = "热门笔记", description = "按热度排序获取笔记列表")
    public Result<List<NoteDTO>> getHotNotes(@RequestParam(name = "limit", defaultValue = "20") Integer limit,
                                              @RequestHeader(value = CommonConstants.USER_ID_HEADER, required = false) Long userId) {
        return Result.success(noteService.getHotNotes(limit, userId));
    }

    @GetMapping("/latest")
    @Operation(summary = "最新笔记", description = "按时间排序获取最新笔记")
    public Result<List<NoteDTO>> getLatestNotes(@RequestParam(name = "limit", defaultValue = "20") Integer limit,
                                                 @RequestHeader(value = CommonConstants.USER_ID_HEADER, required = false) Long userId) {
        return Result.success(noteService.getLatestNotes(limit, userId));
    }

    @GetMapping("/school/{schoolId}")
    @Operation(summary = "学校笔记", description = "获取指定学校的已发布笔记")
    public Result<List<NoteDTO>> getNotesBySchool(@PathVariable(name = "schoolId") Long schoolId,
                                                   @RequestParam(name = "limit", defaultValue = "20") Integer limit) {
        return Result.success(noteService.getNotesBySchoolId(schoolId, limit));
    }

    @GetMapping("/subscribed")
    @Operation(summary = "订阅笔记", description = "获取我订阅的创作者发布的笔记")
    public Result<List<NoteDTO>> getSubscribedNotes(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestHeader(value = CommonConstants.USER_ID_HEADER, required = false) Long userId) {
        
        if (userId == null) {
            return Result.success(List.of());
        }
        
        // 获取用户订阅的创作者列表
        try {
            var result = subscriptionFeignClient.getMySubscriptions(userId);
            if (result == null || result.getData() == null || result.getData().isEmpty()) {
                return Result.success(List.of());
            }
            
            List<Long> creatorIds = result.getData().stream()
                    .map(sub -> ((Number) sub.get("creatorId")).longValue())
                    .collect(Collectors.toList());
            
            return Result.success(noteService.getSubscribedNotes(creatorIds, page, size));
        } catch (Exception e) {
            log.error("获取订阅笔记失败: userId={}", userId, e);
            return Result.success(List.of());
        }
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


    @GetMapping("/published")
    @Operation(summary = "获取已发布笔记", description = "Search-Service 用于同步数据")
    public Result<List<NoteDTO>> getPublishedNotes(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "100") Integer size) {
        try {
            // 计算偏移量
            int offset = (page - 1) * size;

            // 分页查询已发布的笔记
            List<Note> notes = noteMapper.selectPublishedNotesByPage(offset, size);

            // 转换为 DTO
            List<NoteDTO> noteDTOs = notes.stream()
                    .map(note -> {
                        NoteDTO dto = new NoteDTO();
                        BeanUtils.copyProperties(note, dto);
                        return dto;
                    })
                    .collect(Collectors.toList());

            return Result.success(noteDTOs);
        } catch (Exception e) {
            return Result.error("获取已发布笔记失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/batch")
    @Operation(summary = "批量获取笔记", description = "根据ID列表批量获取笔记信息")
    public Result<List<NoteDTO>> getNotesByIds(@RequestParam(name = "ids") String ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.success(List.of());
        }
        List<Long> noteIds = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
        
        if (noteIds.isEmpty()) {
            return Result.success(List.of());
        }
        
        List<Note> notes = noteMapper.selectListByIds(noteIds);
        List<NoteDTO> noteDTOs = notes.stream()
                .map(note -> {
                    NoteDTO dto = new NoteDTO();
                    BeanUtils.copyProperties(note, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return Result.success(noteDTOs);
    }

    @GetMapping("/creator/{creatorId}/stats")
    @Operation(summary = "获取创作者笔记统计", description = "获取创作者的笔记统计数据（浏览、收藏、评论等）")
    public Result<java.util.Map<String, Object>> getCreatorNoteStats(@PathVariable Long creatorId) {
        java.util.Map<String, Object> stats = noteService.getCreatorNoteStats(creatorId);
        return Result.success(stats);
    }
    
    @GetMapping("/stats")
    @Operation(summary = "获取笔记统计数据", description = "获取笔记统计数据，包括总数、新增数、待审核数等")
    public Result<NoteStatsDTO> getNoteStats() {
        return Result.success(noteService.getNoteStats());
    }
    
    @GetMapping("/audit/pending")
    @Operation(summary = "获取待审核笔记列表", description = "获取所有待审核的笔记列表")
    public Result<java.util.Map<String, Object>> getPendingAuditNotes(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        List<NoteDTO> list = noteService.getPendingAuditNotes(page, size);
        Long total = noteService.getPendingAuditCount();
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return Result.success(result);
    }
    
    @GetMapping("/audit/count")
    @Operation(summary = "获取待审核笔记数量", description = "获取待审核笔记的数量")
    public Result<Long> getPendingAuditCount() {
        return Result.success(noteService.getPendingAuditCount());
    }
    
    @PostMapping("/audit/{noteId}/pass")
    @Operation(summary = "审核通过", description = "管理员审核通过笔记")
    public Result<Void> auditPass(
            @PathVariable(name = "noteId") Long noteId,
            @RequestHeader(CommonConstants.ADMIN_ID_HEADER) Long adminId) {
        noteService.auditPass(noteId, adminId);
        return Result.success();
    }
    
    @PostMapping("/audit/{noteId}/reject")
    @Operation(summary = "审核拒绝", description = "管理员审核拒绝笔记")
    public Result<Void> auditReject(
            @PathVariable(name = "noteId") Long noteId,
            @RequestParam(name = "reason") String reason,
            @RequestHeader(CommonConstants.ADMIN_ID_HEADER) Long adminId) {
        noteService.auditReject(noteId, reason, adminId);
        return Result.success();
    }
    
    @PutMapping("/{noteId}/to-draft")
    @Operation(summary = "设置笔记为草稿状态", description = "将笔记状态设置为草稿，用于举报处理")
    public Result<Void> setNoteToDraft(@PathVariable(name = "noteId") Long noteId) {
        noteService.setNoteToDraft(noteId);
        return Result.success();
    }
    
    @GetMapping("/admin/list")
    @Operation(summary = "管理员获取笔记列表", description = "管理员分页获取笔记列表")
    public Result<Map<String, Object>> getNoteListForAdmin(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "keyword", required = false) String keyword) {
        Map<String, Object> result = noteService.getNoteListForAdmin(page, size, status, keyword);
        return Result.success(result);
    }
    
    @DeleteMapping("/admin/{noteId}")
    @Operation(summary = "管理员删除笔记", description = "管理员物理删除笔记")
    public Result<Void> deleteNoteByAdmin(@PathVariable(name = "noteId") Long noteId) {
        noteService.deleteNoteByAdmin(noteId);
        return Result.success();
    }
    
    @PostMapping("/admin/{noteId}/view-count")
    @Operation(summary = "管理员增加浏览量", description = "管理员手动增加笔记浏览量")
    public Result<Void> incrementViewCountByAdmin(
            @PathVariable(name = "noteId") Long noteId,
            @RequestParam(name = "count", defaultValue = "1") Integer count) {
        noteService.incrementViewCountByAdmin(noteId, count);
        return Result.success();
    }
}
