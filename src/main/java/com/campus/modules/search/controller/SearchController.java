package com.campus.modules.search.controller;

import com.campus.common.result.ApiResponse;
import com.campus.modules.knowledge.dto.PostSummaryResponse;
import com.campus.modules.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public ApiResponse<List<PostSummaryResponse>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ApiResponse.ok(searchService.search(keyword, page, size));
    }

    @GetMapping("/posts/hot")
    public ApiResponse<List<PostSummaryResponse>> hot() {
        return ApiResponse.ok(searchService.hot());
    }
}
