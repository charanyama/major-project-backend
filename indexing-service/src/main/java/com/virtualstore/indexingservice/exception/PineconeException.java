package com.virtualstore.indexingservice.exception;

public class PineconeException extends DbException {

    public PineconeException(String message) {
        super(message, "PINECONE_ERROR");
    }

    public PineconeException(String message, Throwable cause) {
        super(message, "PINECONE_ERROR", cause);
    }
}