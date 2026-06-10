package com.campus.modules.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentReferenceResponse {

    private Long postId;
    private String title;
    private String tags;
    private String contentPreview;
}
