package com.osc.backend.service;

import com.osc.backend.model.Repo;
import com.osc.backend.model.RepoStatsHistory;
import com.osc.backend.repos.RepoRepository;
import com.osc.backend.repos.RepoStatsHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepoStatsScheduler {

    private final RepoRepository repoRepository;
    private final RepoStatsHistoryRepository historyRepository;
    private final GithubCommitService commitService;

    private final RepoMetadataSyncService metadataService;

    /**
     * Runs every 24h at midnight (server time) -> "0 0 0 * * *" (CHANGE THIS LATER)
     */
    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void dailyStatsSync() {

        List<Repo> repos = repoRepository.findAllByPublishedOnPlatformTrue();

        for (Repo repo : repos) {

            // sync stars, forks, etc. first
            metadataService.syncMetadataForRepo(repo, repo.getOwner().getGithubUsername());

            // commit count in last 24 hours
            int commits = commitService.getCommitsLast24Hours(
                    repo.getOwner().getGithubUsername(),
                    repo.getName()
            );

            RepoStatsHistory snapshot = RepoStatsHistory.builder()
                    .repo(repo)
                    .date(Instant.now())
                    .commits(commits)
                    .stars(repo.getStargazersCount())
                    .forks(repo.getForksCount())
                    .build();

            historyRepository.save(snapshot);
        }
    }
}
