package com.ticketflow.document_service.service;

import com.ticketflow.document_service.dto.DocumentResponse;
import com.ticketflow.document_service.entity.Document;
import com.ticketflow.document_service.repository.DocumentRepository;
import io.minio.*;
import io.minio.http.Method;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
    private final MinioClient minioClient;
    private final DocumentRepository documentRepository;
    private final KafkaTemplate<String , Object> kafkaTemplate;
    private final String bucketName = "ticketflow-bucket" ;


    /** Methode permettant d'uploader un document*/
    @Transactional
    public DocumentResponse uploadDocument(MultipartFile multipartFile , String userId) throws Exception{
        // 1 - creation du bucket si il n'existe pas
        boolean isBucketCreated = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

        if (!isBucketCreated){ // bloc de code qui s'execute si le bucket n'a pas ete cree
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        String objectName = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();

        // 2 - Upload vers MinIo
        minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(multipartFile.getInputStream() , multipartFile.getSize(), -1)
                        .contentType(multipartFile.getContentType())
                .build());

        // 3 Sauvegarde dans la Bd des info du document uploade
        Document documentUploaded = Document.builder()
                .fileName(multipartFile.getName())
                .mimeType(multipartFile.getContentType())
                .size(multipartFile.getSize())
                .bucket(bucketName)
                .uploadedBy(userId)
                .uploadedAt(LocalDateTime.now())
                .build();

        Document savedDocument = documentRepository.save(documentUploaded);

        // 4 - Envoi de message via Kafka ...
        kafkaTemplate.send("documents-events" ,"document.uploaded" , savedDocument)
                .whenComplete((result , exception) -> {
                    if (exception == null){
                        log.info("[KAFKA] document.uploaded publié OK - partition= {}" , result.getRecordMetadata().partition());

                    }
                    else {
                        log.error("[KAFKA] Echec lors de l'operation <<document.uploaded>> = {}" ,exception);
                    }
                })
        ;

        return mapDocumentToDocumentResponse(savedDocument);
    }

    private DocumentResponse mapDocumentToDocumentResponse(Document savedDocument) {
        return DocumentResponse.builder()
                .id(savedDocument.getId())
                .fileName(savedDocument.getFileName())
                .mimeType(savedDocument.getMimeType())
                .size(savedDocument.getSize())
                .bucket(savedDocument.getBucket())
                .uploadedBy(savedDocument.getUploadedBy())
                .uploadedAt(savedDocument.getUploadedAt())
                .build();
    }

    // Generation d'une url presigne avec expiration de 05 minutes ...
    public String generateDownloadUrl(String id) throws Exception{
        Document document = documentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Document non trouve !"));

        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(document.getBucket())
                        .object(document.getObjectKey())
                        .expiry(300) // 1min = 60s => 5min = 300s
                        .build());
    }

    public DocumentResponse getDocumentInfo(String id) {
        Document document = documentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Document non present !"));
        return mapDocumentToDocumentResponse(document);
    }
}
