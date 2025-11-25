package com.osc.backend.controller;

import com.osc.backend.dto.RepoStatsHistoryDto;
import com.osc.backend.model.Repo;
import com.osc.backend.model.RepoStatsHistory;
import com.osc.backend.repos.RepoRepository;
import com.osc.backend.repos.RepoStatsHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/repos")
@RequiredArgsConstructor
public class RepoAnalyticsController {

    private final RepoRepository repoRepository;
    private final RepoStatsHistoryRepository historyRepository;

    @GetMapping("/{repoId}/analytics")
    public List<RepoStatsHistoryDto> analytics(@PathVariable Long repoId) {
        Repo repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new RuntimeException("Repo not found"));

        Instant start = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant end = Instant.now();

        List<RepoStatsHistory> list = historyRepository.findByRepoAndDateBetween(repo, start, end);

        return list.stream()
                .map(h -> new RepoStatsHistoryDto(
                        h.getDate(),
                        h.getCommits(),
                        h.getStars(),
                        h.getForks()
                ))
                .collect(Collectors.toList());

    }

    @PostMapping("/{repoId}/analytics/generate")
    public String generateAnalytics(@PathVariable Long repoId) {

        Repo repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new RuntimeException("Repo not found"));

        // get stars/forks/commits
        int commits = historyRepository
                .findByRepoOrderByDateDesc(repo).stream()
                .findFirst()
                .map(RepoStatsHistory::getCommits)
                .orElse(0);

        RepoStatsHistory snapshot = RepoStatsHistory.builder()
                .repo(repo)
                .date(Instant.now())
                .commits(commits)
                .stars(repo.getStargazersCount())
                .forks(repo.getForksCount())
                .build();

        historyRepository.save(snapshot);

        return "Snapshot created!";
    }

}
