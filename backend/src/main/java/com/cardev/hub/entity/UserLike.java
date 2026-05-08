package com.cardev.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户点赞实体类
 */
@Data
@TableName("user_like")
public class UserLike {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String targetType;
    
    private Long targetId;
    
    private Integer isLike;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
