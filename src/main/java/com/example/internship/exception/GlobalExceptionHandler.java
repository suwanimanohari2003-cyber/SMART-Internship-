package com.example.internship.exception;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import java.nio.file.AccessDeniedException;

@Hidden
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handle404(NoHandlerFoundException ex, Model model) {
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorMessage", "Page Not Found. The resource you are looking for does not exist.");
        return "error/404";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handle403(AccessDeniedException ex, Model model) {
        model.addAttribute("errorCode", "403");
        model.addAttribute("errorMessage", "Access Denied. You do not have permission to view this page.");
        return "error/403";
    }

    /**
     * Spring Security's AccessDeniedException (thrown by manual ownership
     * checks in controllers, e.g. "this job posting doesn't belong to you")
     * is a different class from java.nio.file's — handle both the same way.
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public String handle403Security(org.springframework.security.access.AccessDeniedException ex, Model model) {
        model.addAttribute("errorCode", "403");
        model.addAttribute("errorMessage", "Access Denied. You do not have permission to view this page.");
        return "error/403";
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handle400(MissingServletRequestParameterException ex, Model model) {
        model.addAttribute("errorCode", "400");
        model.addAttribute("errorMessage", "Bad Request. Missing required information.");
        return "error/400";
    }

    /**
     * Catches malformed path/query params — e.g. a CV or application link
     * rendered/opened with an invalid or missing numeric id (".../null/cv",
     * ".../{id}/cv" with a non-numeric id). Previously this fell through to
     * the generic 500 handler, which was confusing for what is really just
     * a bad/stale link.
     */
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public String handle400TypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex, Model model) {
        model.addAttribute("errorCode", "400");
        model.addAttribute("errorMessage", "Bad Request. The link you used is invalid or has expired — please go back and try again.");
        return "error/400";
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public String handle404NotFound(java.util.NoSuchElementException ex, Model model) {
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorMessage", "Page Not Found. The record you're looking for no longer exists.");
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    public String handle500(Exception ex, Model model) {
        ex.printStackTrace();
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorMessage", "Internal Server Error. Please try again later.");
        return "error/500";
    }
}