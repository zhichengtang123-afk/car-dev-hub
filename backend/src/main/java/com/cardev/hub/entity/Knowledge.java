package com.cardev.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识资源实体类
 */
@Data
@TableName("knowledge")
public class Knowledge {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String type;
    
    private String domain;
    
    private String summary;
    
    private String content;
    
    private Long authorId;
    
    private Integer viewCount;
    
    private Integer likeCount;
    
    private Integer dislikeCount;
    
    private Integer collectCount;
    
    private Integer commentCount;
    
    private Integer status;
    
    private Integer version;
    
    private Long auditorId;
    
    private LocalDateTime auditTime;
    
    private String rejectReason;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 非数据库字段
    @TableField(exist = false)
    private String authorName;
    
    @TableField(exist = false)
    private String auditorName;
    
    @TableField(exist = false)
    private String authorAvatar;

    @TableField(exist = false)
    private String authorDepartment;
    
    @TableField(exist = false)
    private java.util.List<String> tags;
    
    @TableField(exist = false)
    private java.util.List<Long> projectIds;
    
    @TableField(exist = false)
    private Boolean isLiked;
    
    @TableField(exist = false)
    private Boolean isCollected;
}
