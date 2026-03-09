package com.assignment.ce.client;

import com.assignment.ce.exception.GitHubOrgNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class GithubClient {

    @Autowired
    private RestTemplate restTemplate;

    public List<Map<String, Object>> getOrgRepos(String org, String token, int page, int perPage) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response =
                    restTemplate.exchange(
                            "https://api.github.com/orgs/" + org + "/repos?per_page=" + perPage + "&page=" + page,
                            HttpMethod.GET,
                            entity,
                            List.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            // Throw a custom exception for your global handler
            throw new GitHubOrgNotFoundException(org);
        }
    }
    public List<Map<String, Object>> getRepoCollaborators(String org, String repo, String token, int page, int perPage) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response =
                    restTemplate.exchange(
                            "https://api.github.com/repos/" + org + "/" + repo + "/collaborators?per_page=" + perPage + "&page=" + page,
                            HttpMethod.GET,
                            entity,
                            List.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            // Optional: handle repo not found separately if needed
            throw new GitHubOrgNotFoundException(org + "/" + repo);
        }
    }
}