package com.example.internship.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Member 3 — FileStorageService
 * Beyond-CRUD: File Upload with:
 *   - MIME type validation (application/pdf only)
 *   - 5 MB file size enforcement
 *   - UUID-based filename to prevent collisions
 */
@Service
public class FileStorageService {

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final String ALLOWED_MIME_TYPE  = "application/pdf";

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir:uploads/cvs}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException(
                "Could not create the upload directory: " + uploadDir, ex);
        }
    }

    /**
     * Validates and stores a CV file.
     *
     * @param file the uploaded MultipartFile
     * @return the unique filename stored on disk (to save in DB as cv_path)
     * @throws IllegalArgumentException if MIME type is not PDF or file exceeds 5 MB
     * @throws RuntimeException         if the file cannot be written to disk
     */
    public String saveFile(MultipartFile file) {

        // ── 1. Null / empty check ─────────────────────────────────────────────
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file to upload.");
        }

        // ── 2. MIME type validation (must be application/pdf) ─────────────────
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase(ALLOWED_MIME_TYPE)) {
            throw new IllegalArgumentException(
                "Only PDF files are accepted. You uploaded: " +
                (contentType != null ? contentType : "unknown type"));
        }

        // ── 3. File size validation (max 5 MB) ────────────────────────────────
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            long sizeMB = file.getSize() / (1024 * 1024);
            throw new IllegalArgumentException(
                "File is too large (" + sizeMB + " MB). Maximum allowed size is 5 MB.");
        }

        // ── 4. Sanitise original filename ────────────────────────────────────
        String originalFileName = file.getOriginalFilename();
        String extension = ".pdf"; // we already validated it's a PDF
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // ── 5. Generate unique filename and store ─────────────────────────────
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        try {
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException(
                "Could not store file " + uniqueFileName + ". Please try again.", ex);
        }
    }

    /**
     * Deletes a previously stored CV file (e.g. on re-upload).
     */
    public void deleteFile(String filename) {
        if (filename == null || filename.isBlank()) return;
        try {
            Path filePath = fileStorageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // Log but don't throw — deletion failure shouldn't block the user
            System.err.println("Warning: could not delete file " + filename + ": " + ex.getMessage());
        }
    }

    public Path getFilePath(String filename) {
        return fileStorageLocation.resolve(filename).normalize();
    }
}
