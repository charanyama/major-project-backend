package com.vectorstore.mailservice.dto;

import lombok.Data;

@Data
public class EmailRequest {

    private String email;
    private String name;

}