package com.roberto_sodini.authentication.exceptions;

public class TokenExpired extends RuntimeException {
    public TokenExpired(String message) {
        super(message);
    }
}
