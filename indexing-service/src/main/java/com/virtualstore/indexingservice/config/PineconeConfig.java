package com.virtualstore.indexingservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PineconeConfig {

    @Value("${spring.ai.virtualstore.pinecone.api-key}")
    private String apiKey;

    @Value("${spring.ai.virtualstore.pinecone.host}")
    private String host;

    @Bean("pineconeWebClient")
    @Primary
    public WebClient pineconeWebClient() {
        return WebClient.builder()
                .baseUrl(host)
                .defaultHeader("Api-Key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}