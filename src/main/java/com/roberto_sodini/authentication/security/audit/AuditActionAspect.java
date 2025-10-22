package com.roberto_sodini.authentication.security.audit;

import com.roberto_sodini.authentication.model.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class AuditActionAspect {

    @Around("@annotation(AuditAction)")
    public Object AuditAspectMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditAction auditAction = method.getAnnotation(AuditAction.class);

        String userIdentifier =  getUserName();

        if (userIdentifier.equalsIgnoreCase("non autenticato") || userIdentifier.equalsIgnoreCase("anonymousUser")){
            userIdentifier = extractUserFromParam(joinPoint.getArgs());
        }

        log.info("[AUDIT] User {} sta eseguendo {}",userIdentifier, auditAction.action());

        try {

            Object result = joinPoint.proceed();

            if (auditAction.logFinalResault()){
                log.info("[AUDIT] User {} ha completato con successo {} con risultato {}",
                        userIdentifier, auditAction.action(), result);
            } else {
                log.info("[AUDIT] User {} ha completato con successo {}",
                        userIdentifier, auditAction.action());
            }

            return result;

        } catch (Throwable ex){
            log.error("User {} ha tentato {} ma è fallito {}",
                    userIdentifier, auditAction.action(), ex.getMessage(), ex);
            throw ex;
        }
    }

    private String extractUserFromParam(Object[] args) {
        for (Object arg : args) {
            if (arg == null) continue;
            for (Field field : arg.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(AuditUserField.class)) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(arg);
                        if (value != null) return value.toString();
                    } catch (IllegalAccessException e) {
                        log.warn("Impossibile accedere al campo annotato @AuditUserField", e);
                    }
                }
            }
        }
        return "Non dichiarato";
    }

        private String getUserName () {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : "Non autenticato";
        }

}
