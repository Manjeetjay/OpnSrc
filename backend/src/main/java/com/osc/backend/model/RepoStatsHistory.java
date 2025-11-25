package com.osc.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "repo_stats_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RepoStatsHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id")
    private Repo repo;

    private Instant date;            // snapshot date
    private Integer commits;         // commits in last 24 hours
    private Integer stars;           // current stars count
    private Integer forks;           // current forks count
}
