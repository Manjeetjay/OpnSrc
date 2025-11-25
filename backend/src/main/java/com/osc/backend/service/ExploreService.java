package com.osc.backend.service;

import com.osc.backend.dto.RepoExploreDto;
import com.osc.backend.model.Repo;
import com.osc.backend.repos.RepoRepository;
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

    // Tunable weights
    private final double W_LIKES = 0.35;
    private final double W_STARS = 0.45;
    private final double W_RECENCY = 0.20;

    public Page<RepoExploreDto> getExplore(String sort, Pageable pageable) {
        List<Repo> all = repoRepository.findAllByPublishedOnPlatformTrue();
        if (all.isEmpty()) return new PageImpl<>(List.of(), pageable, 0);

        // Gather metrics
        Map<Long, Long> likesMap = new HashMap<>();
        Map<Long, Integer> starsMap = new HashMap<>();
        Map<Long, Double> recencyMap = new HashMap<>();

        Instant now = Instant.now();
        int maxLikes = 1;
        int maxStars = 1;
        double maxRecencyScore = 1.0;

        for (Repo r : all) {
            long likes = likeService.countLikes(r);
            likesMap.put(r.getId(), likes);
            maxLikes = Math.max(maxLikes, (int) likes);

            int stars = r.getStargazersCount() == null ? 0 : r.getStargazersCount();
            starsMap.put(r.getId(), stars);
            maxStars = Math.max(maxStars, stars);

            // recency: newer updatedAt -> higher score
            double recencyScore = 0.0;
            if (r.getUpdatedAt() != null) {
                long days = Duration.between(r.getUpdatedAt(), now).toDays();
                // invert days -> recent = higher. Use exponential decay
                recencyScore = Math.max(0.0, Math.exp(-days / 30.0)); // 30-day scale
            }
            recencyMap.put(r.getId(), recencyScore);
            maxRecencyScore = Math.max(maxRecencyScore, recencyScore);
        }

        // Build DTOs with normalized metrics and score
        int finalMaxLikes = maxLikes;
        int finalMaxStars = maxStars;
        double finalMaxRecencyScore = maxRecencyScore;
        List<RepoExploreDto> dtos = all.stream().map(r -> {
            long likes = likesMap.getOrDefault(r.getId(), 0L);
            int stars = starsMap.getOrDefault(r.getId(), 0);
            double recencyScore = recencyMap.getOrDefault(r.getId(), 0.0);

            double normLikes = (double) likes / (double) finalMaxLikes;
            double normStars = finalMaxStars == 0 ? 0.0 : (double) stars / (double) finalMaxStars;
            double normRecency = finalMaxRecencyScore == 0.0 ? 0.0 : recencyScore / finalMaxRecencyScore;

            double score = W_LIKES * normLikes + W_STARS * normStars + W_RECENCY * normRecency;

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
        }).collect(Collectors.toList());

        // Sorting
        Comparator<RepoExploreDto> comparator;
        switch (Optional.ofNullable(sort).orElse("trending")) {
            case "stars":
                comparator = Comparator.comparing((RepoExploreDto d) -> Optional.ofNullable(d.getStargazersCount()).orElse(0)).reversed();
                break;
            case "likes":
                comparator = Comparator.comparing(RepoExploreDto::getLikes).reversed();
                break;
            case "new":
                comparator = Comparator.comparing((RepoExploreDto d) -> Optional.ofNullable(d.getUpdatedAt()).orElse(Instant.EPOCH)).reversed();
                break;
            default: // trending
                comparator = Comparator.comparing(RepoExploreDto::getScore).reversed();
        }

        List<RepoExploreDto> sorted = dtos.stream().sorted(comparator).collect(Collectors.toList());

        // Paging manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sorted.size());
        List<RepoExploreDto> content = start > end ? List.of() : sorted.subList(start, end);

        return new PageImpl<>(content, pageable, sorted.size());
    }
}
