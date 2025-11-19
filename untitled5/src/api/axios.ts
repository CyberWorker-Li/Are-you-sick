// src/api/axios.ts
import axios from 'axios';
import type { AxiosInstance, AxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';

const baseURL = import.meta.env.VITE_API_BASE || '/api';;
console.log('Axios baseURL =', baseURL);

const rawHttp = axios.create({
    baseURL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
});

// 请求拦截：仅使用 localStorage（避免导入 store 导致循环依赖）
rawHttp.interceptors.request.use(
    (config) => {
        try {
            const token = localStorage.getItem('token');
            if (token && config.headers) {
                config.headers['Authorization'] = `Bearer ${token}`;
            }
        } catch (e) {
            // ignore
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// 响应拦截：把后端 ApiResponse 解包并用 ElMessage 显示错误信息
rawHttp.interceptors.response.use(
    (resp) => {
        const body = resp.data;
        if (body && typeof body.code === 'number' && 'message' in body) {
            if (body.code === 200) {
                return body;  // 返回完整响应对象，让组件处理code和data
            } else {
                const msg = body.message || '请求失败';
                ElMessage.error(msg);
                const err = new Error(msg);
                // @ts-ignore
                err.code = body.code;
                return Promise.reject(err);
            }
        }
        return resp.data;
    },
    (error) => {
        const status = error?.response?.status;
        const serverMsg = error?.response?.data?.message || error?.message || '网络或服务器错误';
        if (status === 401) {
            // 清理 localStorage token（store.logout 也会做，但不能在这里导入 store）
            try { localStorage.removeItem('token'); } catch (e) {}
            ElMessage.error('未授权或登录过期，请重新登录');
        } else {
            ElMessage.error(serverMsg);
        }
        return Promise.reject(error);
    }
);

/**
 * Type wrapper 使得 http.post<T> 返回 Promise<T>
 */
type HttpClient = {
    get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>;
    post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>;
    put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>;
    delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>;
} & { defaults: AxiosInstance['defaults'] };

const http = rawHttp as unknown as HttpClient;

export default http;
