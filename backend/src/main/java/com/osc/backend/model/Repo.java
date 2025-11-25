package com.osc.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "repos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Repo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who published this on our platform
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "github_id", nullable = false)
    private Long githubId;

    private String name;

    private String fullName;

    private String htmlUrl;

    private Integer forksCount;

    private Instant updatedAt;

    @Column(length = 2000)
    private String description;

    private String language;

    private Integer stargazersCount;

    private Boolean publishedOnPlatform = false;

    private Instant selectedAt;
}
