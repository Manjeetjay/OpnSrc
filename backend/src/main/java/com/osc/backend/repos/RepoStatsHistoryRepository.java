package com.osc.backend.repos;

import com.osc.backend.model.Repo;
import com.osc.backend.model.RepoStatsHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RepoStatsHistoryRepository extends JpaRepository<RepoStatsHistory, Long> {

    List<RepoStatsHistory> findByRepoAndDateBetween(Repo repo, Instant start, Instant end);

    List<RepoStatsHistory> findByRepoOrderByDateDesc(Repo repo);
}
