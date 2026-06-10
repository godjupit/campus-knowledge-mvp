package com.campus.modules.agent.service;

import com.campus.modules.agent.dto.AgentAskRequest;
import com.campus.modules.agent.dto.AgentAskResponse;
import com.campus.modules.agent.dto.AgentIndexResponse;

public interface AgentService {

    AgentAskResponse ask(AgentAskRequest request);

    AgentIndexResponse indexPosts(Integer limit);
}
