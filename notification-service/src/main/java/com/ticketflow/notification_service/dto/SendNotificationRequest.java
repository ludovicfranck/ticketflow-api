package com.ticketflow.notification_service.dto;

import com.ticketflow.notification_service.entity.NotificationType;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SendNotificationRequest {
    private NotificationType notificationType;
    private String content;
    private String recipient;
    private String sourceEvent;
    private String sourceId;
    private String subjet;
}
