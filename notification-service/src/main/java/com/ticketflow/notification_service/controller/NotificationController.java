package com.ticketflow.notification_service.controller;

import com.ticketflow.notification_service.dto.NotificationResponse;
import com.ticketflow.notification_service.dto.SendNotificationRequest;
import com.ticketflow.notification_service.entity.Notification;
import com.ticketflow.notification_service.entity.NotificationStatus;
import com.ticketflow.notification_service.entity.NotificationType;
import com.ticketflow.notification_service.repository.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "NotificationController" , description = "Gestion des notifications ")
public class NotificationController {
    private final NotificationRepository notificationRepository;

    /**
     * GET /api/notifications/history
     * Retourne l'historique de toutes les notifications envoyées.
     * Alimenté automatiquement par les consommateurs Kafka.
     */
    @GetMapping("/history")
    @PreAuthorize("hasAuthority('notification:read')")
    @Operation(summary = "Lister tous les notifications" , description = "Recupere et affiche l'historique des notifications")
    @ApiResponses({
            @ApiResponse(responseCode = "201" , description = "Liste recupere avec succes"),
            @ApiResponse(responseCode = "400" , description = "Corps de la requete invalide"),
            @ApiResponse(responseCode = "403" , description = "Scope notification:read manquant")
    })
    public ResponseEntity<List<NotificationResponse>> getHistory() {
        List<NotificationResponse> history = notificationRepository
                .findAllByOrderBySentAtDesc()
                .stream()
                .map(notification -> mapNotificationToNotificationResponse(notification))
                .collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }

    private NotificationResponse mapNotificationToNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .sourceId(notification.getSourceId())
                .notificationType(notification.getType())
                .recipient(notification.getRecipient())
                .subject(notification.getSubject())
                .content(notification.getContent())
                .build();
    }

    /**
     * POST /api/notifications/send
     * Envoi manuel d'une notification (utile pour les tests).
     */
    @PostMapping("/send")
    @PreAuthorize("hasAuthority('notification:send')")
    @Operation(summary = "envoi d'une notification" , description = "Envoi manuel d'une notification")
    @ApiResponses({
            @ApiResponse(responseCode = "201" , description = "Notification envoye avec success"),
            @ApiResponse(responseCode = "403" , description = "Acces refuse car scope notification:send absente")
    })
    public ResponseEntity<NotificationResponse> sendManual(@RequestBody SendNotificationRequest request) {
        log.info("[NOTIFICATION] Envoi manuel vers {}", request.getRecipient());

        // Mock envoi
        log.info("[EMAIL MOCK] ────────────────────────────────────");
        log.info("[EMAIL MOCK] To      : {}", request.getRecipient());
        log.info("[EMAIL MOCK] Subject : {}", request.getSubjet());
        log.info("[EMAIL MOCK] Body    : {}", request.getContent());
        log.info("[EMAIL MOCK] ────────────────────────────────────");

        NotificationType type = NotificationType.valueOf(
                request.getNotificationType() != null ? request.getNotificationType().name().toUpperCase( ) : "EMAIL"
        );

        Notification saved = notificationRepository.save(
                Notification.builder()
                        .type(type)
                        .recipient(request.getRecipient())
                        .subject(request.getSubjet())
                        .content(request.getContent())
                        .sourceEvent("manual")
                        .status(NotificationStatus.SENT)
                        .build()
        );

        return ResponseEntity.ok(mapNotificationToNotificationResponse(saved));
    }


}
