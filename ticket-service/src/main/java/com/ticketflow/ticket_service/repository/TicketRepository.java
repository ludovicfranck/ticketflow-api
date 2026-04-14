package com.ticketflow.ticket_service.repository;

import com.ticketflow.ticket_service.dto.TicketResponse;
import com.ticketflow.ticket_service.entity.Ticket;
import com.ticketflow.ticket_service.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository  extends JpaRepository<Ticket,String> {
    List<TicketResponse> findByCreatorId(String creatorId);
    List<TicketResponse> findByAssigneeId(String assigneeId);
    List<TicketResponse> findByStatus(TicketStatus ticketStatus);

}
