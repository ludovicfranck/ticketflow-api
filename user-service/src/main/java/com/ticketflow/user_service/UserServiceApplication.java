package com.ticketflow.user_service;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@OpenAPIDefinition(
        info = @Info(
                title = "Ticketflow Api",
                version = "1.0",
                description = "TicketFlow est un mini-système de helpdesk permettant à des utilisateurs de créer des tickets de support, de recevoir des notifications automatiques et de joindre des documents .",
                contact = @Contact(
                        name = "Franck Ludovic",
                        email = "franckludovic351@gmail.com",
                        url = "https://github.com/ludovicfranck/ticketflow-api.git"
                ),
                license = @License(
                        name = "TicketFlowAPI",
                        url = "https://github.com/ludovicfranck/ticketflow-api.git"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "Documentation de TicketFlow",
                url = "https://github.com/ludovicfranck/ticketflow-api.git"
        )
)
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
