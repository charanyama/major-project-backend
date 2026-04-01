package com.virtualstore.indexingservice.exception;

public class EmbeddingException extends DbException {

    public EmbeddingException(String message) {
        super(message, "EMBEDDING_ERROR");
    }

    public EmbeddingException(String message, Throwable cause) {
        super(message, "EMBEDDING_ERROR", cause);
    }
}