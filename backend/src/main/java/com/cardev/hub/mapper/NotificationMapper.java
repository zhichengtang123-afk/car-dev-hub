package com.cardev.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cardev.hub.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统通知Mapper
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
