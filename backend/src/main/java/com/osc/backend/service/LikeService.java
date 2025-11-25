package com.osc.backend.service;


import com.osc.backend.model.Like;
import com.osc.backend.model.Repo;
import com.osc.backend.model.User;
import com.osc.backend.repos.LikeRepository;
import com.osc.backend.repos.RepoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final RepoRepository repoRepository;

    public long toggleLike(User user, Long repoId) {
        Repo repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new RuntimeException("Repo not found"));

        var existing = likeRepository.findByUserAndRepo(user, repo);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
        } else {
            Like like = Like.builder()
                    .user(user)
                    .repo(repo)
                    .createdAt(Instant.now())
                    .build();
            likeRepository.save(like);
        }
        return likeRepository.countByRepo(repo);
    }

    public long countLikes(Repo repo) {
        return likeRepository.countByRepo(repo);
    }

    public boolean userLiked(User user, Repo repo) {
        return likeRepository.existsByUserAndRepo(user, repo);
    }
}
