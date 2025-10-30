<template>
  <div class="auth-container">
    <div class="auth-box">
      <img :src="logoUrl" alt="Logo" class="auth-logo" />
    <h3 class="form-title">用户登录</h3>
    <p class="form-subtitle">请输入您的账号密码</p>

    <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        @keyup.enter="handleLogin(loginFormRef)"
    >
      <el-form-item prop="email">
        <el-input
            v-model="loginForm.email"
            placeholder="请输入邮箱"
            :prefix-icon="User"
            size="large"
        />
      </el-form-item>

      <el-form-item prop="password">
        <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            size="large"
        />
      </el-form-item>

      <el-form-item>
        <el-button
            type="primary"
            @click="handleLogin(loginFormRef)"
            style="width: 100%;"
            size="large"
        >
          登录
        </el-button>
      </el-form-item>
    </el-form>

    <div class="form-links">
      <el-link type="primary" :underline="false" @click="router.push({ name: 'ResetPassword' })">忘记密码?</el-link>
      <el-link type="primary" :underline="false" @click="router.push({ name: 'Register' })">还没有账号? 立即注册</el-link>
    </div>
  </div>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';
import logo from '../assets/CSS 容器居中问题.png';
import { useAuthStore } from '../stores/auth';

const logoUrl = ref(logo);
const router = useRouter();

const auth = useAuthStore();

const loginFormRef = ref<FormInstance>();
const loginForm = reactive({
  email: '',
  password: '',
});
const loading = ref(false);

// 验证器（和你之前一致）
const validateEmail = (_: any, value: string, callback: (error?: string | Error) => void) => {
  if (value === '') {
    callback(new Error('请输入邮箱'));
  } else if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
    callback(new Error('请输入有效的邮箱地址'));
  } else {
    callback();
  }
};

const validatePassword = (_: any, value: string, callback: (error?: string | Error) => void) => {
  if (value === '') {
    callback(new Error('请输入密码'));
  } else if (!/^[a-zA-Z0-9.@_]{8,}$/.test(value)) {
    callback(new Error('密码必须由字母、数字或.@_组成，且至少8位'));
  } else {
    callback();
  }
};

const loginRules = reactive<FormRules>({
  email: [{ validator: validateEmail, trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
});

// 仅负责调用 store.login（组件不处理 axios、token 存储细节）
const handleLogin = (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  formEl.validate(async (valid) => {
    if (!valid) {
      ElMessage.error('输入不合法，请检查');
      return;
    }

    loading.value = true;
    try {
      // 调用 pinia store 的 login 方法
      await auth.login({ email: loginForm.email, password: loginForm.password });
      ElMessage.success('登录成功');

      // 登录后跳转（优先 Home 或 Dashboard，或改成你的主页面）
      if (router.hasRoute('Home')) {
        router.push({ name: 'Home' });
      } else if (router.hasRoute('Dashboard')) {
        router.push({ name: 'Dashboard' });
      } else {
        router.push('/');
      }
    } catch (err: any) {
      console.error('登录失败', err);
      ElMessage.error(err?.message || '登录失败');
    } finally {
      loading.value = false;
    }
  });
};
</script>


<style scoped>
/* 样式与之前相同 */
.auth-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  /* 完美复刻截图中的浅蓝色渐变背景 */
  background: linear-gradient(135deg, #f0f8ff 0%, #e6f0ff 100%);
}

.auth-box {
  width: 400px;
  background: #ffffff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  box-sizing: border-box; /* 确保 padding 不会撑大宽度 */
}

.auth-logo {
  display: block;
  width: 110px; /* 根据截图比例设置 */
  margin: 0 auto 25px auto;
}
.form-title {
  text-align: center;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 10px 0;
}
.form-subtitle {
  text-align: center;
  font-size: 14px;
  color: #909399;
  margin: 0 0 30px 0;
}
.form-links {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 14px;
}
:deep(.el-input__inner) {
  height: 40px;
}
</style>