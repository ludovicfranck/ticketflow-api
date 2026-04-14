package com.ticketflow.user_service.kafka;


import com.ticketflow.user_service.dto.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserEventProducer{
    public static final String TOPIC_USER_CREATED = "user.created";
    public final KafkaTemplate<String , Object> kafkaTemplate;

    public void sendUserCreatedEvent(UserCreatedEvent createdUserEvent){
        log.info("[KAFKA] Envoi de l'evenement user.created pour : {}" , createdUserEvent.getUsername());
        kafkaTemplate.send(TOPIC_USER_CREATED , createdUserEvent.getUserId() , createdUserEvent)
                .whenComplete((result , exceptionMessage) -> {
                    if (exceptionMessage == null ){
                        log.info("[KAFKA] Message envoye avec envoyé avec succès à l'offset {}", result.getRecordMetadata().offset());
                    }
                    else{
                        log.error("[KAFKA] Échec de l'envoi ! " , exceptionMessage);
                    }
                });

    }
}
