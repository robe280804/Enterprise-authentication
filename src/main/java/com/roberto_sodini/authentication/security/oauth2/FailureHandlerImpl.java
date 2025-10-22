package com.roberto_sodini.authentication.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
public class FailureHandlerImpl implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String provider = Optional.ofNullable(request.getParameter("provider")).orElse("sconosciuto");

        log.warn("[OAUTH2 FAILURE] Provider {} | Reason {}", provider, exception.getMessage());

        String redirectUlr = "http://localhost:5173/login";

        response.sendRedirect(redirectUlr);
    }
}
