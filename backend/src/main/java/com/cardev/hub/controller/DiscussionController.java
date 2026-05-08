package com.cardev.hub.controller;

import com.cardev.hub.common.PageResult;
import com.cardev.hub.common.Result;
import com.cardev.hub.dto.DiscussionRequest;
import com.cardev.hub.entity.Discussion;
import com.cardev.hub.entity.DiscussionReply;
import com.cardev.hub.service.DiscussionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 讨论控制器
 */
@Tag(name = "讨论接口")
@RestController
@RequestMapping("/discussion")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService discussionService;

    @Operation(summary = "获取讨论列表")
    @GetMapping("/list")
    public Result<PageResult<Discussion>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long knowledgeId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Boolean resolved,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Discussion> result = discussionService.getList(keyword, knowledgeId, projectId, resolved, page, size);
        return Result.success(result);
    }

    @Operation(summary = "获取讨论详情")
    @GetMapping("/{id}")
    public Result<Discussion> getDetail(@PathVariable Long id) {
        try {
            Discussion discussion = discussionService.getDetail(id);
            return Result.success(discussion);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "创建讨论")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody DiscussionRequest request) {
        try {
            Long id = discussionService.create(request);
            return Result.success(id);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新讨论")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DiscussionRequest request) {
        try {
            discussionService.update(id, request);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除讨论")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            discussionService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取讨论回复")
    @GetMapping("/{id}/replies")
    public Result<PageResult<DiscussionReply>> getReplies(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<DiscussionReply> result = discussionService.getReplies(id, page, size);
        return Result.success(result);
    }

    @Operation(summary = "创建回复")
    @PostMapping("/{id}/replies")
    public Result<Long> createReply(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            String content = (String) body.get("content");
            Long parentId = body.get("parentId") != null ? Long.valueOf(body.get("parentId").toString()) : null;
            Long replyId = discussionService.createReply(id, content, parentId);
            return Result.success(replyId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除回复")
    @DeleteMapping("/{discussionId}/replies/{replyId}")
    public Result<Void> deleteReply(@PathVariable Long discussionId, @PathVariable Long replyId) {
        try {
            discussionService.deleteReply(discussionId, replyId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "采纳回复")
    @PostMapping("/{discussionId}/replies/{replyId}/accept")
    public Result<Void> acceptReply(@PathVariable Long discussionId, @PathVariable Long replyId) {
        try {
            discussionService.acceptReply(discussionId, replyId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "点赞回复")
    @PostMapping("/{discussionId}/replies/{replyId}/like")
    public Result<Void> likeReply(@PathVariable Long discussionId, @PathVariable Long replyId) {
        try {
            discussionService.likeReply(discussionId, replyId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "取消点赞回复")
    @DeleteMapping("/{discussionId}/replies/{replyId}/like")
    public Result<Void> unlikeReply(@PathVariable Long discussionId, @PathVariable Long replyId) {
        try {
            discussionService.unlikeReply(discussionId, replyId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
