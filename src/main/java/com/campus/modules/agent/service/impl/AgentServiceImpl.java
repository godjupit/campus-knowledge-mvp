package com.campus.modules.agent.service.impl;

import com.campus.modules.agent.config.RagProperties;
import com.campus.modules.agent.dto.AgentAskRequest;
import com.campus.modules.agent.dto.AgentAskResponse;
import com.campus.modules.agent.dto.AgentIndexResponse;
import com.campus.modules.agent.dto.AgentReferenceResponse;
import com.campus.modules.agent.service.AgentService;
import com.campus.modules.knowledge.dto.PostSummaryResponse;
import com.campus.modules.knowledge.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentServiceImpl implements AgentService {

    private final PostMapper postMapper;
    private final RagProperties ragProperties;
    private final ObjectProvider<VectorStore> vectorStoreProvider;
    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    @Override
    public AgentAskResponse ask(AgentAskRequest request) {
        String question = request.getQuestion().trim();
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        ChatClient.Builder chatClientBuilder = chatClientBuilderProvider.getIfAvailable();

        if (ragProperties.isEnabled() && vectorStore != null && chatClientBuilder != null) {
            return askWithRag(question, vectorStore, chatClientBuilder.build());
        }

        return askWithKeywordFallback(question);
    }

    @Override
    public AgentIndexResponse indexPosts(Integer limit) {
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (!ragProperties.isEnabled() || vectorStore == null) {
            log.warn("RAG index skipped because campus.rag.enabled=false or VectorStore is unavailable");
            return new AgentIndexResponse(0);
        }

        int safeLimit = limit == null || limit < 1 ? ragProperties.getIndexBatchSize() : Math.min(limit, 500);
        List<PostSummaryResponse> posts = postMapper.selectPostsForRag(safeLimit);
        List<Document> documents = posts.stream()
                .map(this::toDocument)
                .toList();

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
        }
        return new AgentIndexResponse(documents.size());
    }

    private AgentAskResponse askWithRag(String question, VectorStore vectorStore, ChatClient chatClient) {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(question)
                .topK(ragProperties.getTopK())
                .build());

        List<AgentReferenceResponse> references = documents.stream()
                .map(this::toReference)
                .toList();

        if (documents.isEmpty()) {
            return new AgentAskResponse("没有检索到相关帖子内容，建议换一个更具体的问题。", references);
        }

        String context = documents.stream()
                .map(Document::getText)
                .filter(Objects::nonNull)
                .reduce("", (left, right) -> left + "\n\n" + right);

        String answer = chatClient.prompt()
                .system("""
                        You are a campus knowledge assistant.
                        Answer in Chinese.
                        Only answer based on the retrieved post context.
                        If the context is insufficient, say that no reliable answer can be found.
                        """)
                .user("""
                        Question:
                        %s

                        Retrieved post context:
                        %s
                        """.formatted(question, context))
                .call()
                .content();

        return new AgentAskResponse(answer, references);
    }

    private AgentAskResponse askWithKeywordFallback(String question) {
        List<PostSummaryResponse> posts = postMapper.searchPosts(question, 0, ragProperties.getTopK());
        List<AgentReferenceResponse> references = posts.stream()
                .map(this::toReference)
                .toList();

        String answer = posts.isEmpty()
                ? "RAG 当前未启用，关键词检索也没有找到相关帖子。请配置 OpenAI API Key 和 pgvector 后开启 campus.rag.enabled。"
                : "RAG 当前未启用，已先返回关键词检索结果。配置 OpenAI API Key 和 pgvector 后，可以生成基于向量检索的智能回答。";
        return new AgentAskResponse(answer, references);
    }

    private Document toDocument(PostSummaryResponse post) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("postId", post.getId());
        metadata.put("title", safeText(post.getTitle()));
        metadata.put("tags", safeText(post.getTags()));

        String text = """
                Title: %s
                Tags: %s
                Content: %s
                """.formatted(safeText(post.getTitle()), safeText(post.getTags()), safeText(post.getContent()));
        return new Document("post-" + post.getId(), text, metadata);
    }

    private AgentReferenceResponse toReference(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        return new AgentReferenceResponse(
                toLong(metadata.get("postId")),
                String.valueOf(metadata.getOrDefault("title", "")),
                String.valueOf(metadata.getOrDefault("tags", "")),
                preview(document.getText())
        );
    }

    private AgentReferenceResponse toReference(PostSummaryResponse post) {
        return new AgentReferenceResponse(
                post.getId(),
                post.getTitle(),
                post.getTags(),
                preview(post.getContent())
        );
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String preview(String value) {
        String text = safeText(value).replaceAll("\\s+", " ").trim();
        return text.length() <= 120 ? text : text.substring(0, 120) + "...";
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
