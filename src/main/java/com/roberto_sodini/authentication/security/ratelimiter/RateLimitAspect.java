package com.roberto_sodini.authentication.security.ratelimiter;

import com.roberto_sodini.authentication.exceptions.RateLimitExceed;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;


/// Aspect -> annotazione di Spring AOP che aggiunge logica trasversale
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {

    private final RedisRateLimiter redisRateLimiter;
    private final HttpServletRequest httpServletRequest;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * <p> Il metodo esegue i seguenti step: </p>
     * <ul>
     *     <li> Ottengo il metodo su cui uso l'annotazione attraverso getSignature() </li>
     *     <li> Costruisco un oggetto del metodo, in modo da ottenere info come le sue annotazioni, il suo return e il nome </li>
     *     <li> Ottengo l'istanza dell'annotazione RateLimit dal metodo </li>
     *     <li> Ottengo l'indirizzo ip del client </li>
     *     <li> Costruisco una chiave specifica per il client e l' endpoint </li>
     *     <li> Chiamo il metodo isAllowed per verificare il numero di richiesta, se ritorna false ritorno un eccezione </li>
     * </ul>
     * @param joinPoint permette di accedere al metodo dove viene usata l'annotazione RateLimit, di eseguirlo o non
     * @return il metodo su cui viene messa l'annotazione
     * @exception RateLimitExceed se il client oltrepassa le richieste
     */
    @Around("@annotation(RateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        String clientIp = httpServletRequest.getRemoteAddr();
        String key = "ratelimit:" + clientIp + "method:" + method.getName();
        String levelKey = "rl:" + key + ":level";

        boolean allowed = redisRateLimiter.isAllowed(key, rateLimit.limit(), rateLimit.timesWindowSecond());

        if (!allowed) {
            Long time = stringRedisTemplate.getExpire(levelKey);
            log.warn("[RATE LIMITER] Utente {} bloccato per {} secondi", clientIp, time);
            throw new RateLimitExceed("Troppe richieste, riprova tra " + time + " secondi");
        }

        return  joinPoint.proceed();
    }
}
