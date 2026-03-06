package com.campus.search.controller;

import com.campus.common.result.ApiResponse;
import com.campus.knowledge.dto.PostSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SearchController {

    @GetMapping("/search")
    public ApiResponse<List<PostSummaryResponse>> search(@RequestParam String keyword) {
        return ApiResponse.todo("TODO: 调用 SearchService 实现关键词搜索");
    }

    @GetMapping("/posts/hot")
    public ApiResponse<List<PostSummaryResponse>> hot() {
        return ApiResponse.todo("TODO: 调用 SearchService 实现热门内容");
    }
}
