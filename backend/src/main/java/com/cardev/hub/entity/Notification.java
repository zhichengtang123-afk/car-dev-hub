package com.cardev.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统通知实体类
 */
@Data
@TableName("sys_notification")
public class Notification {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String title;
    
    private String content;
    
    private String type;
    
    private Integer isRead;
    
    private Long referenceId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
