package com.cardev.hub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

/**
 * 创建/更新知识请求DTO
 */
@Data
public class KnowledgeRequest {
    
    @NotBlank(message = "标题不能为空")
    private String title;
    
    @NotBlank(message = "知识类型不能为空")
    private String type;
    
    @NotBlank(message = "所属领域不能为空")
    private String domain;
    
    @NotBlank(message = "摘要不能为空")
    private String summary;
    
    private String content;
    
    private List<String> tags;
    
    private List<Long> projectIds;
    
    private List<Long> attachmentIds;
    
    private Integer status; // 0草稿/1发布
}
