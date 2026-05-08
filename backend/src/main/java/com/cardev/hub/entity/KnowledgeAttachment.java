package com.cardev.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识附件实体类
 */
@Data
@TableName("knowledge_attachment")
public class KnowledgeAttachment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long knowledgeId;
    
    private String fileName;
    
    private String filePath;
    
    private Long fileSize;
    
    private String fileType;
    
    private Integer downloadCount;

    @TableField(exist = false)
    private String url;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
