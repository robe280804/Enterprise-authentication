package com.roberto_sodini.authentication.security.ratelimiter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
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
     *     <li> Creo tre chiavi, una per il contatore e una per il livello di blocco persistente e una che ha il tempo del blocco </li>
     *     <li> Ottengo il valore memorizzato per la chiave block-key, se null la inizializzo a 0 altrimenti prende il valore </li>
     *     <li> Controllo se la chiave level-key è scaduta, altrimenti l'utente è ancora bloccato </li>
     *     <li> Incremento un contatore in redis associato alla count-key </li>
     *     <li> Se count = null, redis non lo avrà inizializzato correttamente e ritorno false</li>
     *     <li> Se è la prima chiamata, si imposta un tempo di scadenza </li>
     *     <li> Se il count è > del limit, incremento il block-level e salvo il suo valore riferito alla chiave block-key </li>
     *     <li> Definisco per ogni livello di blocco un tempo in secondi in cui l'utente non potrà eseguire quella richiesta </li>
     *     <li> Definisco l' expiration della chiave come il tempo del livello di blocco ed elimino quella del contatore </li>
     * </ul>
     *
     * @param key identificatore dell'utente
     * @param limit limite delle richieste
     * @param timesWindowSecond durata della finestra temporale
     * @return true se il contatore è minore del limite e non sono presenti block level,
     *         false se sono presentii block level, se il contatore non viene inizializzato e se il contatore > limite
     * @throws DataAccessException per errori dovuti da redis
     */
    public boolean isAllowed(String key, int limit, int timesWindowSecond) {
        try {
            String countKey = "rl:" + key + ":count";   // Chiave per il contatore
            String levelKey = "rl:" + key + ":level";   // Chiave per il livello di blocco non persistente
            String blockKey = "rl:" + key + ":block";   // Chiave per il livello di blocco persistente

            String blockValue = stringRedisTemplate.opsForValue().get(blockKey);
            int blockLevel = (blockValue == null) ? 0 : Integer.parseInt(blockValue);


            // Se l'utente ha già un blocco e non è ancora scaduto ritorna false
            Long remainingBlockTime = stringRedisTemplate.getExpire(levelKey);
            if (remainingBlockTime > 0) {
               return false;
            }

            Long count = stringRedisTemplate.opsForValue().increment(countKey);

            if (Objects.isNull(count)) return false;

            // Imposto expire contatore alla prima richiesta
            if (count == 1) {
                stringRedisTemplate.expire(key, Duration.ofSeconds(timesWindowSecond));
            }

            if (count > limit) {
                // Incremento il livello di blocco
                blockLevel++;

                long blockSecond;
                switch (blockLevel) {
                    case 1 -> blockSecond = 60;   // 1 minuto
                    case 2 -> blockSecond = 120;  // 2 minuti
                    case 3 -> blockSecond = 300;  // 5 minuti
                    default -> blockSecond = 600; // 10 minuti
                }

                // Imposto la scadenza della chiave riferente al livello di blocco
                stringRedisTemplate.opsForValue().set(blockKey, String.valueOf(blockLevel), Duration.ofMinutes(5));  // Ogni 10 minuti si resetta
                stringRedisTemplate.opsForValue().set(levelKey, "blocked", Duration.ofSeconds(blockSecond));
                stringRedisTemplate.delete(countKey);

                return false;
            }
            return true;

        } catch (RedisConnectionFailureException ex) {
            log.warn("[REDIS RATE LIMITER] Redis non disponibile {}", ex.getMessage());
            return true; // In caso di fallimento Redis, permettiamo la richiesta
        } catch (DataAccessException ex) {
            log.error("[REDIS RATE LIMITER] Errore di accesso ai dati Redis", ex);
            return true; // In caso di altri errori Redis, permettiamo la richiesta
        } catch (Exception ex) {
            log.error("[REDIS RATE LIMITER] Errore imprevisto", ex);
            return true; // In caso di errori imprevisti, permettiamo la richiesta
        }
    }
}
