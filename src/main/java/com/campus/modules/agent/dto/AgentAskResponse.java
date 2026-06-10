package com.campus.modules.agent.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentAskResponse {

    private String answer;
    private List<AgentReferenceResponse> references;
}
