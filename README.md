## Autenticazione enterprise

## Flusso Registrazione utente
- **1** Il client invia email e password validati dal DTO.
- **2** Controllo che l'email non sia già registrata nel db, nel caso lancio un eccezione.
- **3** Creo un model Email Verification Token che contiene un token a breve scadenza e i dati dell'utente per la registrazione.
- **4** Invio l'email all'utente dove è presente un link + token che lo reindirizzerà al server.
- **5** Il server verifica che il token sia esistente e non sia scaduto, se scaduto la registrazione non viene eseguita.
- **6** Creo il nuovo utente e lo salvo nel database, ottenendo i suoi dati dal modello salvato nel db.
- Tutti gli eventi importanti (creazione token, invio email, conferma registrazione, errori) sono loggati in modo strutturato.

## Autenticazione utente
- Dopo la registrazione dell'utente, viene inviata un email al client insieme a un token a breve scadenza (5 minuti)
- Il client una volta confermata la registrazione verrà inserito nel sistema
- Se l'autenticazione dell' utente va a buon fine, genero un access token e un refresh token
- Attraverso kafka invio: - un event per salvare nel db il login dell'utente (login history)
                          - un event per salvare nel db il refresh token
- Utilizzo kafka perchè non sono eventi critici che motivano il blocco del login dell'utente
- Salvo il refresh token in un cookie
- Invio all'utente l'access token che verrà inviato a ogni richiesta



## Endpoint

- api/auth/register | (POST) | (ROLE: ANY) | Metodo per la registrazione

## Modelli database

# User
- **id**
- **email** (String)
- **password** (String)
- **roles** (Set)
- **authProvider **
- **createdAt** (LocalDateTime)
- **updatedAt** (LocalDateTime)

# Email verification token
- *id**
- **userEmail** (String) 
- **userPassword** (String) password hashata 
- **token** (String) a breve scadenza
- **expiryDate** (LocalDateTime) 5 minuti dopo la creazione
- **revoked** (Boolean) 
- **registerSuccess** (Boolean) 

# Refresh token
- **id**
- **userId** (user.id)
- **refreshToken** (String)
- **expiryDate** (LocalDateTime)
- **revoked** (boolean)

# Login history
- **id**
- **userId** (user.id)
- **loginTime** (LocalDateTime)
- **loginProvider**
- **ipAddress** (String) indirizzo ip del client
- **userAgent** (String) identifica il browser, device e sistema operativo del client
- **success** (boolean)
- **failureReason** (String) in caso di success=false

# Reset password
- **id**
- **userId** (user.id)
- **token** (String)
- **expiryDate** (LocalDateTime)
- **used** (boolean)