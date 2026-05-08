package com.cardev.hub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

/**
 * 创建/更新讨论请求DTO
 */
@Data
public class DiscussionRequest {
    
    @NotBlank(message = "标题不能为空")
    private String title;
    
    @NotBlank(message = "内容不能为空")
    private String content;
    
    private Long knowledgeId;
    
    private Long projectId;
    
    private List<String> tags;
}
