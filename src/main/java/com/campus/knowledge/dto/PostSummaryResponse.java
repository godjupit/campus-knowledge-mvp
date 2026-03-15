package com.campus.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryResponse {
    private Long id;
    private String title;
    private String content;
    private String tags;
}
