<template>
  <div class="patient-dashboard">
    <div class="header">
      <h1>患者挂号系统</h1>
      <div class="user-info">
        <span>欢迎，{{ userEmail }}</span>
        <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </div>

    <div class="main-content">
      <!-- 挂号功能 -->
      <div class="registration-section">
        <h2>挂号服务</h2>
        <el-card class="registration-card">
          <el-form :model="registrationForm" label-width="100px">
            <el-form-item label="选择科室">
              <el-select v-model="registrationForm.department" placeholder="请选择科室">
                <el-option
                  v-for="dept in departments"
                  :key="dept.value"
                  :label="dept.label"
                  :value="dept.value"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="选择医生">
              <el-select v-model="registrationForm.doctorId" placeholder="请选择医生" :disabled="!registrationForm.department">
                <el-option
                  v-for="doc in availableDoctors"
                  :key="doc.id"
                  :label="`${doc.name} - ${doc.title || '医生'}`"
                  :value="doc.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="备注信息">
              <el-input
                v-model="registrationForm.notes"
                type="textarea"
                :rows="2"
                placeholder="请输入病情描述或特殊要求"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="就诊时间">
              <el-date-picker
                v-model="registrationForm.appointmentTime"
                type="datetime"
                placeholder="选择就诊时间"
                :disabled-date="disabledDate"
                :shortcuts="timeShortcuts"
              />
            </el-form-item>

            <el-form-item>
              <el-button 
                type="primary" 
                @click="handleRegistration"
                :disabled="!canRegister"
              >
                确认挂号
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <!-- 我的挂号记录 -->
      <div class="appointments-section">
        <h2>我的挂号记录</h2>
        <el-table :data="appointments" style="width: 100%" empty-text="暂无挂号记录" v-loading="loading">
          <el-table-column prop="id" label="挂号编号" width="100" />
          <el-table-column prop="doctorDepartment" label="科室" width="120" />
          <el-table-column label="医生" width="150">
            <template #default="scope">
              {{ scope.row.doctorName }} {{ scope.row.doctorTitle ? `(${scope.row.doctorTitle})` : '' }}
            </template>
          </el-table-column>
          <el-table-column label="就诊时间" width="180">
            <template #default="scope">
              {{ new Date(scope.row.appointmentTime).toLocaleString() }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="备注" width="200">
            <template #default="scope">
              <el-tooltip :content="scope.row.notes" placement="top" v-if="scope.row.notes">
                <span class="notes-text">{{ scope.row.notes.length > 20 ? scope.row.notes.substring(0, 20) + '...' : scope.row.notes }}</span>
              </el-tooltip>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button
                v-if="scope.row.status === 'PENDING' || scope.row.status === 'CONFIRMED'"
                type="danger"
                size="small"
                @click="handleCancel(scope.row)"
                :loading="loading"
              >
                退号
              </el-button>
              <span v-else>不可操作</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useAuthStore } from '../stores/auth';
import appointmentApi from '../api/appointment';
import doctorApi from '../api/doctor';
import type { AppointmentDTO, AppointmentRequest, DoctorDTO } from '../types/dto';

interface RegistrationForm {
  department: string;
  doctorId: number | null;
  appointmentTime: Date | null;
  notes?: string;
}

const router = useRouter();
const auth = useAuthStore();

const userEmail = ref(auth.userInfo?.name || '用户');
const patientId = ref(auth.userInfo?.id || 0);

// 真实数据
const departments = ref([
  { value: '内科', label: '内科' },
  { value: '外科', label: '外科' },
  { value: '儿科', label: '儿科' },
  { value: '妇产科', label: '妇产科' },
  { value: '眼科', label: '眼科' }
]);

const doctors = ref<DoctorDTO[]>([]);
const availableDoctors = ref<DoctorDTO[]>([]);

const registrationForm = ref<RegistrationForm>({
  department: '',
  doctorId: null,
  appointmentTime: null,
  notes: ''
});

const appointments = ref<AppointmentDTO[]>([]);
const loading = ref(false);

// 监听科室变化，加载对应科室的医生
watch(() => registrationForm.value.department, async (newDepartment) => {
  if (newDepartment) {
    try {
      console.log('正在加载科室医生:', newDepartment);
      const response = await doctorApi.getDoctorsByDepartment(newDepartment);
      console.log('医生API响应:', response);
      if (response.code === 200 && response.data) {
        availableDoctors.value = response.data;
        console.log('成功加载医生:', response.data);
      } else {
        console.error('医生API返回错误:', response);
        ElMessage.error('加载医生列表失败: ' + (response.message || '未知错误'));
      }
    } catch (error) {
      console.error('加载医生列表异常:', error);
      const errorMessage = error instanceof Error ? error.message : '网络错误';
      ElMessage.error('加载医生列表失败: ' + errorMessage);
    }
  } else {
    availableDoctors.value = [];
    registrationForm.value.doctorId = null;
  }
});

// 计算属性
const canRegister = computed(() => {
  return registrationForm.value.department && 
         registrationForm.value.doctorId && 
         registrationForm.value.appointmentTime;
});

