package com.ticketflow.ticket_service.controller;


import com.ticketflow.ticket_service.dto.CommentResponse;
import com.ticketflow.ticket_service.dto.CreateCommentRequest;
import com.ticketflow.ticket_service.dto.CreateTicketRequest;
import com.ticketflow.ticket_service.dto.TicketResponse;
import com.ticketflow.ticket_service.entity.TicketStatus;
import com.ticketflow.ticket_service.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasAuthority(ticket:create)")
    public ResponseEntity<TicketResponse> createTicket(@RequestBody CreateTicketRequest ticketRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(ticketRequest));
    }

    @GetMapping
    @PreAuthorize("hasAuthority(ticket:read)")
    public ResponseEntity<Page<TicketResponse>> getAllTickets(Pageable pageable){
        return ResponseEntity.ok(ticketService.getAllTickets(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(ticket:read)")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable String id){
        return ResponseEntity.ok(ticketService.getTicket(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ticket:update')")
    public ResponseEntity<TicketResponse> updateTicketStatus(@PathVariable String id , @RequestBody TicketStatus newStatus){
        return ResponseEntity.ok(ticketService.updateStatus(id , newStatus));
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAuthority('ticket:comment')")
    public ResponseEntity<CommentResponse> createComment(@PathVariable String idTicket , @RequestBody CreateCommentRequest commentRequest){
        return ResponseEntity.ok(ticketService.createComment(idTicket , commentRequest ));
    }


}
