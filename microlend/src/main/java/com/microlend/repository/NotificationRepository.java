package com.microlend.repository;

import com.microlend.entity.Notification;
import com.microlend.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserID(Long userID);
    List<Notification> findByUserIDAndStatus(Long userID, NotificationStatus status);
}
