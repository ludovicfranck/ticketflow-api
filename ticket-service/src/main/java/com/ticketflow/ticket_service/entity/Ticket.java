package com.ticketflow.ticket_service.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "tickets")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN ;
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;
    /*ID de l'utilisateur ayant cree le ticket*/
    @Column(nullable = false)
    private String creatorId;
    private String assigneeId; // assigneTo { Id de l'agent assigne )
    @ElementCollection
    @CollectionTable(
            name = "ticket_documents",
            joinColumns = @JoinColumn(name = "ticket_id")
    )
    @Builder.Default
    private List<String> documentIds = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;


}