// 时间选择器配置
const timeShortcuts = [
  {
    text: '今天',
    value: new Date()
  },
  {
    text: '明天',
    value: new Date(Date.now() + 3600 * 1000 * 24)
  },
  {
    text: '一周后',
    value: new Date(Date.now() + 3600 * 1000 * 24 * 7)
  }
];

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 3600 * 1000 * 24;
};

const getStatusType = (status: string) => {
  const statusMap: { [key: string]: string } = {
    'PENDING': 'warning',
    'CONFIRMED': 'success',
    'COMPLETED': 'info',
    'CANCELLED': 'danger',
    'EXPIRED': 'info'
  };
  return statusMap[status] || 'info';
};

const getStatusText = (status: string) => {
  const statusMap: { [key: string]: string } = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'EXPIRED': '已过期'
  };
  return statusMap[status] || status;
};

// 操作方法
const handleRegistration = async () => {
  if (!canRegister.value) return;

  try {
    loading.value = true;
    
    const request: AppointmentRequest = {
      patientId: patientId.value,
      doctorId: registrationForm.value.doctorId!,
      appointmentTime: registrationForm.value.appointmentTime!.toISOString(),
      notes: registrationForm.value.notes
    };

    const response = await appointmentApi.createAppointment(request);
    
    if (response.code === 200) {
      ElMessage.success('挂号成功！');
      
      // 重新加载预约列表
      await loadAppointments();
      
      // 重置表单
      registrationForm.value = {
        department: '',
        doctorId: null,
        appointmentTime: null,
        notes: ''
      };
    } else {
      ElMessage.error(response.message || '挂号失败');
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '挂号失败');
  } finally {
    loading.value = false;
  }
};

const handleCancel = async (appointment: AppointmentDTO) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消 ${appointment.doctorDepartment} - ${appointment.doctorName} 的挂号吗？`,
      '确认退号',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    const response = await appointmentApi.cancelAppointment(appointment.id, '患者主动取消');
    
    if (response.code === 200) {
      ElMessage.success('退号成功');
      // 重新加载预约列表
      await loadAppointments();
    } else {
      ElMessage.error(response.message || '退号失败');
    }
  } catch {
    // 用户取消操作
  }
};

const loadAppointments = async () => {
  try {
    console.log('正在加载患者预约，患者ID:', patientId.value);
    const response = await appointmentApi.getPatientAppointments(patientId.value);
    console.log('预约API响应:', response);
    if (response.code === 200) {
      appointments.value = response.data;
      console.log('成功加载预约:', response.data);
    } else {
      console.error('预约API返回错误:', response);
      ElMessage.error('加载预约记录失败: ' + (response.message || '未知错误'));
    }
  } catch (error) {
    console.error('加载预约记录异常:', error);
    const errorMessage = error instanceof Error ? error.message : '网络错误';
    ElMessage.error('加载预约记录失败: ' + errorMessage);
  }
};

const loadAllDoctors = async () => {
  try {
    console.log('正在加载所有医生');
    const response = await doctorApi.getAllDoctors();
    console.log('所有医生API响应:', response);
    if (response.code === 200) {
      doctors.value = response.data;
      console.log('成功加载所有医生:', response.data);
    } else {
      console.error('所有医生API返回错误:', response);
      ElMessage.error('加载医生列表失败: ' + (response.message || '未知错误'));
    }
  } catch (error) {
    console.error('加载所有医生异常:', error);
    const errorMessage = error instanceof Error ? error.message : '网络错误';
    ElMessage.error('加载医生列表失败: ' + errorMessage);
  }
};

const handleLogout = () => {
  auth.logout();
  router.push('/login');
  ElMessage.success('已退出登录');
};

// 初始化加载数据
onMounted(async () => {
  await Promise.all([loadAppointments(), loadAllDoctors()]);
});
</script>

<style scoped>
.patient-dashboard {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.header h1 {
  color: #303133;
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 14px;
  color: #606266;
}

.main-content {
  display: grid;
  gap: 30px;
}

.registration-section,
.appointments-section {
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.registration-section h2,
.appointments-section h2 {
  color: #303133;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 2px solid #409eff;
  padding-bottom: 8px;
}

.registration-card {
  max-width: 600px;
  border: none;
  box-shadow: none;
}

.notes-text {
  cursor: pointer;
  color: #606266;
  font-size: 12px;
}

.notes-text:hover {
  color: #409eff;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-table) {
  margin-top: 10px;
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table__header) {
  background-color: #f5f7fa;
}

:deep(.el-table th) {
  background-color: #f5f7fa !important;
  color: #606266;
  font-weight: 600;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  border: none;
  border-radius: 6px;
  font-weight: 500;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #66b1ff, #409eff);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #f56c6c, #f78989);
  border: none;
  border-radius: 6px;
  font-weight: 500;
}

:deep(.el-button--danger:hover) {
  background: linear-gradient(135deg, #f78989, #f56c6c);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}

:deep(.el-select),
:deep(.el-date-editor),
:deep(.el-input) {
  width: 100%;
}

:deep(.el-tag) {
  border-radius: 12px;
  font-weight: 500;
}

.loading-overlay {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

@media (max-width: 768px) {
  .patient-dashboard {
    padding: 10px;
  }
  
  .header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .registration-card {
    max-width: 100%;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
}
</style>