package com.ticketflow.ticket_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "comments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** ID de l'auteur du commentaire (extrait du JWT) */
    @Column(nullable = false)
    private String authorId;

    private String authorUsername;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
