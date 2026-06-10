package com.campus.modules.agent.config;

import javax.sql.DataSource;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableConfigurationProperties(RagProperties.class)
public class RagConfig {

    @Bean
    @ConditionalOnProperty(prefix = "campus.rag", name = "enabled", havingValue = "true")
    public DataSource ragPgvectorDataSource(RagProperties ragProperties) {
        RagProperties.Pgvector pgvector = ragProperties.getPgvector();
        return DataSourceBuilder.create()
                .url(pgvector.getJdbcUrl())
                .username(pgvector.getUsername())
                .password(pgvector.getPassword())
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "campus.rag", name = "enabled", havingValue = "true")
    public JdbcTemplate ragPgvectorJdbcTemplate(DataSource ragPgvectorDataSource) {
        return new JdbcTemplate(ragPgvectorDataSource);
    }

    @Bean
    @ConditionalOnProperty(prefix = "campus.rag", name = "enabled", havingValue = "true")
    public VectorStore ragVectorStore(
            JdbcTemplate ragPgvectorJdbcTemplate,
            EmbeddingModel embeddingModel,
            RagProperties ragProperties) {
        RagProperties.Pgvector pgvector = ragProperties.getPgvector();
        return PgVectorStore.builder(ragPgvectorJdbcTemplate, embeddingModel)
                .schemaName(pgvector.getSchemaName())
                .vectorTableName(pgvector.getTableName())
                .dimensions(pgvector.getDimensions())
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .initializeSchema(true)
                .build();
    }
}
