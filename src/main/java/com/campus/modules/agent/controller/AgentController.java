package com.campus.modules.agent.controller;

import com.campus.common.ratelimit.RedisRateLimitService;
import com.campus.common.result.ApiResponse;
import com.campus.modules.agent.dto.AgentAskRequest;
import com.campus.modules.agent.dto.AgentAskResponse;
import com.campus.modules.agent.dto.AgentIndexResponse;
import com.campus.modules.agent.service.AgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private static final int AGENT_ASK_RATE_LIMIT = 20;
    private static final Duration AGENT_ASK_RATE_LIMIT_WINDOW = Duration.ofMinutes(1);

    private final AgentService agentService;
    private final RedisRateLimitService redisRateLimitService;

    @PostMapping("/ask")
    public ApiResponse<AgentAskResponse> ask(@Valid @RequestBody AgentAskRequest request) {
        redisRateLimitService.checkIpLimit("agent:ask", AGENT_ASK_RATE_LIMIT, AGENT_ASK_RATE_LIMIT_WINDOW);
        return ApiResponse.ok(agentService.ask(request));
    }

    @PostMapping("/index-posts")
    public ApiResponse<AgentIndexResponse> indexPosts(@RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(agentService.indexPosts(limit));
    }
}
