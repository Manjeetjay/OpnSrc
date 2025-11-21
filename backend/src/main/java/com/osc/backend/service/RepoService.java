package com.osc.backend.service;


import com.osc.backend.dto.PublishRepoRequest;
import com.osc.backend.dto.RepoResponse;
import com.osc.backend.model.Repo;
import com.osc.backend.model.User;
import com.osc.backend.repos.RepoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepoService {

    private final RepoRepository repoRepository;
    private final RepoMetadataSyncService metadataSyncService;


    public RepoResponse toResponse(Repo r) {
        return RepoResponse.builder()
                .id(r.getId())
                .githubId(r.getGithubId())
                .name(r.getName())
                .fullName(r.getFullName())
                .htmlUrl(r.getHtmlUrl())
                .description(r.getDescription())
                .language(r.getLanguage())
                .stargazersCount(r.getStargazersCount())
                .publishedOnPlatform(r.getPublishedOnPlatform())
                .selectedAt(r.getSelectedAt())
                .build();
    }

    public List<RepoResponse> getMyRepos(User user) {
        List<Repo> repos = repoRepository.findByOwner(user);
        return repos.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public void publishRepos(User owner, List<PublishRepoRequest> requests) {

        for (PublishRepoRequest req : requests) {
            if (repoRepository.existsByGithubIdAndOwner(req.getGithubId(), owner))
                continue;

            // Create minimal repo entry
            Repo repo = Repo.builder()
                    .owner(owner)
                    .githubId(req.getGithubId())
                    .name(req.getName())
                    .description(req.getDescription())
                    .publishedOnPlatform(true)
                    .selectedAt(Instant.now())
                    .build();

            repo = repoRepository.save(repo);

            // Perform metadata sync
            metadataSyncService.syncMetadataForRepo(repo, owner.getGithubUsername());
        }
    }

}
