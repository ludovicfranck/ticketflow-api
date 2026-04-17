package com.ticketflow.ticket_service.controller;


import com.ticketflow.ticket_service.dto.CommentResponse;
import com.ticketflow.ticket_service.dto.CreateCommentRequest;
import com.ticketflow.ticket_service.dto.CreateTicketRequest;
import com.ticketflow.ticket_service.dto.TicketResponse;
import com.ticketflow.ticket_service.entity.TicketStatus;
import com.ticketflow.ticket_service.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "TicketController" , description = "Gestion des Tickets")
public class TicketController {
    private final TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasAuthority(ticket:create)")
    @Operation(summary = "Creer un ticket",
        description = "Creer un ticket + Synchronisation avec Keycloak +  Message via Kafka"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201" , description = "Ticket créé"),
            @ApiResponse(responseCode = "400" , description = "Corps de la requête manquante"),
            @ApiResponse(responseCode = "403" , description = "scope (permission) ticket:create manquant")
    })
    public ResponseEntity<TicketResponse> createTicket(@RequestBody CreateTicketRequest ticketRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(ticketRequest));
    }

    @GetMapping
    @PreAuthorize("hasAuthority(ticket:read)")
    @Operation(summary = "Recuperer tous les tickets" ,
            description = "Retourne une liste paginée ")
    @ApiResponses({
            @ApiResponse(responseCode = "200" , description = "Liste des tickets recuperes avec success"),
            @ApiResponse(responseCode = "404" , description = "Liste des tickets inexistant"),
            @ApiResponse(responseCode = "403" , description = "Scope ticket:read manquant")
    })
    public ResponseEntity<Page<TicketResponse>> getAllTickets(@RequestBody Pageable pageable){
        return ResponseEntity.ok(ticketService.getAllTickets(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(ticket:read)")
    @Operation(summary = "Recuperer un ticket",
              description = "Retourne les informations d'un ticket"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket trouve avec success"),
            @ApiResponse(responseCode = "404" , description = "Ticket inexistant"),
            @ApiResponse(responseCode = "403" , description = "scope ticket:read manquant")
    })
    public ResponseEntity<TicketResponse> getTicket(@PathVariable String id){
        return ResponseEntity.ok(ticketService.getTicket(id));
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ticket:update')")
    @Operation(summary = "Mettre a jour un ticket", description = "Met a jour les informations d'un ticket existant dans la Bd + Synchro keycloak")
    public ResponseEntity<TicketResponse> updateTicketStatus(@PathVariable String id , @RequestBody TicketStatus newStatus){
        return ResponseEntity.ok(ticketService.updateStatus(id , newStatus));
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAuthority('ticket:comment')")
    @Operation(summary = "creer un commentaire sur un ticket", description = "Deposer un commentaire a propos d'un ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "201" , description = "Commentaire ajoute avec succces"),
            @ApiResponse(responseCode = "403" , description = "scope ticket:comment mannquant")
    })
    public ResponseEntity<CommentResponse> createComment(@PathVariable String idTicket , @RequestBody CreateCommentRequest commentRequest){
        return ResponseEntity.ok(ticketService.createComment(idTicket , commentRequest ));
    }


}
