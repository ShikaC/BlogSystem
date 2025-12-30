package com.blogs.service;

import com.blogs.common.PageResult;
import com.blogs.entity.Notification;
import com.blogs.exception.BusinessException;
import com.blogs.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消息通知服务
 */
@Service
@Transactional
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    public void sendNotification(Long receiverId, Long senderId, String type, String title, String content, Long targetId, String targetType) {
        if (receiverId == null) {
            throw new BusinessException("接收者ID不能为空");
        }
        Notification notification = new Notification();
        notification.setReceiverId(receiverId);
        notification.setSenderId(senderId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetId(targetId);
        notification.setTargetType(targetType);
        notificationRepository.save(notification);
    }
    
    public PageResult<Notification> getNotifications(Long userId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Notification> notificationPage = notificationRepository.findByReceiverId(userId, pageable);
        return PageResult.of(notificationPage.getContent(), notificationPage.getTotalElements(), page, size);
    }
    
    public void markAsRead(Long id) {
        if (id == null) {
            throw new BusinessException("通知ID不能为空");
        }
        notificationRepository.findById(id).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }
    
    public long getUnreadCount(Long userId) {
        if (userId == null) {
            return 0L;
        }
        return notificationRepository.countByReceiverIdAndIsReadFalse(userId);
    }
}

