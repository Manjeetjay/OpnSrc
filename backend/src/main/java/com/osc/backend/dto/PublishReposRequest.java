package com.osc.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class PublishReposRequest {
    private List<PublishRepoRequest> repos;
}
