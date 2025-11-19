<template>
  <div class="auth-container">
    <img :src="logoUrl" alt="Logo" class="auth-logo" />

    <div class="auth-box">

     <router-view/>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import Login from './Login.vue';
import Register from './Register.vue';
import ResetPassword from './ResetPassword.vue';
import logo from '../assets/CSS 容器居中问题.png';

// 控制当前显示的组件，默认为 'login'
const currentView = ref('login');

// Logo (从您的截图中提取并转为Base64，确保100%一致)
const logoUrl = ref(logo);

</script>

<style scoped>
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
</style>