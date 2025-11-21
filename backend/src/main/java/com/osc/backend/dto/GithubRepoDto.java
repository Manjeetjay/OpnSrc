package com.osc.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GithubRepoDto {
    private Long id;
    private String name;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("html_url")
    private String htmlUrl;
    private String description;
    private String language;
    @JsonProperty("stargazers_count")
    private Integer stargazersCount;
}

