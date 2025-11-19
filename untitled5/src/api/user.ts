// src/api/user.ts
import http from './axios';
import type { UserInfo } from '../types/dto';

/**
 * 获取当前用户信息（需要后端提供 /auth/me）
 * 返回 UserInfo（或在后端返回 null 时抛错）
 */
export const userApi = {
    getCurrentUser: async (): Promise<UserInfo> => {
        // 假设后端 GET /auth/me 返回 ApiResponse<UserInfo>
        return http.get<UserInfo>('/auth/me');
    },
};
