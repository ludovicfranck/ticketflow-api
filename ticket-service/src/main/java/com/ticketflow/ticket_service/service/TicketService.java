package com.ticketflow.ticket_service.service;

import com.ticketflow.ticket_service.dto.CommentResponse;
import com.ticketflow.ticket_service.dto.CreateCommentRequest;
import com.ticketflow.ticket_service.dto.CreateTicketRequest;
import com.ticketflow.ticket_service.dto.TicketResponse;
import com.ticketflow.ticket_service.entity.Comment;
import com.ticketflow.ticket_service.entity.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface TicketService {
    TicketResponse createTicket(CreateTicketRequest ticketRequest);
    TicketResponse updateStatus(String id , TicketStatus ticketStatus);
    void validateTransition(TicketStatus actualStatus , TicketStatus nextStatus);
    Page<TicketResponse> getAllTickets(Pageable pageable);
    TicketResponse getTicket(String id);
    CommentResponse createComment(String id,CreateCommentRequest commentRequest);
    TicketResponse userServiceFallback(CreateTicketRequest ticketRequest , Throwable t);
}
