import { createRouter, createWebHistory} from 'vue-router';
import type {RouteRecordRaw} from "vue-router";
import Login from '../components/Login.vue';
import Register from '../components/Register.vue';
import ResetPassword from '../components/ResetPassword.vue';

const routes: Array<RouteRecordRaw> = [
    {
        // 根路径重定向到登录页
        path: '/',
        redirect: '/login'
    },

            {
                path: '/login', // 对应 /auth/login
                name: 'Login',
                component: Login
            },
            {
                path: '/register', // 对应 /auth/register
                name: 'Register',
                component: Register
            },
            {
                path: '/reset', // 对应 /auth/reset
                name: 'ResetPassword',
                component: ResetPassword
            }


];

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
});

export default router;