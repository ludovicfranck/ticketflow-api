package com.ticketflow.document_service.controller;

import com.ticketflow.document_service.dto.DocumentResponse;
import com.ticketflow.document_service.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "DocumentController" , description = "Gestion des documents")
public class DocumentController {
    private final DocumentService documentService;

    // uploade un document
    @PostMapping(value = "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('document:upload')")
    @Operation(summary = "Televerser|uploader un document" , description = "Sauvegarde un document dans la Bd + minio + kafka")
    @ApiResponses({
            @ApiResponse(responseCode = "201" , description = "Document uploade avec succes"),
            @ApiResponse(responseCode = "400" , description = "Corps de la requete invalide"),
            @ApiResponse(responseCode = "403" , description = "scope document:upload absent")
    })
    public ResponseEntity<DocumentResponse> uploadDocument(@RequestBody MultipartFile file , String userId) throws Exception{
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.uploadDocument(file ,userId));
    }

    // Recuperer les Metadonnees d'un document ...
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('document:read')")
    @Operation(summary = "Lire un document" , description = "Recupere et affiche les info d'un document")
    @ApiResponses({
            @ApiResponse(responseCode = "200" , description = "Document recupere avec succes"),
            @ApiResponse(responseCode = "403" , description = "scope document:read absent")
    })
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable String id){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(documentService.getDocumentInfo(id));
    }

    // Telecharger un document a partir de son url presignee genere...
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('document:download')")
    @Operation(summary = "Telecharger un document" , description = "Telecharger un document a partir de l'url presignee")
    @ApiResponses({
            @ApiResponse(responseCode = "200" , description = "Document telecharge avec succes"),
            @ApiResponse(responseCode = "403" , description = "Scope document:download manquant")
    })
    public ResponseEntity<String> getDocumentUrl(@PathVariable String id) throws Exception {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(documentService.generateDownloadUrl(id));
    }
}
