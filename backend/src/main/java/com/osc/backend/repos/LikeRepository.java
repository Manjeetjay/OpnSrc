package com.osc.backend.repos;

import com.osc.backend.model.Like;
import com.osc.backend.model.Repo;
import com.osc.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    long countByRepo(Repo repo);
    Optional<Like> findByUserAndRepo(User user, Repo repo);
    boolean existsByUserAndRepo(User user, Repo repo);
}

