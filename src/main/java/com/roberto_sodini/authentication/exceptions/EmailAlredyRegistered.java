package com.roberto_sodini.authentication.exceptions;

public class EmailAlredyRegistered extends RuntimeException {
    public EmailAlredyRegistered(String message) {
        super(message);
    }
}
