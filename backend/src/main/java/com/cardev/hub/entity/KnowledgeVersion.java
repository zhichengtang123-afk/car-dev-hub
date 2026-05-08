package com.cardev.hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识版本历史实体类
 */
@Data
@TableName("knowledge_version")
public class KnowledgeVersion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long knowledgeId;

    private Integer version;

    private String title;

    private String summary;

    private String content;

    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 非数据库字段
    @TableField(exist = false)
    private String operatorName;
}
