package com.cardev.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 讨论回复实体类
 */
@Data
@TableName("discussion_reply")
public class DiscussionReply {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long discussionId;
    
    private String content;
    
    private Long authorId;
    
    private Long parentId;
    
    private Integer isAccepted;
    
    private Integer likeCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    // 非数据库字段
    @TableField(exist = false)
    private String authorName;
    
    @TableField(exist = false)
    private String authorAvatar;
    
    @TableField(exist = false)
    private Boolean isLiked;
}
