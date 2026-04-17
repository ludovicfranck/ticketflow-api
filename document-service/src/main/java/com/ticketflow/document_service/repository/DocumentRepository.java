package com.ticketflow.document_service.repository;

import com.ticketflow.document_service.dto.DocumentResponse;
import com.ticketflow.document_service.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document , String> {
    List<DocumentResponse> findByTicketId(String ticketId);
    List<DocumentResponse> findByUploadedBy(String uploadedBy);
}
