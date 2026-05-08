package com.cardev.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 知识标签实体类
 */
@Data
@TableName("knowledge_tag")
public class KnowledgeTag {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long knowledgeId;
    
    private String tagName;
}
