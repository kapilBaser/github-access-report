package com.assignment.ce.controller;

import com.assignment.ce.service.GithubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github")
public class GithubController {
    @Autowired
    private OAuth2AuthorizedClientService clientService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GithubService githubService;

    @GetMapping("/test")
    public String test(){
        return "Hello World";
    }


    @GetMapping("/repos")
    public Object getRepos(){
        String accessToken = getAccessToken();
        System.out.println(accessToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Object> response = restTemplate.exchange("https://api.github.com/user/repos", HttpMethod.GET, entity, Object.class);
        System.out.println(response.getBody());
        return response.getBody();
    }


    @GetMapping("/access-report/{org}")
    public Map<String, List<String>> getAccessReport(
            @PathVariable String org,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage)
            throws Exception {

        String token = getAccessToken();
        try {
            Map<String, List<String>> report = githubService.generateAccessReport(org, token, page, perPage);
            return report;
        } catch (RuntimeException e) {
            return (Map<String, List<String>>) ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/organizations")
    public List<Map<String, Object>> getUserOrganizations() {



        String accessToken = getAccessToken();

        return githubService.getUserOrgs(accessToken);
    }


    public String getAccessToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(auth);
        OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) auth;
        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient(
                        authentication.getAuthorizedClientRegistrationId(),
                        authentication.getName()
                );

        return client.getAccessToken().getTokenValue();
    }



}
