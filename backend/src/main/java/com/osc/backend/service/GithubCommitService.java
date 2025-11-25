package com.osc.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class GithubCommitService {

    private final RestTemplate restTemplate;

    public int getCommitsLast24Hours(String owner, String repoName) {
        Instant since = Instant.now().minusSeconds(24 * 3600);
        String url = "https://api.github.com/repos/" + owner + "/" + repoName
                + "/commits?since=" + since.toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github.v3+json");

        ResponseEntity<Object[]> resp =
                restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Object[].class);

        Object[] commits = resp.getBody();
        return commits == null ? 0 : commits.length;
    }
}
