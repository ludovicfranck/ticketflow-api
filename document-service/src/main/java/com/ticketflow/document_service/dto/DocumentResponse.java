package com.ticketflow.document_service.dto;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DocumentResponse {
    private String id; // Autogenere dans la Bd
    private String fileName;
    private String mimeType;
    private Long size;
    private String bucket;
    private String uploadedBy;
    private LocalDateTime uploadedAt;

}
