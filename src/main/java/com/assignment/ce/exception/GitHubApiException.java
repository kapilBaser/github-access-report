package com.assignment.ce.exception;

public class GitHubApiException extends RuntimeException {
    public GitHubApiException(String message) {
        super(message);
    }

    public GitHubApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
