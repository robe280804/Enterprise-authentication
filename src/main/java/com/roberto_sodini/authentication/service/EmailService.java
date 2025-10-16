package com.roberto_sodini.authentication.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public void sendHtmlEmail(String templateName, String to, String subject, Map<String, Object> variables) {
        try {

            Context context = new Context();
            context.setVariables(variables);

            // Creo il contenuto in modo dinamico
            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage email = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(email, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(email);
            log.info("[SEND HTML EMAIL] Email inviata con successo a {} da {}", to, subject);

        } catch (MessagingException e) {
            log.error("[SEND HTML EMAIL] Errore nella creazione dell'email {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
