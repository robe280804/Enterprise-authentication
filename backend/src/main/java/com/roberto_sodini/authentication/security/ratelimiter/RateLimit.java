package com.roberto_sodini.authentication.security.ratelimiter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Custom Annotation
/// Target = applicabile solo sui metodi
/// Retention = annotazione disponibile a runtime
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int limit();
    int timesWindowSecond();
}
