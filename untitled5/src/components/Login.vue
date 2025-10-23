<template>
  <div class="form-wrapper">
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
      <el-link type="primary" :underline="false" @click="$emit('switch-to-reset')">忘记密码?</el-link>
      <el-link type="primary" :underline="false" @click="$emit('switch-to-register')">还没有账号? 立即注册</el-link>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';

// 定义 emit 事件，用于通知父组件切换视图
const emit = defineEmits(['switch-to-register', 'switch-to-reset']);

const loginFormRef = ref(null);
const loginForm = reactive({
  email: '',
  password: '',
});

// --- 验证规则 ---

// 邮箱验证
const validateEmail = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入邮箱'));
  } else if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
    callback(new Error('请输入有效的邮箱地址'));
  } else {
    callback();
  }
};

// 密码验证 (字母, 数字, ., @, _, 最少8位)
const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'));
  } else if (!/^[a-zA-Z0-9.@_]{8,}$/.test(value)) {
    callback(new Error('密码必须由字母、数字或.@_组成，且至少8位'));
  } else {
    callback();
  }
};

const loginRules = reactive({
  email: [{ validator: validateEmail, trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
});

// --- 事件处理 ---

const handleLogin = (formEl) => {
  if (!formEl) return;
  formEl.validate((valid) => {
    if (valid) {
      // 验证通过
      console.log('Login form valid:', loginForm);
      // 提示：由于没有后端，我们在这里显示一个成功的提示
      ElMessage.success('登录成功 (模拟)');
      // 此处应调用登录接口
    } else {
      // 验证失败
      ElMessage.error('输入不合法，请检查');
      return false;
    }
  });
};
</script>

<style scoped>
/* 通用表单样式 */
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
/* Element Plus 样式微调 */
:deep(.el-input__inner) {
  height: 40px; /* 统一样式 */
}
</style>