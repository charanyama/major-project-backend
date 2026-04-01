package com.virtualstore.indexingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UpsertRequest {
    private List<PineconeVectorDto> vectors;
}