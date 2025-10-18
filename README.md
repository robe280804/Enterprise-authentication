# Autenticazione enterprise

## Flusso Registrazione utente
-  Il client invia email e password validati dal DTO.
-  Controllo che l'email non sia già registrata nel db, nel caso lancio un eccezione.
-  Creo un model Email Verification Token che contiene un token hashato a breve scadenza e i dati dell'utente per la registrazione.
-  Invio l'email all'utente dove è presente un link + token che lo reindirizzerà al server.
-  Il server verifica che il token sia esistente e non sia scaduto, se scaduto la registrazione non viene eseguita.
-  Creo il nuovo utente e lo salvo nel database, ottenendo i suoi dati dal modello salvato nel db.
- Tutti gli eventi importanti (creazione token, invio email, conferma registrazione, errori) sono loggati in modo strutturato.

## Autenticazione utente
- Se l'autenticazione dell' utente va a buon fine, genero un access token e un refresh token
- Attraverso kafka invio: - un event per salvare nel db il login dell'utente (login history)
                          - un event per salvare nel db il refresh token
- Utilizzo kafka perchè non sono eventi critici che motivano il blocco del login dell'utente
- Salvo il refresh token in un cookie
- Invio all'utente l'access token che verrà inviato a ogni richiesta


# Security 

## Rate liming
**Limito il numero di chiamate che il client può fare a un endpoint, proteggendo il sistema da attacchi DoS**
- Annotazione personalizzata RateLimit da usare sopra i metodi del controller, accetta il limite di richieste e il varco temporale
- Aggiungo della logica all'annotazione attraverso il RedisLimitAspect dove ottengo l'ip dell'utente e altri dati del metodo su cui 
    viene usata l'annotazione.
- Utilizzo il RedisRateLimiter per tenere traccia dei tentativi


# Implementazioni della sicurezza future
CAPTCHA / reCAPTCHA (step up dopo N tentativi o sempre su registrazione)
Throttling per email (resend) e cooldown (es. 3 resi/ora)
Lockout progressive / exponential backoff (progressive delay su tentativi ripetuti)
Honeypot/hidden field per bot semplici
Password policy & hashing forte (BCrypt/Argon2, min length, checks)
Email normalization & uniqueness check (normalizza prima di check duplicati)
Token protections (hash token, breve expiry, single‑use) — già veduto
IP reputation / blocklist / WAF (Cloud WAF, fail2ban, Cloudflare)
Logging strutturato, metriche e alerting (Prometheus/Grafana, alert su spike)
Async logging/audit (Kafka) con retries e DLQ) — non blocca flusso principale
Device fingerprinting / rate per device (opzionale per alto rischio)
MFA & verification step (se vuoi aumentare la sicurezza)
CAP on account‑creation throughput (per evitare account farming)

## Endpoint

- **api/auth/register** | (POST) | (ROLE: ANY) | Metodo per la registrazione e invio email per la conferma
- **api/auth/confirm-register** | (GET) |(ROLE: ANY) | Metodo per confermare la registrazione

# Modelli database

## User
- **id**
- **email** (String)
- **password** (String)
- **roles** (Set)
- **authProvider **
- **createdAt** (LocalDateTime)
- **updatedAt** (LocalDateTime)

## Email verification token
- *id**
- **userEmail** (String) 
- **userPassword** (String) password hashata 
- **token** (String) token hashato a breve scadenza
- **expiryDate** (LocalDateTime) 5 minuti dopo la creazione
- **revoked** (Boolean) 
- **registerSuccess** (Boolean) 

## Refresh token
- **id**
- **user** (user.id)
- **refreshToken** (String)
- **expiryDate** (LocalDateTime)
- **revoked** (boolean)

## Login history
- **id**
- **user** (user.id)
- **loginTime** (LocalDateTime)
- **loginProvider**
- **ipAddress** (String) indirizzo ip del client
- **userAgent** (String) identifica il browser, device e sistema operativo del client
- **success** (boolean)
- **failureReason** (String) in caso di success=false

## Reset password
- **id**
- **userId** (user.id)
- **token** (String)
- **expiryDate** (LocalDateTime)
- **used** (boolean)