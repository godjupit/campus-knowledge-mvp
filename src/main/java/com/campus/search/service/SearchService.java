package com.campus.search.service;

import com.campus.knowledge.dto.PostSummaryResponse;

import java.util.List;

public interface SearchService {

    List<PostSummaryResponse> search(String keyword, Integer page, Integer size);

    List<PostSummaryResponse> hot();
}
