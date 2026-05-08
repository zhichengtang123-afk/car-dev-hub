package com.cardev.hub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

/**
 * 创建/更新项目请求DTO
 */
@Data
public class ProjectRequest {
    
    @NotBlank(message = "项目名称不能为空")
    private String name;
    
    private String description;
    
    private Long leaderId;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private Integer status;
}
