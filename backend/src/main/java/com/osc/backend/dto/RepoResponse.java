package com.osc.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RepoResponse {
    private Long id;
    private Long githubId;
    private String name;
    private String fullName;
    private String htmlUrl;
    private String description;
    private String language;
    private Integer stargazersCount;
    private Boolean publishedOnPlatform;
    private Instant selectedAt;
}

