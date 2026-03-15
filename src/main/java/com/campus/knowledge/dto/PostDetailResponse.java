package com.campus.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailResponse {
    private Long id;
    private String title;
    private String content;
    private String tags;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
}
