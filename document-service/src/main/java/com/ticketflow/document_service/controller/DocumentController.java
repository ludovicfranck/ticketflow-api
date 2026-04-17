package com.ticketflow.document_service.controller;

import com.ticketflow.document_service.dto.DocumentResponse;
import com.ticketflow.document_service.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    // uploade un document
    @PostMapping(value = "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority(document:upload)")
    public ResponseEntity<DocumentResponse> uploadDocument(@RequestBody MultipartFile file , String userId) throws Exception{
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.uploadDocument(file ,userId));
    }

    // Recuperer les Metadonnees d'un document ...
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(document:read)")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable String id){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(documentService.getDocumentInfo(id));
    }

    // Telecharger un document a partir de son url presignee genere...
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority(document:download)")
    public ResponseEntity<String> getDocumentUrl(@PathVariable String id) throws Exception {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(documentService.generateDownloadUrl(id));
    }
}
