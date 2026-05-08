package com.cardev.hub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cardev.hub.common.PageResult;
import com.cardev.hub.entity.Notification;
import com.cardev.hub.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通知服务
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserService userService;

    public PageResult<Notification> getMyNotifications(int page, int size) {
        Long userId = userService.getCurrentUserId();
        Page<Notification> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);
        
        Page<Notification> result = notificationMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public long getUnreadCount() {
        Long userId = userService.getCurrentUserId();
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    @Transactional
    public void markAsRead(Long id) {
        Long userId = userService.getCurrentUserId();
        Notification notification = notificationMapper.selectById(id);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        }
    }

    @Transactional
    public void markAllAsRead() {
        Long userId = userService.getCurrentUserId();
        Notification updateObj = new Notification();
        updateObj.setIsRead(1);
        notificationMapper.update(updateObj, new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    @Transactional
    public void sendNotification(Long userId, String title, String content, String type, Long referenceId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }
}
