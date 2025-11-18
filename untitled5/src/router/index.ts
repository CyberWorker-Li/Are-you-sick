import { createRouter, createWebHistory} from 'vue-router';
import type {RouteRecordRaw} from "vue-router";
import { useAuthStore } from '../stores/auth';
import Login from '../components/Login.vue';
import Register from '../components/Register.vue';
import ResetPassword from '../components/ResetPassword.vue';
import PatientDashboard from '../components/PatientDashboard.vue';

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
        path: '/patient',
        name: 'PatientDashboard',
        component: PatientDashboard,
        meta: { requiresAuth: true }
    }
];

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
});

// 添加路由守卫
router.beforeEach(async (to, from, next) => {
  const auth = useAuthStore();
  
  // 如果路由需要认证
  if (to.meta.requiresAuth) {
    // 检查是否已登录
    if (auth.isLoggedIn && auth.userInfo) {
      next();
    } else {
      // 未登录，重定向到登录页
      next('/login');
    }
  } else {
    next();
  }
});

export default router;