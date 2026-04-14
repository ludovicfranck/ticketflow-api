package com.ticketflow.notification_service.repository;

import com.ticketflow.notification_service.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository  extends JpaRepository<Notification ,String> {
    List<Notification> findAllByOrderBySentAtDesc();
    List<Notification> findBySourceEvent(String sourceEvent);
    List<Notification> findByRecipient(String recipient);
}
