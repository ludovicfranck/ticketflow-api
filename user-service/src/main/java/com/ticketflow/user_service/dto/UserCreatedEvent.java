package com.ticketflow.user_service.dto;


import lombok.Builder;
import lombok.Data;


// classe dto qui represente le message (Kafka) lors de la creation d'un user
@Data
@Builder
public class UserCreatedEvent {
    private String userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
}
