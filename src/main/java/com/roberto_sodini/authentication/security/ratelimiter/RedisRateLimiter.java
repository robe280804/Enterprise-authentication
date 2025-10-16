package com.roberto_sodini.authentication.security.ratelimiter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisRateLimiter {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * <p> Il metodo esegue i seguenti step: </p>
     * <ul>
     *     <li> Incremento un contatore in redis associato alla key passata (ogni volta che verrà chiamato il metodo) </li>
     *     <li> Se count = null, redis non lo avrà inizializzato correttamente e ritorno false</li>
     *     <li> Se è la prima chiamata, si imposta un tempo di scadenza </li>
     *     <li> Una volta che il tempo scade, Redis eliminà la chiave e imposta il contatore a 0 </li>
     *     <li> Ritorna true se il contatore è <= del limite, altrimenti false </li>
     * </ul>
     * @param key identificatore dell'utente
     * @param limit limite delle richieste
     * @param timesWindowSecond durata della finestra temporale
     * @return true se il contatore è minore del limite, false se maggiore
     * @exception DataAccessException per errori dovuti da redis
     */
    public boolean isAllowed(String key, int limit, int timesWindowSecond){
        try {
            Long count = stringRedisTemplate.opsForValue().increment(key);

            if (Objects.isNull(count)) return false;

            if (count == 1) {
                stringRedisTemplate.expire(key, Duration.ofSeconds(timesWindowSecond));
            }

            return count <= limit;

        } catch (DataAccessException ex){
            log.error("[REDIS RATE LIMITER] Impossibile valutare il rate limiter", ex);
            return false;
        }
    }
}
