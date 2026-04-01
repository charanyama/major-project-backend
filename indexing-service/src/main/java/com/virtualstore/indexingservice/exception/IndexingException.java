package com.virtualstore.indexingservice.exception;

public class IndexingException extends DbException {

    public IndexingException(String message) {
        super(message, "INDEXING_ERROR");
    }

    public IndexingException(String message, Throwable cause) {
        super(message, "INDEXING_ERROR", cause);
    }
}