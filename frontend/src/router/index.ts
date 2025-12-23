import { createRouter, createWebHistory} from 'vue-router';
import type {RouteRecordRaw} from "vue-router";
import { useAuthStore } from '../stores/auth';
import Login from '../components/Login.vue';
import Register from '../components/Register.vue';
import ResetPassword from '../components/ResetPassword.vue';
import PatientDashboard from '../components/PatientDashboard.vue';
import AdminDashboard from '../components/AdminDashboard1.vue';
import DoctorDashboard from '../components/DoctorDashboard.vue';

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
        meta: { requiresAuth: true, role: 'PATIENT' }
    },
    {
        path: '/admin',
        name: 'AdminDashboard',
        component: AdminDashboard,
        meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
        path: '/doctor',
        name: 'DoctorDashboard',
        component: DoctorDashboard,
        meta: { requiresAuth: true, role: 'DOCTOR' }
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
      // 检查角色权限
      const requiredRole = to.meta.role;
      const userRole = auth.userInfo?.role || auth.role;
      
      if (requiredRole && userRole !== requiredRole) {
        // 角色不匹配，重定向到对应角色的页面
        if (userRole === 'ADMIN') {
          next('/admin');
        } else if (userRole === 'DOCTOR') {
          next('/doctor');
        } else if (userRole === 'PATIENT') {
          next('/patient');
        } else {
          next('/login');
        }
      } else {
        next();
      }
    } else {
      // 未登录，重定向到登录页
      next('/login');
    }
  } else {
    next();
  }
});

export default router;