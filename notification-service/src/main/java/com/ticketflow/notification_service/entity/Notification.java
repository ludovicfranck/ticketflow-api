package com.ticketflow.notification_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private NotificationType type = NotificationType.EMAIL;

    /* Destinataire ... */
    private String recipient;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    /** Source de la notification : user:created , ... */
    private String sourceEvent;

    /** ID de la source userId , ticketId , ...*/
    private String sourceId;

    private NotificationStatus status = NotificationStatus.PENDING;

    /** Message d'erreur ... */
    private String errorMessage;

    @CreationTimestamp
    private LocalDateTime sentAt;




}
