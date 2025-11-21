package com.osc.backend.dto;

import lombok.Data;

@Data
public class PublishRepoRequest {
    private Long githubId;
    private String name;
    private String description;
}
