package com.assignment.ce.exception;

public class GitHubOrgNotFoundException extends RuntimeException {
    public GitHubOrgNotFoundException(String orgName) {
        super("GitHub organization '" + orgName + "' not found");
    }
}