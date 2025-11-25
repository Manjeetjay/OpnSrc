package com.osc.backend.controller;

import com.osc.backend.dto.RepoExploreDto;
import com.osc.backend.service.ExploreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/explore")
@RequiredArgsConstructor
public class ExploreController {

    private final ExploreService exploreService;

    @GetMapping
    public Page<RepoExploreDto> explore(
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return exploreService.getExplore(sort, PageRequest.of(page, size));
    }
}
