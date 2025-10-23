<template>
  <div class="form-wrapper">
    <h3 class="form-title">用户注册</h3>
    <p class="form-subtitle">创建您的新客户</p>

    <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
    >
      <el-form-item prop="email">
        <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            :prefix-icon="Message"
            size="large"
        />
      </el-form-item>

      <el-form-item prop="verificationCode">
        <el-row :gutter="10" style="width: 100%;">
          <el-col :span="15">
            <el-input
                v-model="registerForm.verificationCode"
                placeholder="请输入验证码"
                :prefix-icon="Select"
                size="large"
            />
          </el-col>
          <el-col :span="9">
            <el-button
                type="success"
                @click="handleGetCode"
                style="width: 100%;"
                size="large"
            >
              获取验证码
            </el-button>
          </el-col>
        </el-row>
      </el-form-item>

      <el-form-item prop="password">
        <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            size="large"
        />
      </el-form-item>

      <el-form-item prop="confirmPassword">
        <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            :prefix-icon="Lock"
            show-password
            size="large"
        />
      </el-form-item>

      <el-form-item>
        <el-button
            type="success"
            @click="handleRegister(registerFormRef)"
            style="width: 100%;"
            size="large"
        >
          注册
        </el-button>
      </el-form-item>
    </el-form>

    <div class="form-links" style="justify-content: flex-end;">
      <el-link type="primary" :underline="false" @click="$emit('switch-to-login')">已有账号? 立即登录</el-link>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { Message, Lock, Select } from '@element-plus/icons-vue';

const emit = defineEmits(['switch-to-login']);

const registerFormRef = ref(null);
const registerForm = reactive({
  email: '',
  verificationCode: '',
  password: '',
  confirmPassword: '',
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
    // 如果输入了确认密码，则触发确认密码的验证
    if (registerForm.confirmPassword !== '') {
      registerFormRef.value.validateField('confirmPassword');
    }
    callback();
  }
};

// 确认密码验证
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const registerRules = reactive({
  email: [{ validator: validateEmail, trigger: 'blur' }],
  verificationCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
});

// --- 事件处理 ---

// 模拟获取验证码
const handleGetCode = () => {
  // 实际应用中会先验证邮箱
  ElMessage.success('验证码已发送 (模拟)');
};

const handleRegister = (formEl) => {
  if (!formEl) return;
  formEl.validate((valid) => {
    if (valid) {
      console.log('Register form valid:', registerForm);
      ElMessage.success('注册成功 (模拟)');
      // 模拟注册成功后跳转到登录
      setTimeout(() => {
        emit('switch-to-login');
      }, 1000);
    } else {
      ElMessage.error('输入不合法，请检查');
      return false;
    }
  });
};
</script>

<style scoped>
/* 样式复用自 Login.vue */
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