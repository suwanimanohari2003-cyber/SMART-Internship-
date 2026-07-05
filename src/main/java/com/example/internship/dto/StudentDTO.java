package com.example.internship.dto;

import jakarta.validation.constraints.*;

/**
 * Member 4 — StudentDTO
 * Data Transfer Object with Bean Validation for student input.
 */
public class StudentDTO {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "University is required")
    private String university;

    @NotNull(message = "GPA is required")
    @DecimalMin(value = "0.0", message = "GPA must be at least 0.0")
    @DecimalMax(value = "4.0", message = "GPA cannot exceed 4.0")
    private Double gpa;

    @NotBlank(message = "Skills are required")
    private String skills;

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }

    public Double getGpa() { return gpa; }
    public void setGpa(Double gpa) { this.gpa = gpa; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
}
