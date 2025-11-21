package com.osc.backend.controller;

import com.osc.backend.dto.PublishReposRequest;
import com.osc.backend.dto.RepoResponse;
import com.osc.backend.model.Repo;
import com.osc.backend.model.User;
import com.osc.backend.repos.RepoRepository;
import com.osc.backend.service.RepoMetadataSyncService;
import com.osc.backend.service.RepoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repos")
@RequiredArgsConstructor
public class ReposController {

    private final RepoService repoService;
    private final RepoRepository repoRepository;
    private final RepoMetadataSyncService metadataSyncService;

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

}
