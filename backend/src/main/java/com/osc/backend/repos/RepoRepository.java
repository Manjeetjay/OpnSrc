package com.osc.backend.repos;


import com.osc.backend.model.Repo;
import com.osc.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepoRepository extends JpaRepository<Repo, Long> {
    List<Repo> findByOwner(User owner);
    boolean existsByGithubIdAndOwner(Long githubId, User owner);
    List<Repo> findAllByPublishedOnPlatformTrue();

}
