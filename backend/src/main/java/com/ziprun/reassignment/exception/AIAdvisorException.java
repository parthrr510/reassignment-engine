package com.ziprun.reassignment.exception;

public class AIAdvisorException extends RuntimeException {
    public AIAdvisorException(String message) {
        super(message);
    }

    public AIAdvisorException(String message, Throwable cause) {
        super(message, cause);
    }
}
