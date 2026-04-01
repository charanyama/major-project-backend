package com.virtualstore.indexingservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.virtualstore.indexingservice.dto.ProductDto;
import com.virtualstore.indexingservice.exception.ExternalApiException;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductFetchService {

    private final WebClient webClient;

    @Value("${external.product-api}")
    private String productApi;

    public ProductFetchService(WebClient externalApiClient) {
        this.webClient = externalApiClient;
    }

    public List<ProductDto> fetchProducts() {
        try {

            return Arrays.asList(
                    webClient.get()
                            .uri(productApi)
                            .retrieve()
                            .bodyToMono(ProductDto[].class)
                            .block());
        } catch (Exception e) {
            throw new ExternalApiException("Failed to fetch Products", e);
        }
    }
}