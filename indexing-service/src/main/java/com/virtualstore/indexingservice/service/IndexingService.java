package com.virtualstore.indexingservice.service;

import com.virtualstore.indexingservice.dto.PineconeVectorDto;
import com.virtualstore.indexingservice.dto.ProductDto;
import com.virtualstore.indexingservice.client.PineconeClient;
import com.virtualstore.indexingservice.dto.UpsertRequest;
import com.virtualstore.indexingservice.util.EmbeddingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IndexingService {

    private final PineconeClient pineconeClient;

    @Autowired
    private EmbeddingUtil embeddingUtil;

    public IndexingService(PineconeClient pineconeClient) {
        this.pineconeClient = pineconeClient;
    }

    public void indexProducts(List<ProductDto> products) {

        List<PineconeVectorDto> vectors = new ArrayList<>();

        for (ProductDto product : products) {

            // 1. Build unified semantic text (IMPORTANT)
            String text = this.buildProductText(product);

            // 2. Generate embedding from full text
        List<Double> embedding = embeddingUtil.generateEmbedding(text);

            // 3. Metadata (for filtering + display)
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("text", text); 
            metadata.put("name", product.getTitle());
            metadata.put("category", product.getCategory());
            metadata.put("price", product.getPrice());

            vectors.add(new PineconeVectorDto(product.getId(), embedding, metadata));
        }

        pineconeClient.upsertVectors(new UpsertRequest(vectors));
    }

    private String buildProductText(ProductDto product) {
        return String.format(
                "Product Name: %s. Description: %s. Category: %s. Price: %s",
                this.safe(product.getTitle()),
                this.safe(product.getCategory()),
                product.getPrice()
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public void deleteProduct(String id) {
        pineconeClient.deleteVector(id);
    }
}