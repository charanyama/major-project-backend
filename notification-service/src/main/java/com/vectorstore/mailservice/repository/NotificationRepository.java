package com.vectorstore.mailservice.repository;

import com.vectorstore.mailservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}