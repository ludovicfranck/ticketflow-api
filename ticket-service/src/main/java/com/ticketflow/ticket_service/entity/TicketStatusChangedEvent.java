package com.ticketflow.ticket_service.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketStatusChangedEvent {
    private String ticketId;
    private String title;
    private String oldStatus;
    private String newStatus;
    private String creatorId;
    private LocalDateTime changedAt;
}
