package com.osc.backend.service;

import com.osc.backend.dto.RepoExploreDto;
import com.osc.backend.model.Repo;
import com.osc.backend.model.RepoStatsHistory;
import com.osc.backend.repos.RepoRepository;
import com.osc.backend.repos.RepoStatsHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExploreService {

    private final RepoRepository repoRepository;
    private final LikeService likeService;
    private final RepoStatsHistoryRepository historyRepository;

    private final double W_LIKES = 0.35;
    private final double W_STARS = 0.45;
    private final double W_RECENCY = 0.20;
    private final double W_COMMITS = 0.25;

    public Page<RepoExploreDto> getExplore(String sort, Pageable pageable) {

        List<Repo> repos = repoRepository.findAllByPublishedOnPlatformTrue();
        if (repos.isEmpty())
            return new PageImpl<>(List.of(), pageable, 0);

        Map<Long, Long> likesMap = new HashMap<>();
        Map<Long, Integer> starsMap = new HashMap<>();
        Map<Long, Double> recencyMap = new HashMap<>();
        Map<Long, Integer> commitsMap = new HashMap<>();

        Instant now = Instant.now();

        int maxLikes = 1;
        int maxStars = 1;
        double maxRecencyScore = 1.0;
        int maxCommits = 1;

        for (Repo r : repos) {

            // Likes
            long likes = likeService.countLikes(r);
            likesMap.put(r.getId(), likes);
            maxLikes = Math.max(maxLikes, (int) likes);

            // Stars
            int stars = r.getStargazersCount() == null ? 0 : r.getStargazersCount();
            starsMap.put(r.getId(), stars);
            maxStars = Math.max(maxStars, stars);

            // Recency (updatedAt)
            double recencyScore = 0.0;
            if (r.getUpdatedAt() != null) {
                long days = Duration.between(r.getUpdatedAt(), now).toDays();
                recencyScore = Math.max(0.0, Math.exp(-days / 30.0)); // exponential decay
            }
            recencyMap.put(r.getId(), recencyScore);
            maxRecencyScore = Math.max(maxRecencyScore, recencyScore);

            // Commits in last 24 hours (from history)
            int commits = historyRepository
                    .findByRepoOrderByDateDesc(r).stream()
                    .findFirst()
                    .map(RepoStatsHistory::getCommits)
                    .orElse(0);

            commitsMap.put(r.getId(), commits);
            maxCommits = Math.max(maxCommits, commits);
        }

        // Build DTO list
        int finalMaxLikes = maxLikes;
        int finalMaxStars = maxStars;
        double finalMaxRecency = maxRecencyScore;
        int finalMaxCommits = maxCommits;

        List<RepoExploreDto> dtos = repos.stream().map(r -> {

            long likes = likesMap.getOrDefault(r.getId(), 0L);
            int stars = starsMap.getOrDefault(r.getId(), 0);
            double recencyScore = recencyMap.getOrDefault(r.getId(), 0.0);
            int commits = commitsMap.getOrDefault(r.getId(), 0);

            // Normalize metrics
            double normLikes = (double) likes / finalMaxLikes;
            double normStars = finalMaxStars == 0 ? 0.0 : (double) stars / finalMaxStars;
            double normRecency = finalMaxRecency == 0 ? 0.0 : recencyScore / finalMaxRecency;
            double normCommits = finalMaxCommits == 0 ? 0.0 : (double) commits / finalMaxCommits;

            // Composite trending score
            double score = W_LIKES * normLikes +
                    W_STARS * normStars +
                    W_RECENCY * normRecency +
                    W_COMMITS * normCommits;

            return RepoExploreDto.builder()
                    .id(r.getId())
                    .githubId(r.getGithubId())
                    .name(r.getName())
                    .fullName(r.getFullName())
                    .htmlUrl(r.getHtmlUrl())
                    .description(r.getDescription())
                    .language(r.getLanguage())
                    .stargazersCount(r.getStargazersCount())
                    .forksCount(r.getForksCount())
                    .updatedAt(r.getUpdatedAt())
                    .likes(likes)
                    .score(score)
                    .ownerDisplayName(r.getOwner() != null ? r.getOwner().getDisplayName() : null)
                    .ownerGithubUsername(r.getOwner() != null ? r.getOwner().getGithubUsername() : null)
                    .build();

        }).toList();

        // Sorting
        Comparator<RepoExploreDto> comparator;

        switch (Optional.ofNullable(sort).orElse("trending")) {
            case "stars":
                comparator = Comparator.comparing(
                        (RepoExploreDto d) -> Optional.ofNullable(d.getStargazersCount()).orElse(0)
                ).reversed();
                break;

            case "likes":
                comparator = Comparator.comparing(
                        (RepoExploreDto d) -> d.getLikes()
                ).reversed();
                break;

            case "new":
                comparator = Comparator.comparing(
                        (RepoExploreDto d) -> Optional.ofNullable(d.getUpdatedAt()).orElse(Instant.EPOCH)
                ).reversed();
                break;

            default: // trending → score
                comparator = Comparator.comparing((RepoExploreDto d) -> d.getScore()).reversed();
        }


        // Sort result
        List<RepoExploreDto> sorted = dtos.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        // Paging manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sorted.size());

        List<RepoExploreDto> content = start >= end ? List.of() : sorted.subList(start, end);

        return new PageImpl<>(content, pageable, sorted.size());
    }
}
