package com.roberto_sodini.authentication.service;

import com.roberto_sodini.authentication.model.RegisterToken;
import com.roberto_sodini.authentication.model.User;
import com.roberto_sodini.authentication.repository.RegisterTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegisterTokenService {

    private final RegisterTokenRepository registerTokenRepository;

    public void createRegisterToken(User user){

        List<RegisterToken> userToken =  registerTokenRepository.findAll();

        if (!userToken.isEmpty()){
            userToken.forEach(token -> {
                token.setRevoked(true);
            });
        }
        String token = 
       /// Metto revoked=true tutti i token che aveva prima l utente
    }
}
