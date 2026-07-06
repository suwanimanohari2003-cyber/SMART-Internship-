package com.example.internship.validator;

import org.springframework.stereotype.Component;

/**
 * Member 4 — StudentValidator
 * Custom validation for student skill input.
 */
@Component
public class StudentValidator {

    /**
     * Validates that the skills string is not empty and contains at least one real keyword.
     * Skills should be comma-separated, e.g., "Java, Spring Boot, MySQL"
     */
    public boolean isValidSkills(String skills) {
        if (skills == null || skills.trim().isEmpty()) {
            return false;
        }
        String[] skillArray = skills.split(",");
        for (String skill : skillArray) {
            if (!skill.trim().isEmpty() && skill.trim().length() >= 2) {
                return true; // At least one real keyword found
            }
        }
        return false;
    }

    /**
     * Returns a clean, trimmed, comma-separated skills string.
     */
    public String normalizeSkills(String skills) {
        if (skills == null) return "";
        StringBuilder normalized = new StringBuilder();
        String[] parts = skills.split(",");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (!part.isEmpty()) {
                if (normalized.length() > 0) normalized.append(", ");
                normalized.append(part);
            }
        }
        return normalized.toString();
    }
}
