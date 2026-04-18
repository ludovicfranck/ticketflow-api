package com.ticketflow.api_gateway;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
@OpenAPIDefinition(info = @Info(
        title = "Api Gateway of Ticketflow Api",
        version = "1.1",
        description = "TicketFlow est un mini-système de helpdesk permettant à des utilisateurs de créer des tickets de support, de recevoir des notifications automatiques et de joindre des documents ",
        contact = @Contact(
                name = "Franck Ludovic",
                email = "franckludovic351@gmail.com",
                url = "https://github.com/ludovicfranck/ticketflow-api.git"
        ),
        license = @License(
                name = "Secure API with Spring Boot | Spring Cloud | Spring Security | Keycloak | ...",
                url = "https://github.com/ludovicfranck/ticketflow-api.git"
        )
),
        externalDocs = @ExternalDocumentation(
                description = "Documentation of ticketflow API",
                url = "https://github.com/ludovicfranck/ticketflow-api.git"
        ))
public class ApiGatewayApplication {
	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

    @Bean
    public GroupedOpenApi gatewayApi() {
        return GroupedOpenApi.builder()
                .group("api-gateway")
                .pathsToMatch("/**")
                .build();
    }
}
