package com.ticketflow.ticket_service.service;

import com.ticketflow.ticket_service.dto.CommentResponse;
import com.ticketflow.ticket_service.dto.CreateCommentRequest;
import com.ticketflow.ticket_service.dto.CreateTicketRequest;
import com.ticketflow.ticket_service.dto.TicketResponse;
import com.ticketflow.ticket_service.entity.Comment;
import com.ticketflow.ticket_service.entity.Ticket;
import com.ticketflow.ticket_service.entity.TicketStatus;
import com.ticketflow.ticket_service.feign.UserServiceClient;
import com.ticketflow.ticket_service.repository.CommentRepository;
import com.ticketflow.ticket_service.repository.TicketRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImplementation implements TicketService{

    private final TicketRepository ticketRepository;
    private final UserServiceClient userServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    @CircuitBreaker(name = "userServiceBreaker", fallbackMethod = "userServiceFallback")
    public TicketResponse createTicket(CreateTicketRequest ticketRequest) {
        // 1 Validation de l'auteur via Feign
        try {
            userServiceClient.getUserById(ticketRequest.getAssigneeId());
        }catch (Exception e){
            log.error("Utilisateur non trouvé : {}", ticketRequest.getAssigneeId());
            throw new RuntimeException("Utilisateur invalide");
        }
        // 2 Creation d'un ticket ...
        Ticket ticket = Ticket.builder()
                .title(ticketRequest.getTitle())
                .description(ticketRequest.getDescription())
                .priority(ticketRequest.getPriority())
                .status(TicketStatus.OPEN)
                .id(ticketRequest.getCreatorId())
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        // 3. Envoi de message par Kafka
        kafkaTemplate.send("ticket-events" , "ticket.created" , savedTicket);

        return mapTicketToTicketResponse(savedTicket);
    }
    // mapper de Ticket -> TicketResponse
    private TicketResponse mapTicketToTicketResponse(Ticket savedTicket) {
        return TicketResponse.builder()
                .id(savedTicket.getId())
                .title(savedTicket.getTitle())
                .description(savedTicket.getDescription())
                .creatorId(savedTicket.getCreatorId())
                .status(savedTicket.getStatus())
                .build();
    }

    @Override
    @Transactional
    public TicketResponse updateStatus(String id, TicketStatus newtStatus) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket absent !"));

        validateTransition(ticket.getStatus() , newtStatus);
        ticket.setStatus(newtStatus);

        Ticket updated = ticketRepository.save(ticket);
        // Envoi de message Kafka ticket.status.changed
        kafkaTemplate.send("ticket-events","ticket.status.changed" , updated);
        return mapTicketToTicketResponse(updated);

    }


    @Override
    public void validateTransition(TicketStatus actualStatus, TicketStatus nextStatus) {
        if (actualStatus == TicketStatus.CLOSED) {
            throw new RuntimeException("Transition impossible depuis CLOSED");
        }
    }

    // Retourner une liste pagine de Ticket

    @Override
    public Page<TicketResponse> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(
                ticket -> mapTicketToTicketResponse(ticket)
        );
    }

    @Override
    public TicketResponse getTicket(String id){
        Ticket findTicket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket non trouve !"));
        return mapTicketToTicketResponse(findTicket);
    }

    @Override
    public CommentResponse createComment(String idTicket,CreateCommentRequest commentRequest) {
        Comment comment = Comment.builder()
                .id(commentRequest.getId())
                .content(commentRequest.getContent())
                .authorId(commentRequest.getAuthorId())
                .authorUsername(commentRequest.getAuthorUsername())
                .createdAt(LocalDateTime.now()) // instant de creation t
                .build();
        Comment savedComment = commentRepository.save(comment);

        return mapCommentToCommentResponse(comment);

    }

    @Override
    public TicketResponse userServiceFallback(CreateTicketRequest ticketRequest, Throwable t) {
        log.warn("FALLBACK : Le service User est indisponible. Cause : {}", t.getMessage());

        // On retourne une réponse "dégradée" pour éviter le crash (RES-003)
        return TicketResponse.builder()
                .title(ticketRequest.getTitle())
                .description(ticketRequest.getDescription())
                .status(TicketStatus.OPEN)
                .creatorId("SERVICE_USER_INDISPONIBLE")
                .build();
    }

    private CommentResponse mapCommentToCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthorId())
                .authorUsername(comment.getAuthorUsername())
                .build();
    }
}
