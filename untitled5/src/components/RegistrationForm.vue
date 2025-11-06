<template>
  <div class="registration-form">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>医院挂号</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="选择科室" prop="departmentId">
          <el-select
            v-model="form.departmentId"
            placeholder="请选择科室"
            style="width: 100%"
            @change="handleDepartmentChange"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="选择医生" prop="doctorId">
          <el-select
            v-model="form.doctorId"
            placeholder="请先选择科室"
            style="width: 100%"
            :disabled="!form.departmentId"
          >
            <el-option
              v-for="doctor in doctors"
              :key="doctor.id"
              :label="`${doctor.name} ${doctor.title || ''}`"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="患者姓名" prop="patientName">
          <el-input v-model="form.patientName" placeholder="请输入患者姓名" />
        </el-form-item>

        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>

        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" />
        </el-form-item>

        <el-form-item label="预约时间" prop="registrationTime">
          <el-date-picker
            v-model="form.registrationTime"
            type="datetime"
            placeholder="选择预约时间"
            style="width: 100%"
            :disabled-date="disabledDate"
            :disabled-hours="disabledHours"
            :disabled-minutes="disabledMinutes"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            提交挂号
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { departmentApi } from '../api/department';
import { doctorApi } from '../api/doctor';
import { registrationApi } from '../api/registration';
import type { Department, Doctor, RegistrationRequest } from '../types/dto';

const formRef = ref<FormInstance>();
const loading = ref(false);
const departments = ref<Department[]>([]);
const doctors = ref<Doctor[]>([]);

const form = reactive<RegistrationRequest>({
  departmentId: 0,
  doctorId: 0,
  patientName: '',
  phone: '',
  idCard: '',
  registrationTime: ''
});

const rules: FormRules = {
  departmentId: [
    { required: true, message: '请选择科室', trigger: 'change' }
  ],
  doctorId: [
    { required: true, message: '请选择医生', trigger: 'change' }
  ],
  patientName: [
    { required: true, message: '请输入患者姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  registrationTime: [
    { required: true, message: '请选择预约时间', trigger: 'change' }
  ]
};

// 禁用过去的日期
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7; // 禁用昨天之前的日期
};

// 禁用过去的小时
const disabledHours = () => {
  const hours = [];
  const now = new Date();
  const selectedDate = new Date(form.registrationTime);
  
  if (selectedDate.toDateString() === now.toDateString()) {
    for (let i = 0; i < now.getHours(); i++) {
      hours.push(i);
    }
  }
  
  // 医院工作时间：8:00-17:00
  for (let i = 0; i < 8; i++) {
    hours.push(i);
  }
  for (let i = 18; i < 24; i++) {
    hours.push(i);
  }
  
  return hours;
};

// 禁用过去的分钟
const disabledMinutes = (hour: number) => {
  const minutes = [];
  const now = new Date();
  const selectedDate = new Date(form.registrationTime);
  
  if (selectedDate.toDateString() === now.toDateString() && hour === now.getHours()) {
    for (let i = 0; i <= now.getMinutes(); i++) {
      minutes.push(i);
    }
  }
  
  return minutes;
};

// 加载科室列表
const loadDepartments = async () => {
  try {
    departments.value = await departmentApi.getList();
  } catch (error) {
    ElMessage.error('加载科室列表失败');
  }
};

// 科室变化时加载对应医生
const handleDepartmentChange = async (departmentId: number) => {
  form.doctorId = 0;
  doctors.value = [];
  
  if (departmentId) {
    try {
      doctors.value = await doctorApi.getByDepartment(departmentId);
    } catch (error) {
      ElMessage.error('加载医生列表失败');
    }
  }
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    await ElMessageBox.confirm(
      '确认提交挂号申请吗？',
      '确认挂号',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }
    );
    
    loading.value = true;
    await registrationApi.create(form);
    
    ElMessage.success('挂号成功！');
    resetForm();
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '挂号失败');
    }
  } finally {
    loading.value = false;
  }
};

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }
  doctors.value = [];
};

onMounted(() => {
  loadDepartments();
});
</script>

<style scoped>
.registration-form {
  max-width: 600px;
  margin: 20px auto;
  padding: 20px;
}

.form-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}
</style>