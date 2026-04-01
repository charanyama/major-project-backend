package com.virtualstore.indexingservice.client;

import com.virtualstore.indexingservice.dto.UpsertRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class PineconeClient {

    private final WebClient webClient;

    public PineconeClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public void upsertVectors(UpsertRequest request) {
        webClient.post()
                .uri("/vectors/upsert")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public void deleteVector(String id) {
        webClient.post()
                .uri("/vectors/delete")
                .bodyValue(Map.of("ids", List.of(id)))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}