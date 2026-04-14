package com.ticketflow.notification_service.dto;

import com.ticketflow.notification_service.entity.NotificationType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponse {
    private String sourceId;
    private NotificationType notificationType;
    private String recipient;
    private String subject;
    private String content;
}
