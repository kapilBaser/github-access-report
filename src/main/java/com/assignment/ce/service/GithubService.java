package com.assignment.ce.service;

import com.assignment.ce.client.GithubClient;
import com.assignment.ce.exception.GitHubApiException;
import com.assignment.ce.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GithubService {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GithubClient githubClient;

    public Map<String, List<String>> generateAccessReport(String org, String token, int page, int perPage) {

        Map<String, List<String>> userRepoMap = new ConcurrentHashMap<>();
        List<Map<String, Object>> repos;

        try {
            repos = githubClient.getOrgRepos(org, token, page, perPage);

            if (repos == null || repos.isEmpty()) {
                throw new RuntimeException("No repos found on page " + page);
            }

        } catch (RestClientException ex) {
            throw new GitHubApiException("Failed to connect to GitHub API", ex);
        }

        if (repos == null || repos.isEmpty()) {
            throw new ResourceNotFoundException("No repositories found for org: " + org);
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Map<String, Object> repo : repos) {
            String repoName = repo.get("name").toString();
            futures.add(fetchCollaborators(org, repoName, token, userRepoMap));
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException ex) {
            // throw the actual cause so GlobalExceptionHandler can catch it
            throw ex.getCause() instanceof RuntimeException
                    ? (RuntimeException) ex.getCause()
                    : new RuntimeException(ex.getCause());
        }
        return userRepoMap;
    }

    @Async
    public CompletableFuture<Void> fetchCollaborators(String org, String repo, String token,
                                                      Map<String, List<String>> map) {
        try {
            List<Map<String, Object>> collaborators = githubClient.getRepoCollaborators(org, repo, token);

            if (collaborators != null) {
                for (Map<String, Object> user : collaborators) {
                    String username = user.get("login").toString();
                    map.computeIfAbsent(username, k -> Collections.synchronizedList(new ArrayList<>()))
                            .add(repo);
                }
            }
        } catch (RestClientException ex) {
            throw new GitHubApiException("Failed to fetch collaborators for repo: " + repo, ex);
        } catch (Exception ex) {
            throw new GitHubApiException("Unexpected error fetching collaborators for repo: " + repo, ex);
        }

        return CompletableFuture.completedFuture(null);
    }

//    public Map<String, List<String>> generateReport(
//            String org,
//            String token){
//
//        Map<String, List<String>> report = new HashMap<>();
//
//        List<Map<String,Object>> repos = getOrgRepos(org, token);
//
//        for(Map<String,Object> repo : repos){
//
//            String repoName = repo.get("name").toString();
//
//            List<Map<String,Object>> users =
//                    getRepoCollaborators(org, repoName, token);
//
//            for(Map<String,Object> user : users){
//
//                String username = user.get("login").toString();
//
//                report
//                        .computeIfAbsent(username, k -> new ArrayList<>())
//                        .add(repoName);
//            }
//        }
//
//        return report;
//    }

    public List<Map<String, Object>> getUserOrgs(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            List<Map<String, Object>> orgs = restTemplate.exchange(
                    "https://api.github.com/user/orgs",
                    HttpMethod.GET,
                    entity,
                    List.class
            ).getBody();

            if (orgs == null || orgs.isEmpty()) {
                throw new ResourceNotFoundException("No organizations found for user.");
            }

            return orgs;

        } catch (RestClientException ex) {
            throw new GitHubApiException("Failed to fetch user organizations", ex);
        } catch (Exception ex) {
            throw new GitHubApiException("Unexpected error fetching user organizations", ex);
        }
    }
}
