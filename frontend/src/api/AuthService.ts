import type { AuthRequest, RegisterResponse } from "../types/AuthType";

export async function Register(data: AuthRequest): Promise<RegisterResponse> {
    const response = await fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        body: JSON.stringify(data)
    });

    const registerResult = await response.json();

    if (!response.ok){
        throw new Error(registerResult. || "Errore durante la registrazione")
    }

    return registerResult;
}