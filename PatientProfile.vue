<template>
  <div class="patient-profile">
    <div class="header">
      <el-button type="primary" @click="goBack" class="back-btn">
        <i class="el-icon-arrow-left"></i>
        返回挂号页面
      </el-button>
      <h1>个人信息管理</h1>
    </div>

    <el-card class="profile-card">
      <el-form :model="profileForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="profileForm.name" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="profileForm.phone" placeholder="请输入联系电话" maxlength="20" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-select v-model="profileForm.gender" placeholder="请选择性别">
            <el-option label="男" value="MALE" />
            <el-option label="女" value="FEMALE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-form-item label="出生日期" prop="birthDate">
          <el-date-picker
            v-model="profileForm.birthDate"
            type="date"
            placeholder="选择出生日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">
            保存信息
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useAuthStore } from '../stores/auth';
import patientApi from '../api/patient';

const router = useRouter();
const auth = useAuthStore();
const formRef = ref<FormInstance>();
const loading = ref(false);

const profileForm = ref({
  name: '',
  phone: '',
  gender: '' as 'MALE' | 'FEMALE' | 'OTHER' | '',
  birthDate: ''
});

const rules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }]
};

const goBack = () => {
  router.push('/patient');
};

const submitForm = async () => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
    loading.value = true;

    console.log('提交数据:', profileForm.value);

    // 检查 patientApi 是否存在
    if (!patientApi || !patientApi.updatePatientProfile) {
      console.error('patientApi 未定义:', patientApi);
      ElMessage.error('API配置错误，请检查导入');
      return;
    }

    // 调用API
    const response = await patientApi.updatePatientProfile(profileForm.value);
    console.log('API响应:', response);

    if (response.code === 200) {
      ElMessage.success('个人信息保存成功');
      // 更新本地存储
      if (auth.userInfo) {
        auth.userInfo.name = profileForm.value.name;
        auth.userInfo.phone = profileForm.value.phone;
      }
      // 延迟返回
      setTimeout(() => router.push('/patient'), 1000);
    } else {
      ElMessage.error(response.message || '保存失败');
    }
  } catch (error: any) {
    console.error('保存失败 - 完整错误:', error);
    console.error('错误类型:', error?.constructor?.name);
    console.error('错误消息:', error?.message);
    console.error('错误堆栈:', error?.stack);

    // 显示具体的错误信息
    if (error.message?.includes('patientApi is not defined')) {
      ElMessage.error('前端API配置错误，请检查控制台');
    } else if (error.response) {
      ElMessage.error(`服务器错误: ${error.response.status} - ${error.response.data?.message || '未知错误'}`);
    } else if (error.request) {
      ElMessage.error('网络错误，请检查连接');
    } else {
      ElMessage.error(`保存失败: ${error.message || '未知错误'}`);
    }
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

// 加载现有信息
onMounted(() => {
  // 如果有现有信息，可以预先加载
  if (auth.userInfo?.name) {
    profileForm.value.name = auth.userInfo.name;
  }
  if (auth.userInfo?.phone) {
    profileForm.value.phone = auth.userInfo.phone;
  }
});
</script>

<style scoped>
.patient-profile {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.profile-card {
  padding: 20px;
}
</style>