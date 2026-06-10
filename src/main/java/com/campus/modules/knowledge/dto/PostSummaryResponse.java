package com.campus.modules.knowledge.dto;

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
    private Integer likeCount;
    private Integer favoriteCount;
}
