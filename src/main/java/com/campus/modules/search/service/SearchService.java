package com.campus.modules.search.service;

import com.campus.modules.knowledge.dto.PostSummaryResponse;

import java.util.List;

public interface SearchService {

    List<PostSummaryResponse> search(String keyword, Integer page, Integer size);

    List<PostSummaryResponse> hot();
}
