package com.osc.backend.repos;


import com.osc.backend.model.Repo;
import com.osc.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepoRepository extends JpaRepository<Repo, Long> {
    List<Repo> findByOwner(User owner);
    boolean existsByGithubIdAndOwner(Long githubId, User owner);
}
