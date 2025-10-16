package com.roberto_sodini.authentication.exceptions;

public class RateLimitExceed extends RuntimeException {
    public RateLimitExceed(String message) {
        super(message);
    }
}
