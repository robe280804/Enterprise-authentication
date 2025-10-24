export interface AuthRequest {
    email: string;
    password: string;
}

export interface RegisterResponse {
    message: string,
    userId: string,
    email: string
}