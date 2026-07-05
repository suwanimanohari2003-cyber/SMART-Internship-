package com.example.internship.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI smartInternshipOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Internship Portal API")
                        .description("API Documentation for the Smart Internship & Placement Portal")
                        .version("v1.0.0"))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local Development Server")));
    }
}
