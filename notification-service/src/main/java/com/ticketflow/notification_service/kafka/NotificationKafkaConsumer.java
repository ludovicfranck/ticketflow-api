package com.ticketflow.notification_service.kafka;

import com.ticketflow.notification_service.entity.Notification;
import com.ticketflow.notification_service.entity.NotificationStatus;
import com.ticketflow.notification_service.entity.NotificationType;
import com.ticketflow.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaConsumer {
    private final NotificationRepository notificationRepository;

    // ─────────────────────────────────────────
    // TOPIC : user.created → Email de bienvenue
    // ─────────────────────────────────────────

    /**
     * Consomme user.created publié par user-service.
     *
     * Action : envoie un email de bienvenue au nouvel utilisateur.
     * En production, ici on appellerait SendGrid, Mailgun, etc.
     * En dev : log + sauvegarde en historique.
     */
    @KafkaListener(
            topics = "user.created",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onUserCreated(Map<String, Object> event) {
        log.info("[KAFKA] Réception user.created : userId={}", event.get("userId"));

        String email     = (String) event.get("email");
        String firstName = (String) event.get("firstName");
        String userId    = (String) event.get("userId");

        String subject = "Bienvenue sur TicketFlow, " + firstName + " !";
        String content = String.format(
                "Bonjour %s,\n\n" +
                        "Votre compte TicketFlow a été créé avec succès.\n" +
                        "Vous pouvez dès maintenant créer et suivre vos tickets de support.\n\n" +
                        "L'équipe TicketFlow",
                firstName
        );

        // Mock envoi email
        log.info("[EMAIL MOCK] ────────────────────────────────────");
        log.info("[EMAIL MOCK] To      : {}", email);
        log.info("[EMAIL MOCK] Subject : {}", subject);
        log.info("[EMAIL MOCK] Body    : {}", content);
        log.info("[EMAIL MOCK] ────────────────────────────────────");

        // Persister en historique
        saveNotification(
                email, subject, content,
                NotificationType.EMAIL,
                "user.created", userId,
                NotificationStatus.SENT
        );
    }

    // ─────────────────────────────────────────
    // TOPIC : ticket.created → Notification support
    // ─────────────────────────────────────────

    /**
     * Consomme ticket.created publié par ticket-service.
     *
     * Action : notifie l'équipe support qu'un nouveau ticket est arrivé.
     */
    @KafkaListener(
            topics = "ticket.created",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTicketCreated(Map<String, Object> event) {
        log.info("[KAFKA] Réception ticket.created : ticketId={}", event.get("ticketId"));

        String ticketId = (String) event.get("ticketId");
        String title    = (String) event.get("title");
        String priority = (String) event.get("priority");

        String recipient = "support@ticketflow.com";
        String subject   = "[NOUVEAU TICKET] " + title + " (Priorité : " + priority + ")";
        String content   = String.format(
                "Un nouveau ticket a été créé.\n\n" +
                        "ID       : %s\n" +
                        "Titre    : %s\n" +
                        "Priorité : %s\n\n" +
                        "Connectez-vous pour le traiter.",
                ticketId, title, priority
        );

        log.info("[EMAIL MOCK] ────────────────────────────────────");
        log.info("[EMAIL MOCK] To      : {}", recipient);
        log.info("[EMAIL MOCK] Subject : {}", subject);
        log.info("[EMAIL MOCK] Body    : {}", content);
        log.info("[EMAIL MOCK] ────────────────────────────────────");

        saveNotification(
                recipient, subject, content,
                NotificationType.EMAIL,
                "ticket.created", ticketId,
                NotificationStatus.SENT
        );
    }

    // ─────────────────────────────────────────
    // TOPIC : ticket.status.changed → Notification utilisateur
    // ─────────────────────────────────────────

    /**
     * Consomme ticket.status.changed publié par ticket-service.
     *
     * Action : notifie le créateur du ticket du changement de statut.
     */
    @KafkaListener(
            topics = "ticket.status.changed",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTicketStatusChanged(Map<String, Object> event) {
        log.info("[KAFKA] Réception ticket.status.changed : ticketId={} {} → {}",
                event.get("ticketId"), event.get("oldStatus"), event.get("newStatus"));

        String ticketId   = (String) event.get("ticketId");
        String title      = (String) event.get("title");
        String oldStatus  = (String) event.get("oldStatus");
        String newStatus  = (String) event.get("newStatus");
        String creatorId  = (String) event.get("creatorId");

        // En production, on ferait un appel user-service pour récupérer l'email du créateur
        // Ici on simule avec un email générique basé sur l'ID
        String recipient = "user-" + creatorId + "@ticketflow.com";
        String subject   = "[TICKET MIS À JOUR] " + title;
        String content   = String.format(
                "Le statut de votre ticket a été mis à jour.\n\n" +
                        "Ticket  : %s\n" +
                        "Statut  : %s → %s\n\n" +
                        "Connectez-vous pour voir les détails.",
                title, oldStatus, newStatus
        );

        log.info("[EMAIL MOCK] ────────────────────────────────────");
        log.info("[EMAIL MOCK] To      : {}", recipient);
        log.info("[EMAIL MOCK] Subject : {}", subject);
        log.info("[EMAIL MOCK] Body    : {}", content);
        log.info("[EMAIL MOCK] ────────────────────────────────────");

        saveNotification(
                recipient, subject, content,
                NotificationType.EMAIL,
                "ticket.status.changed", ticketId,
                NotificationStatus.SENT
        );
    }

    // ─────────────────────────────────────────
    // UTILITAIRE : persistance historique
    // ─────────────────────────────────────────

    private void saveNotification(
            String recipient, String subject, String content,
            NotificationType type, String sourceEvent, String sourceId,
            NotificationStatus status) {
        try {
            notificationRepository.save(
                    Notification.builder()
                            .recipient(recipient)
                            .subject(subject)
                            .content(content)
                            .type(type)
                            .sourceEvent(sourceEvent)
                            .sourceId(sourceId)
                            .status(status)
                            .build()
            );
            log.debug("[NOTIFICATION] Enregistrée en base : sourceEvent={}, sourceId={}",
                    sourceEvent, sourceId);
        } catch (Exception e) {
            log.error("[NOTIFICATION] Erreur sauvegarde historique : {}", e.getMessage());
        }
    }
}
