package com.vectorstore.cartservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class MergeCartRequest {
    private List<MergeItem> items;

    @Data
    public static class MergeItem {
        private String productId;
        private Integer quantity;
    }
}