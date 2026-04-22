package com.virtualstore.notification_service.repository;

import com.virtualstore.notification_service.entity.NotificationLog;
import com.virtualstore.notification_service.entity.NotificationStatus;
import com.virtualstore.notification_service.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    Page<NotificationLog> findByRecipientEmail(String email, Pageable pageable);

    Page<NotificationLog> findByRecipientPhone(String phone, Pageable pageable);

    List<NotificationLog> findByStatus(NotificationStatus status);

    long countByTypeAndStatusAndCreatedAtAfter(
            NotificationType type, NotificationStatus status, Instant since);
}