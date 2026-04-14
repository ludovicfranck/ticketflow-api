package com.ticketflow.ticket_service.dto;

import com.ticketflow.ticket_service.entity.Priority;
import com.ticketflow.ticket_service.entity.TicketStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketResponse {
    private String id;
    private String title;
    private String description;
    private TicketStatus status;
    private Priority priority;
    private String creatorId;
}
