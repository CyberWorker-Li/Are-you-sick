<template>
  <div class="auth-container">
    <div class="auth-box">
      <img :src="logoUrl" alt="Logo" class="auth-logo" />
      <h3 class="form-title">重置密码</h3>
      <p class="form-subtitle">请输入邮箱与验证码，设置新密码</p>

      <el-form
          ref="resetFormRef"
          :model="resetForm"
          :rules="resetRules"
          label-position="top"
          @keyup.enter="handleReset(resetFormRef)"
      >
        <el-form-item prop="email">
          <el-input v-model="resetForm.email" placeholder="请输入邮箱" size="large" />
        </el-form-item>

        <el-form-item prop="verificationCode">
          <el-input v-model="resetForm.verificationCode" placeholder="请输入验证码" size="large">
            <template #append>
              <el-button
                  :loading="sending"
                  :disabled="sending || countdown > 0 || !canSend"
                  @click="handleSendCode"
                  size="small"
              >
                <span v-if="countdown === 0">获取验证码</span>
                <span v-else>{{ countdown }}s</span>
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="newPassword">
          <el-input v-model="resetForm.newPassword" type="password" placeholder="请输入新密码" show-password size="large" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleReset(resetFormRef)" style="width:100%" :loading="loading">重置密码</el-button>
        </el-form-item>
      </el-form>

      <div class="form-links">
        <el-link type="primary" :underline="false" @click="router.push({ name: 'Login' })">返回登录</el-link>
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

const resetFormRef = ref<FormInstance>();
const resetForm = reactive({
  email: '',
  verificationCode: '',
  newPassword: '',
});
const loading = ref(false);
const sending = ref(false);
const countdown = ref(0);
let timer: number | undefined;

const canSend = computed(() => !!resetForm.email && /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(resetForm.email) && countdown.value === 0);

const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,}$/;

const validateEmail = (_: any, value: string, cb: (err?: string | Error) => void) => {
  if (!value) return cb(new Error('请输入邮箱'));
  if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) return cb(new Error('请输入有效邮箱'));
  cb();
};
const validateCode = (_: any, value: string, cb: (err?: string | Error) => void) => {
  if (!value) return cb(new Error('请输入验证码'));
  cb();
};
const validateNewPassword = (_: any, value: string, cb: (err?: string | Error) => void) => {
  if (!value) return cb(new Error('请输入新密码'));
  if (!passwordPattern.test(value)) return cb(new Error('密码必须至少8位，包含大小写字母和数字'));
  cb();
};

const resetRules = reactive<FormRules>({
  email: [{ validator: validateEmail, trigger: 'blur' }],
  verificationCode: [{ validator: validateCode, trigger: 'blur' }],
  newPassword: [{ validator: validateNewPassword, trigger: 'blur' }],
});

const startCountdown = (seconds = 60) => {
  countdown.value = seconds;
  timer && clearInterval(timer);
  timer = setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0) {
      clearInterval(timer);
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
    await authApi.sendCode({ email: resetForm.email, type: 'RESET_PASSWORD' });
    ElMessage.success('验证码已发送');
    startCountdown(60);
  } catch (err: any) {
    ElMessage.error(err?.message || '发送验证码失败');
  } finally {
    sending.value = false;
  }
};

const handleReset = (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  formEl.validate(async (valid) => {
    if (!valid) {
      ElMessage.error('表单校验未通过，请检查输入');
      return;
    }
    loading.value = true;
    try {
      await authApi.resetPassword({
        email: resetForm.email,
        verificationCode: resetForm.verificationCode,
        newPassword: resetForm.newPassword,
      });
      ElMessage.success('密码已重置，请登录');
      router.push({ name: 'Login' });
    } catch (err: any) {
      ElMessage.error(err?.message || '重置失败');
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