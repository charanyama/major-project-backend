package com.virtualstore.indexingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@Data
@AllArgsConstructor
public class PineconeVectorDto {
    private String id;
    private List<Double> values;
    private Map<String, Object> metadata;
}