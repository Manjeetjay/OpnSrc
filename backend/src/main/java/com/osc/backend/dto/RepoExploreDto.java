package com.osc.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RepoExploreDto {
    private Long id;
    private Long githubId;
    private String name;
    private String fullName;
    private String htmlUrl;
    private String description;
    private String language;
    private Integer stargazersCount;
    private Integer forksCount;
    private Instant updatedAt;
    private Long likes;
    private Double score;
    private String ownerDisplayName;
    private String ownerGithubUsername;
}

