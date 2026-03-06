package com.campus.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostSummaryResponse {
    private Long id;
    private String title;
    private String tags;
}
