// src/api/auth.ts
import http from './axios';
import type {
    LoginRequestPayload,
    LoginResponse,
    RegisterRequestPayload,
    ResetPasswordRequestPayload,
    SendCodeRequestPayload,
} from '../types/dto';

// 注意：因为 axios 拦截器已经 unwrap ApiResponse，http.post 返回的就是 data（即 LoginResponse）
export const authApi = {
    login: async (payload: LoginRequestPayload): Promise<LoginResponse> => {
        // POST /auth/login
        return http.post<LoginResponse>('/auth/login', payload);
    },

    register: async (payload: RegisterRequestPayload): Promise<void> => {
        await http.post<void>('/auth/register', payload);
    },

    resetPassword: async (payload: ResetPasswordRequestPayload): Promise<void> => {
        await http.post<void>('/auth/reset-password', payload);
    },

    sendCode: async (payload: SendCodeRequestPayload): Promise<void> => {
        await http.post<void>('/auth/send-code', payload);
    },
};
