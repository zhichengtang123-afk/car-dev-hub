package com.cardev.hub.controller;

import com.cardev.hub.common.PageResult;
import com.cardev.hub.common.Result;
import com.cardev.hub.entity.Knowledge;
import com.cardev.hub.service.KnowledgeService;
import com.cardev.hub.service.SysConfigService;
import com.cardev.hub.dto.AdminSettingsDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "管理后台接口")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final KnowledgeService knowledgeService;
    private final SysConfigService configService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "获取待审核/已审核的知识列表")
    @GetMapping("/audit/list")
    public Result<PageResult<Knowledge>> getAuditList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 使用 knowledgeService.getList, 这里我们不传入复杂的过滤条件，只传入 status
        // status 会传给 knowledgeService.getList，注意如果不传，getList默认会加 status=2，所以前端这里应该总是传入指定的status
        PageResult<Knowledge> result = knowledgeService.getList(null, null, null, null, null, status, "time", page, size);
        return Result.success(result);
    }

    @Operation(summary = "审核通过")
    @PostMapping("/audit/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        try {
            knowledgeService.approve(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "审核拒绝")
    @PostMapping("/audit/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            knowledgeService.reject(id, body.get("reason"));
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "保存基础系统设置")
    @PostMapping("/settings/system")
    public Result<Void> saveSystemSettings(@RequestBody Map<String, String> settings) {
        settings.forEach((k, v) -> {
            configService.setConfigValue("sys_" + k, v);
        });
        return Result.success();
    }

    @Operation(summary = "获取所有后台设置")
    @GetMapping("/settings/all")
    public Result<AdminSettingsDTO> getAllSettings() throws Exception {
        AdminSettingsDTO dto = new AdminSettingsDTO();
        
        String categories = configService.getConfigValue("admin_categories");
        if (categories != null) {
            dto.setCategories(objectMapper.readValue(categories, new TypeReference<List<Map<String, Object>>>() {}));
        }
        
        String domains = configService.getConfigValue("admin_domains");
        if (domains != null) {
            dto.setDomains(objectMapper.readValue(domains, new TypeReference<List<Map<String, Object>>>() {}));
        }
        
        String tagGroups = configService.getConfigValue("admin_tag_groups");
        if (tagGroups != null) {
            dto.setTagGroups(objectMapper.readValue(tagGroups, new TypeReference<List<Map<String, Object>>>() {}));
        }
        
        String systemSettings = configService.getConfigValue("admin_system_settings");
        if (systemSettings != null) {
            dto.setSystemSettings(objectMapper.readValue(systemSettings, new TypeReference<Map<String, Object>>() {}));
        }
        
        return Result.success(dto);
    }

    @Operation(summary = "保存所有后台设置")
    @PostMapping("/settings/all")
    public Result<Void> saveAllSettings(@RequestBody AdminSettingsDTO dto) throws Exception {
        if (dto.getCategories() != null) {
            configService.setConfigValue("admin_categories", objectMapper.writeValueAsString(dto.getCategories()));
            // 更新通用知识类型
            List<String> types = dto.getCategories().stream()
                    .map(c -> (String) c.get("name"))
                    .collect(Collectors.toList());
            configService.setConfigValue("knowledge_types", objectMapper.writeValueAsString(types));
        }
        
        if (dto.getDomains() != null) {
            configService.setConfigValue("admin_domains", objectMapper.writeValueAsString(dto.getDomains()));
            // 更新通用知识领域
            List<String> domainNames = dto.getDomains().stream()
                    .map(d -> (String) d.get("name"))
                    .collect(Collectors.toList());
            configService.setConfigValue("knowledge_domains", objectMapper.writeValueAsString(domainNames));
        }
        
        if (dto.getTagGroups() != null) {
            configService.setConfigValue("admin_tag_groups", objectMapper.writeValueAsString(dto.getTagGroups()));
        }
        
        if (dto.getSystemSettings() != null) {
            configService.setConfigValue("admin_system_settings", objectMapper.writeValueAsString(dto.getSystemSettings()));
        }
        
        return Result.success();
    }
}
