package com.cardev.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 讨论标签实体类
 */
@Data
@TableName("discussion_tag")
public class DiscussionTag {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long discussionId;
    
    private String tagName;
}
