package com.osc.backend.service;

import com.osc.backend.dto.GithubRepoDetailDto;
import com.osc.backend.model.Repo;
import com.osc.backend.repos.RepoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RepoMetadataSyncService {

    private final RestTemplate restTemplate;
    private final RepoRepository repoRepository;

    public void syncMetadataForRepo(Repo repo, String ownerUsername) {
        String url = "https://api.github.com/repos/" + ownerUsername + "/" + repo.getName();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<GithubRepoDetailDto> resp = restTemplate.exchange(
                url, HttpMethod.GET, entity, GithubRepoDetailDto.class
        );

        GithubRepoDetailDto data = resp.getBody();
        if (data == null) return;

        repo.setFullName(data.getFullName());
        repo.setHtmlUrl(data.getHtmlUrl());
        repo.setLanguage(data.getLanguage());
        repo.setDescription(data.getDescription());
        repo.setStargazersCount(data.getStargazersCount());
        repo.setForksCount(data.getForksCount());

        if (data.getUpdatedAt() != null) {
            repo.setUpdatedAt(Instant.parse(data.getUpdatedAt()));
        }

        repoRepository.save(repo);
    }
}
