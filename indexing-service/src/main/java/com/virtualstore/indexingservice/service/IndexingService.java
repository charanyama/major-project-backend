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

        for (ProductDto product: products) {
            String text = product.getName() + " " + product.getDescription();

            List<Double> embedding = embeddingUtil.generateEmbedding(text);

            Map<String, Object> metadata = new HashMap<>();

            metadata.put("name", product.getName());
            metadata.put(("category"), product.getCategory());
            metadata.put("price", product.getPrice());

            vectors.add(new PineconeVectorDto(product.getId(), embedding, metadata));
        }

        pineconeClient.upsertVectors(new UpsertRequest(vectors));
    }

    public void deleteProduct(String id) {
        pineconeClient.deleteVector(id);
    }
}