package com.ticketflow.document_service.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Builder
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    /* Nom du fichier ...*/
    @Column(nullable = false)
    private String fileName;
    /* Cle d'objet dans Minio*/
    @Column(nullable = false)
    private String objectKey;
    private String mimeType;
    private Long size;
    /** Bucket MinIo ou le fichier est stocke */
    @Column(nullable = false)
    @Builder.Default
    private String bucket = "ticketflow-bucket";
    /** Id de l'utilisateur qui a uploade le document*/
    @Column(nullable = false)
    private String uploadedBy;
    /** Id du ticket associe , optionnel a l'upload lie via Kafka ... */
    private String ticketId;
    @CreationTimestamp
    private LocalDateTime uploadedAt;

}
