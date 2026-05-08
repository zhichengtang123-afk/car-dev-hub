package com.cardev.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 讨论/问答实体类
 */
@Data
@TableName("discussion")
public class Discussion {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String content;
    
    private Long authorId;
    
    private Long knowledgeId;
    
    private Long projectId;
    
    private Integer viewCount;
    
    private Integer replyCount;
    
    private Integer isResolved;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 非数据库字段
    @TableField(exist = false)
    private String authorName;
    
    @TableField(exist = false)
    private String authorAvatar;
    
    @TableField(exist = false)
    private java.util.List<String> tags;
}
