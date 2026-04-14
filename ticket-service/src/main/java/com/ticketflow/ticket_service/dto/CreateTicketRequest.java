package com.ticketflow.ticket_service.dto;

import com.ticketflow.ticket_service.entity.Priority;
import lombok.Data;

@Data
public class CreateTicketRequest {
    private String title;
    private String description;
    private Priority priority;
    private String assigneeId;
    private String creatorId;
}
