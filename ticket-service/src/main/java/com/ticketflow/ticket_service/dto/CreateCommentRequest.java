package com.ticketflow.ticket_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CreateCommentRequest {
    private String id;
    private String content;
    private String authorId;
    private String authorUsername;
    private LocalDateTime createdAt;
}
