package com.campus.modules.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgentAskRequest {

    @NotBlank(message = "question cannot be blank")
    private String question;
}
