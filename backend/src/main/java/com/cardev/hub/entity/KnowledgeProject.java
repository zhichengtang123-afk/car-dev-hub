package com.cardev.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识-项目关联实体类
 */
@Data
@TableName("knowledge_project")
public class KnowledgeProject {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long knowledgeId;
    
    private Long projectId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
