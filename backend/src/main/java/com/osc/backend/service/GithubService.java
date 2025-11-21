package com.osc.backend.service;

import com.osc.backend.dto.GithubRepoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubService {

    private final RestTemplate restTemplate;

    public List<GithubRepoDto> fetchPublicRepos(String username) {
        String url = UriComponentsBuilder.fromHttpUrl("https://api.github.com/users/{username}/repos")
                .queryParam("per_page", 100)
                .buildAndExpand(username)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, "application/vnd.github.v3+json");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<GithubRepoDto[]> resp = restTemplate.exchange(url, HttpMethod.GET, entity, GithubRepoDto[].class);
        GithubRepoDto[] arr = resp.getBody();
        if (arr == null) return List.of();
        return Arrays.asList(arr);
    }
}
