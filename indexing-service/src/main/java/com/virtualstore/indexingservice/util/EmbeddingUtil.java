package com.virtualstore.indexingservice.util;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;

@Component
public class EmbeddingUtil {

    private final WebClient webClient;

    public EmbeddingUtil(@Qualifier("externalAPIClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public List<Double> generateEmbedding(String text) {
        Map<String, Object> request = Map.of(
                "model", "nomic-embed-text",
                "prompt", text // Ollama uses "prompt", not "input"
        );

        Map<String, Object> response = webClient.post()
                .uri("http://localhost:11434/api/embeddings")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();

        // Ollama returns { "embedding": [...] } directly, not nested under "data"
        return (List<Double>) response.get("embedding");
    }
}