package com.roberto_sodini.authentication.exceptions;

public class WrongAuthProvider extends RuntimeException {
    public WrongAuthProvider(String message) {
        super(message);
    }
}
