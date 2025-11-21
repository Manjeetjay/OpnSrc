package com.osc.backend.controller;

import com.osc.backend.dto.GithubRepoDto;
import com.osc.backend.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GithubController {

    private final GithubService githubService;

    @GetMapping("/repos/{username}")
    public List<GithubRepoDto> getUserRepos(@PathVariable String username) {
        return githubService.fetchPublicRepos(username);
    }
}

