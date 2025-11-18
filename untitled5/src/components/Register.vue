<template>
  <div class="auth-container">
    <div class="auth-box">
      <img :src="logoUrl" alt="Logo" class="auth-logo" />
      <h3 class="form-title">用户注册</h3>
      <p class="form-subtitle">请填写信息完成注册</p>

      <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-position="top"
          @keyup.enter="handleRegister(registerFormRef)"
      >
        <el-form-item prop="email">
          <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              size="large"
              clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              size="large"
          />
        </el-form-item>

        <el-form-item prop="verificationCode">
          <el-input
              v-model="registerForm.verificationCode"
              placeholder="请输入验证码"
              size="large"
          >
            <template #append>
              <el-button
                  :loading="sending"
                  :disabled="sending || !canSend"
                  @click="handleSendCode"
                  size="small"
              >
                <span v-if="countdown === 0">获取验证码</span>
                <span v-else>{{ countdown }}s</span>
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              @click="handleRegister(registerFormRef)"
              style="width: 100%;"
              size="large"
              :loading="loading"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="form-links">
        <el-link type="primary" :underline="false" @click="router.push({ name: 'Login' })">已有账号？去登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import logo from '../assets/CSS 容器居中问题.png';
import { authApi } from '../api/auth';

const logoUrl = ref(logo);
const router = useRouter();

const registerFormRef = ref<FormInstance>();
const registerForm = reactive({
  email: '',
  password: '',
  verificationCode: '',
});
const loading = ref(false);

// 发送验证码状态
const sending = ref(false);
const countdown = ref(0);
let countdownTimer: number | undefined;

const canSend = computed(() => {
  // 简单校验：邮箱符合格式且不是倒计时时
  return !!registerForm.email && /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(registerForm.email) && countdown.value === 0;
});

const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,}$/;

const validateEmail = (_: any, value: string, cb: (err?: string | Error) => void) => {
  if (!value) return cb(new Error('请输入邮箱'));
  if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) return cb(new Error('请输入有效邮箱'));
  cb();
};
const validatePassword = (_: any, value: string, cb: (err?: string | Error) => void) => {
  if (!value) return cb(new Error('请输入密码'));
  if (!passwordPattern.test(value)) return cb(new Error('密码必须至少8位，包含大小写字母和数字'));
  cb();
};
const validateCode = (_: any, value: string, cb: (err?: string | Error) => void) => {
  if (!value) return cb(new Error('请输入验证码'));
  cb();
};

const registerRules = reactive<FormRules>({
  email: [{ validator: validateEmail, trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  verificationCode: [{ validator: validateCode, trigger: 'blur' }],
});

const startCountdown = (seconds = 60) => {
  countdown.value = seconds;
  countdownTimer && clearInterval(countdownTimer);
  countdownTimer = setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0) {
      clearInterval(countdownTimer);
      countdown.value = 0;
    }
  }, 1000) as unknown as number;
};

const handleSendCode = async () => {
  if (!canSend.value) {
    ElMessage.warning('请输入有效邮箱后再获取验证码');
    return;
  }
  try {
    sending.value = true;
    await authApi.sendCode({ email: registerForm.email, type: 'REGISTER' });
    ElMessage.success('验证码已发送，请查收邮箱');
    startCountdown(60);
  } catch (err: any) {
    // axios / 拦截器会有全局提示，这里再做一次友好提示
    ElMessage.error(err?.message || '发送验证码失败');
  } finally {
    sending.value = false;
  }
};

const handleRegister = (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  formEl.validate(async (valid) => {
    if (!valid) {
      ElMessage.error('表单校验未通过，请检查输入');
      return;
    }
    loading.value = true;
    try {
      await authApi.register({
        email: registerForm.email,
        password: registerForm.password,
        verificationCode: registerForm.verificationCode,
      });
      ElMessage.success('注册成功，请登录');
      router.push({ name: 'Login' });
    } catch (err: any) {
      ElMessage.error(err?.message || '注册失败');
    } finally {
      loading.value = false;
    }
  });
};
</script>


<style scoped>
/* 样式复用 */
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