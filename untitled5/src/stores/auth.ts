// src/stores/auth.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { authApi } from '../api/auth';
import { userApi } from '../api/user';
import type { LoginRequestPayload, LoginResponse, UserInfo } from '../types/dto';
import http from '../api/axios';

const TOKEN_KEY = 'token';

export const useAuthStore = defineStore('auth', () => {
    const token = ref<string | null>(localStorage.getItem(TOKEN_KEY));
    const userInfo = ref<UserInfo | null>(null);
    const role = ref<string | null>(localStorage.getItem('role'));

    const isLoggedIn = computed(() => !!token.value);

    function setToken(t?: string | null) {
        token.value = t ?? null;
        if (t) {
            localStorage.setItem(TOKEN_KEY, t);
            http.defaults.headers.common['Authorization'] = `Bearer ${t}`;
        } else {
            localStorage.removeItem(TOKEN_KEY);
            delete http.defaults.headers.common['Authorization'];
        }
    }

    async function login(payload: LoginRequestPayload) {
        const resp: any = await authApi.login(payload);
        // 处理 ApiResponse 包装
        const loginData = resp && resp.code === 200 ? resp.data : resp;
        
        if (!loginData || !loginData.token) {
            throw new Error('登录返回无 token');
        }
        setToken(loginData.token);
        userInfo.value = loginData.userInfo ?? null;
        role.value = loginData.role || null;
        if (role.value) {
            localStorage.setItem('role', role.value);
        }
        return loginData;
    }

    function logout() {
        setToken(null);
        userInfo.value = null;
        role.value = null;
        localStorage.removeItem('role');
    }

    /**
     * init()：
     * 如果本地有 token，则尝试调用 /auth/me 恢复 userInfo。
     * 在应用入口（main.ts）中可调用 auth.init()。
     */
    async function init() {
        const t = localStorage.getItem(TOKEN_KEY);
        if (!t) {
            return;
        }
        // 先设置 token，使请求拦截器会带上 Authorization
        setToken(t);
        try {
            const ui = await userApi.getCurrentUser();
            userInfo.value = ui ?? null;
        } catch (err) {
            // 如果取 user 失败（token 失效等），清理 token（可选策略）
            console.warn('恢复用户信息失败：', err);
            // 选择一个策略：登出或仅清除 userInfo
            // 这里我选择登出以避免无效 token 留在本地
            logout();
        }
    }

    return {
        token,
        userInfo,
        role,
        isLoggedIn,
        setToken,
        login,
        logout,
        init,
    };
});
