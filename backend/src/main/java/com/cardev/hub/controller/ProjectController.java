package com.cardev.hub.controller;

import com.cardev.hub.common.PageResult;
import com.cardev.hub.common.Result;
import com.cardev.hub.dto.ProjectRequest;
import com.cardev.hub.entity.Knowledge;
import com.cardev.hub.entity.Project;
import com.cardev.hub.entity.User;
import com.cardev.hub.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 项目控制器
 */
@Tag(name = "项目接口")
@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "获取项目列表")
    @GetMapping("/list")
    public Result<PageResult<Project>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Project> result = projectService.getList(keyword, status, page, size);
        return Result.success(result);
    }

    @Operation(summary = "获取项目详情")
    @GetMapping("/{id}")
    public Result<Project> getDetail(@PathVariable Long id) {
        try {
            Project project = projectService.getDetail(id);
            return Result.success(project);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "创建项目")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ProjectRequest request) {
        try {
            Long id = projectService.create(request);
            return Result.success(id);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新项目")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        try {
            projectService.update(id, request);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除项目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            projectService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取项目成员")
    @GetMapping("/{id}/members")
    public Result<List<User>> getMembers(@PathVariable Long id) {
        List<User> members = projectService.getMembers(id);
        return Result.success(members);
    }

    @Operation(summary = "添加项目成员")
    @PostMapping("/{id}/members")
    public Result<Void> addMember(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            projectService.addMember(id, body.get("userId"));
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "移除项目成员")
    @DeleteMapping("/{id}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        try {
            projectService.removeMember(id, userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取项目关联的知识")
    @GetMapping("/{id}/knowledge")
    public Result<PageResult<Knowledge>> getProjectKnowledge(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Knowledge> result = projectService.getProjectKnowledge(id, page, size);
        return Result.success(result);
    }

    @Operation(summary = "关联知识到项目")
    @PostMapping("/{projectId}/knowledge/{knowledgeId}")
    public Result<Void> linkKnowledge(@PathVariable Long projectId, @PathVariable Long knowledgeId) {
        try {
            projectService.linkKnowledge(projectId, knowledgeId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "取消关联")
    @DeleteMapping("/{projectId}/knowledge/{knowledgeId}")
    public Result<Void> unlinkKnowledge(@PathVariable Long projectId, @PathVariable Long knowledgeId) {
        try {
            projectService.unlinkKnowledge(projectId, knowledgeId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
