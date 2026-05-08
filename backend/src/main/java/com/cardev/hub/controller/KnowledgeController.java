package com.cardev.hub.controller;

import com.cardev.hub.common.PageResult;
import com.cardev.hub.common.Result;
import com.cardev.hub.dto.KnowledgeRequest;
import com.cardev.hub.entity.Comment;
import com.cardev.hub.entity.Knowledge;
import com.cardev.hub.entity.KnowledgeAttachment;
import com.cardev.hub.entity.KnowledgeVersion;
import com.cardev.hub.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 知识控制器
 */
@Tag(name = "知识接口")
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final CommentService commentService;
    private final FileService fileService;
    private final SysConfigService configService;

    @Operation(summary = "获取知识列表")
    @GetMapping("/list")
    public Result<PageResult<Knowledge>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Knowledge> result = knowledgeService.getList(keyword, type, domain, tag, authorId, status, sort, page, size);
        return Result.success(result);
    }

    @Operation(summary = "搜索知识")
    @GetMapping("/search")
    public Result<PageResult<Knowledge>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Knowledge> result = knowledgeService.search(keyword, type, domain, tags, sort, page, size);
        return Result.success(result);
    }

    @Operation(summary = "获取知识详情")
    @GetMapping("/{id}")
    public Result<Knowledge> getDetail(@PathVariable Long id) {
        try {
            Knowledge knowledge = knowledgeService.getDetail(id);
            return Result.success(knowledge);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "创建知识")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody KnowledgeRequest request) {
        try {
            Long id = knowledgeService.create(request);
            return Result.success(id);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新知识")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody KnowledgeRequest request) {
        try {
            knowledgeService.update(id, request);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除知识")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            knowledgeService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "上传附件")
    @PostMapping("/upload")
    public Result<KnowledgeAttachment> upload(@RequestParam("file") MultipartFile file) {
        try {
            KnowledgeAttachment attachment = fileService.uploadFile(file);
            return Result.success(attachment);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "下载附件")
    @GetMapping("/attachment/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        try {
            Resource resource = fileService.downloadFile(id);
            KnowledgeAttachment attachment = fileService.getAttachment(id);
            
            String encodedFileName = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename*=UTF-8''" + encodedFileName)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "点赞知识")
    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        try {
            knowledgeService.like(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "取消点赞")
    @DeleteMapping("/{id}/like")
    public Result<Void> unlike(@PathVariable Long id) {
        try {
            knowledgeService.unlike(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "收藏知识")
    @PostMapping("/{id}/collect")
    public Result<Void> collect(@PathVariable Long id, @RequestBody(required = false) Map<String, Long> body) {
        try {
            Long folderId = body != null ? body.get("folderId") : null;
            knowledgeService.collect(id, folderId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{id}/collect")
    public Result<Void> uncollect(@PathVariable Long id) {
        try {
            knowledgeService.uncollect(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取我的收藏")
    @GetMapping("/collections")
    public Result<PageResult<Knowledge>> getMyCollections(
            @RequestParam(required = false) Long folderId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Knowledge> result = knowledgeService.getMyCollections(folderId, page, size);
        return Result.success(result);
    }

    @Operation(summary = "获取知识评论")
    @GetMapping("/{id}/comments")
    public Result<PageResult<Comment>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Comment> result = commentService.getComments(id, page, size);
        return Result.success(result);
    }

    @Operation(summary = "发表评论")
    @PostMapping("/{id}/comments")
    public Result<Long> createComment(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            String content = (String) body.get("content");
            Long parentId = body.get("parentId") != null ? Long.valueOf(body.get("parentId").toString()) : null;
            Long commentId = commentService.createComment(id, content, parentId);
            return Result.success(commentId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{knowledgeId}/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long knowledgeId, @PathVariable Long commentId) {
        try {
            commentService.deleteComment(knowledgeId, commentId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取版本历史")
    @GetMapping("/{id}/versions")
    public Result<List<KnowledgeVersion>> getVersions(@PathVariable Long id) {
        List<KnowledgeVersion> versions = knowledgeService.getVersions(id);
        return Result.success(versions);
    }

    @Operation(summary = "回滚版本")
    @PostMapping("/{id}/rollback/{versionId}")
    public Result<Void> rollbackVersion(@PathVariable Long id, @PathVariable Long versionId) {
        try {
            knowledgeService.rollbackVersion(id, versionId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取热门知识")
    @GetMapping("/hot")
    public Result<List<Knowledge>> getHotKnowledge(@RequestParam(defaultValue = "10") int limit) {
        List<Knowledge> list = knowledgeService.getHotKnowledge(limit);
        return Result.success(list);
    }

    @Operation(summary = "获取推荐知识")
    @GetMapping("/recommend")
    public Result<List<Knowledge>> getRecommendKnowledge(@RequestParam(defaultValue = "10") int limit) {
        List<Knowledge> list = knowledgeService.getRecommendKnowledge(limit);
        return Result.success(list);
    }

    @Operation(summary = "获取知识类型")
    @GetMapping("/types")
    public Result<List<String>> getTypes() {
        List<String> types = configService.getKnowledgeTypes();
        return Result.success(types);
    }

    @Operation(summary = "获取知识领域")
    @GetMapping("/domains")
    public Result<List<String>> getDomains() {
        List<String> domains = configService.getKnowledgeDomains();
        return Result.success(domains);
    }

    @Operation(summary = "获取热门标签")
    @GetMapping("/tags/hot")
    public Result<List<String>> getHotTags(@RequestParam(defaultValue = "20") int limit) {
        List<String> tags = knowledgeService.getHotTags(limit);
        return Result.success(tags);
    }
}
