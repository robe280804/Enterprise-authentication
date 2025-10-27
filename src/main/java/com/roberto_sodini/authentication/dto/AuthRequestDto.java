package com.roberto_sodini.authentication.dto;

import com.roberto_sodini.authentication.security.audit.AuditUserField;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AuthRequestDto {

    @AuditUserField
    @NotBlank(message = "L'email non può essere vuota")
    @Email(message = "Formato email non valido")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Email non valida: deve contenere un dominio valido"
    )
    private String email;

    @NotNull(message = "La password non può essere vuota")
    @Size(min = 6, message = "La password deve essere lunga almeno 6 caratteri")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$",
            message = "La password deve contenere almeno una maiuscola, una minuscola, un numero e un carattere speciale")
    private String password;
}
