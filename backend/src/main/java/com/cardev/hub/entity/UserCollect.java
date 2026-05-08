package com.cardev.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户收藏实体类
 */
@Data
@TableName("user_collect")
public class UserCollect {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long knowledgeId;
    
    private Long folderId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
