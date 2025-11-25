package com.osc.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class RepoStatsHistoryDto {
    private Instant date;
    private int commits;
    private int stars;
    private int forks;
}
