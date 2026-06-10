package com.campus.modules.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "campus.rag")
public class RagProperties {

    private boolean enabled = false;
    private int topK = 5;
    private int indexBatchSize = 100;
    private Pgvector pgvector = new Pgvector();

    @Data
    public static class Pgvector {
        private String jdbcUrl = "jdbc:postgresql://127.0.0.1:5432/campus_rag";
        private String username = "postgres";
        private String password = "postgres";
        private String schemaName = "public";
        private String tableName = "campus_post_vectors";
        private int dimensions = 1536;
    }
}
