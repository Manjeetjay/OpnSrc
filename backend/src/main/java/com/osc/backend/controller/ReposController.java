package com.osc.backend.controller;

import com.osc.backend.dto.PublishReposRequest;
import com.osc.backend.dto.RepoResponse;
import com.osc.backend.model.Repo;
import com.osc.backend.model.User;
import com.osc.backend.repos.RepoRepository;
import com.osc.backend.service.LikeService;
import com.osc.backend.service.RepoMetadataSyncService;
import com.osc.backend.service.RepoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repos")
@RequiredArgsConstructor
public class ReposController {

    private final RepoService repoService;
    private final RepoRepository repoRepository;
    private final RepoMetadataSyncService metadataSyncService;
    private final LikeService likeService;

    @PostMapping("/publish")
    public ResponseEntity<?> publishRepos(@AuthenticationPrincipal User user,
                                          @RequestBody PublishReposRequest request) {
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        repoService.publishRepos(user, request.getRepos());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<List<RepoResponse>> myRepos(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(repoService.getMyRepos(user));
    }

    @PostMapping("/{repoId}/sync")
    public ResponseEntity<?> syncRepo(@AuthenticationPrincipal User user,
                                      @PathVariable Long repoId) {

        Repo repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new RuntimeException("Repo not found"));

        if (!repo.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Not your repo");
        }

        metadataSyncService.syncMetadataForRepo(repo, user.getGithubUsername());
        return ResponseEntity.ok("Synced successfully");
    }

    @GetMapping("/{repoId}")
    public ResponseEntity<?> getRepoDetail(@PathVariable Long repoId,
                                           @AuthenticationPrincipal User user) {
        Repo repo = repoRepository.findById(repoId).orElseThrow(() -> new RuntimeException("Repo not found"));
        long likes = likeService.countLikes(repo);
        boolean likedByMe = (user != null) && likeService.userLiked(user, repo);

        Map<String, Object> body = new HashMap<>();
        body.put("id", repo.getId());
        body.put("githubId", repo.getGithubId());
        body.put("name", repo.getName());
        body.put("fullName", repo.getFullName());
        body.put("htmlUrl", repo.getHtmlUrl());
        body.put("description", repo.getDescription());
        body.put("language", repo.getLanguage());
        body.put("stargazersCount", repo.getStargazersCount());
        body.put("forksCount", repo.getForksCount());
        body.put("updatedAt", repo.getUpdatedAt());
        body.put("likes", likes);
        body.put("likedByMe", likedByMe);
        body.put("owner", Map.of(
                "id", repo.getOwner().getId(),
                "displayName", repo.getOwner().getDisplayName(),
                "githubUsername", repo.getOwner().getGithubUsername()
        ));

        return ResponseEntity.ok(body);
    }

    @PostMapping("/{repoId}/like")
    public ResponseEntity<?> likeRepo(@AuthenticationPrincipal User user,
                                      @PathVariable Long repoId) {
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        long likes = likeService.toggleLike(user, repoId);
        return ResponseEntity.ok(Map.of("likes", likes));
    }



}
