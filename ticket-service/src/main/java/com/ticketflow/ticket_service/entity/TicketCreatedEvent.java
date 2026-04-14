package com.ticketflow.ticket_service.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketCreatedEvent {
    private String ticketId;
    private String title;
    private String creatorId;
    private String assigneeId;
    private String priority;
    private LocalDateTime createdAt;
}
