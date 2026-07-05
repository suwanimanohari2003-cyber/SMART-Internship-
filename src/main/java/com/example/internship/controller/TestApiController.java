package com.example.internship.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test API", description = "API for testing Swagger functionality")
public class TestApiController {

    @Operation(summary = "Hello World Endpoint", description = "The system provides a message to confirm that it is working correctly.")
    @GetMapping("/hello")
    public String sayHello() {
        return "Congratulations! Swagger is running very successfully!";
    }
}
