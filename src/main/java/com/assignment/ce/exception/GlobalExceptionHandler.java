package com.assignment.ce.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletionException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public void redirect(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/github");
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
        ApiError error = new ApiError(403, "Forbidden", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ApiError error = new ApiError(404, "Not Found", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(GitHubApiException.class)
    public ResponseEntity<ApiError> handleGitHubApi(GitHubApiException ex, HttpServletRequest request) {
        ApiError error = new ApiError(500, "GitHub API Error", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(GitHubOrgNotFoundException.class)
    public ResponseEntity<Object> handleOrgNotFound(GitHubOrgNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "status", 404,
                        "error", "Not Found",
                        "message", ex.getMessage(),
                        "path", request.getRequestURI()
                ));
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<ApiError> handleCompletionException(CompletionException ex, HttpServletRequest request) {
        Throwable cause = ex.getCause();
        ApiError error = new ApiError(500, "Async Processing Error", cause != null ? cause.getMessage() : ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(Exception ex, HttpServletRequest request) {
        ApiError error = new ApiError(500, "Internal Server Error", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}