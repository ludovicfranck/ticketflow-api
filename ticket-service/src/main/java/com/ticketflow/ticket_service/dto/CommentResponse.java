package com.ticketflow.ticket_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentResponse {
    private String id;
    private String content;
    private String authorId;
    private String authorUsername;
    private String ticketId;
}
