import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import Login from '../components/Login.vue';
import Register from '../components/Register.vue';
import ResetPassword from '../components/ResetPassword.vue';
import RegistrationForm from '../components/RegistrationForm.vue';
import RegistrationList from '../components/RegistrationList.vue';
import Dashboard from '../components/Dashboard.vue';
import ApiTest from '../components/ApiTest.vue';
import { useAuthStore } from '../stores/auth';

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
    },
    {
        path: '/api-test',
        name: 'ApiTest',
        component: ApiTest,
        meta: { requiresAuth: false }
    },
    {
        path: '/dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { requiresAuth: true }
    },
    {
        path: '/registration',
        name: 'RegistrationForm',
        component: RegistrationForm,
        meta: { requiresAuth: true }
    },
    {
        path: '/registrations',
        name: 'RegistrationList',
        component: RegistrationList,
        meta: { requiresAuth: true }
    }
];

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
});

// TOKEN_KEY常量，与stores/auth.ts保持一致
const TOKEN_KEY = 'token';

// 路由守卫
router.beforeEach((to, from, next) => {
    // 直接从localStorage获取token，避免在路由守卫中使用Pinia store
    const token = localStorage.getItem(TOKEN_KEY);
    const isLoggedIn = !!token;
    
    if (to.meta.requiresAuth && !isLoggedIn) {
        // 需要认证但未登录，跳转到登录页
        next({ name: 'Login' });
    } else if (!to.meta.requiresAuth && isLoggedIn && (to.name === 'Login' || to.name === 'Register' || to.name === 'ResetPassword')) {
        // 已登录用户访问登录页面，跳转到仪表板
        next({ name: 'Dashboard' });
    } else {
        next();
    }
});

export default router;