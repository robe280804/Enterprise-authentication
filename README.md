## Autenticazione enterprise

## Modelli database

# User
- id
- email (String)
- password (String)
- roles (Set)
- authProvider 
- createdAt (LocalDateTime)
- updatedAt (LocalDateTime)

# Refresh token
- id
- userId (user.id)
- refreshToken (String)
- expiryDate (LocalDateTime)
- revoked (boolean)

# Login history
- id 
- userId (user.id)
- loginTime (LocalDateTime)
- loginProvider
- ipAddress (String) indirizzo ip del client
- userAgent (String) identifica il browser, device e sistema operativo del client
- success (boolean)
- failureReason (String) in caso di success=false

# Reset password
- id
- userId (user.id)
- token (String)
- expiryDate (LocalDateTime)
- used (boolean)