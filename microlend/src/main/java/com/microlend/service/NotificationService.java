package com.microlend.service;

import com.microlend.entity.Notification;
import com.microlend.enums.NotificationCategory;
import com.microlend.enums.NotificationStatus;
import com.microlend.exception.ResourceNotFoundException;
import com.microlend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification send(Long userID, String message, NotificationCategory category) {
        Notification notification = Notification.builder()
                .userID(userID)
                .message(message)
                .category(category)
                .status(NotificationStatus.UNREAD)
                .createdDate(LocalDateTime.now())
                .build();
        return notificationRepository.save(notification);
    }

    public List<Notification> getByUser(Long userID) {
        return notificationRepository.findByUserID(userID);
    }

    public List<Notification> getUnreadByUser(Long userID) {
        return notificationRepository.findByUserIDAndStatus(userID, NotificationStatus.UNREAD);
    }

    public Notification markRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
        notification.setStatus(NotificationStatus.READ);
        return notificationRepository.save(notification);
    }

    public Notification dismiss(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
        notification.setStatus(NotificationStatus.DISMISSED);
        return notificationRepository.save(notification);
    }

    public List<Notification> getAll() {
        return notificationRepository.findAll();
    }
}
