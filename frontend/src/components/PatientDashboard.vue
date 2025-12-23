<template>
  <div class="patient-dashboard">
    <div class="header">
      <h1>患者挂号系统</h1>
      <div class="user-info">
        <span>{{ userGreeting }}</span>
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
                  :key="dept.id"
                  :label="dept.name"
                  :value="dept.id"
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

            <el-form-item label="患者姓名" required>
              <el-input
                v-model="registrationForm.patientName"
                placeholder="请输入患者姓名"
              />
            </el-form-item>

            <el-form-item label="联系电话" required>
              <el-input
                v-model="registrationForm.patientPhone"
                placeholder="请输入联系电话"
                maxlength="20"
              />
            </el-form-item>

            <el-form-item label="就诊日期">
              <el-date-picker
                v-model="selectedDate"
                type="date"
                placeholder="选择就诊日期"
                :disabled-date="disabledDate"
                @change="loadAvailableTimeSlots"
              />
            </el-form-item>

            <el-form-item label="就诊时间" v-if="availableTimeSlots.length > 0">
              <el-select v-model="registrationForm.appointmentTime" placeholder="请选择时间段">
                <el-option
                  v-for="slot in availableTimeSlots"
                  :key="slot.startTime"
                  :label="formatTimeSlot(slot)"
                  :value="slot.startTime"
                  :disabled="!slot.available"
                  :class="['time-slot-option', { 'time-slot-working': slot.isWorkingTime }]"
                >
                  <span :style="{ color: slot.available ? '#409eff' : '#c0c4cc' }">
                    {{ formatTimeSlot(slot) }}
                    <span v-if="!slot.available" style="color: #f56c6c;"> (已满)</span>
                    <span v-else style="color: #67c23a;"> (剩余{{ slot.maxPatients - slot.currentPatients }}人)</span>
                  </span>
                </el-option>
              </el-select>
              <div v-if="availableTimeSlots.length === 0 && selectedDate && registrationForm.doctorId" style="color: #909399; margin-top: 8px;">
                该日期暂无可用时间段
              </div>
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
import departmentApi from '../api/department';
import scheduleApi from '../api/schedule';
import type { AppointmentDTO, AppointmentRequest, DepartmentDTO, DoctorDTO } from '../types/dto';
import type { TimeSlotDTO } from '../api/schedule';

interface RegistrationForm {
  department: number | null;
  doctorId: number | null;
  appointmentTime: string | null;
  notes?: string;
  patientName: string;
  patientPhone: string;
}

const router = useRouter();
const auth = useAuthStore();
const userGreeting = computed(() => auth.greetingText);
const patientId = ref(auth.userInfo?.id || 0);

// 真实数据
const departments = ref<DepartmentDTO[]>([]);

const doctors = ref<DoctorDTO[]>([]);
const availableDoctors = ref<DoctorDTO[]>([]);

const registrationForm = ref<RegistrationForm>({
  department: null,
  doctorId: null,
  appointmentTime: null,
  notes: '',
  patientName: auth.userInfo?.name || '',
  patientPhone: auth.userInfo?.phone || ''
});

const selectedDate = ref<Date | null>(null);
const availableTimeSlots = ref<TimeSlotDTO[]>([]);
const appointments = ref<AppointmentDTO[]>([]);
const loading = ref(false);

const formatDate = (date: Date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

// 监听科室变化，加载对应科室的医生
watch(() => registrationForm.value.department, async (newDepartment) => {
  if (typeof newDepartment === 'number' && !Number.isNaN(newDepartment)) {
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
    availableTimeSlots.value = [];
  }
});

// 监听医生变化，清空时间段
watch(() => registrationForm.value.doctorId, () => {
  availableTimeSlots.value = [];
  registrationForm.value.appointmentTime = null;
});

// 加载可用时间段
const loadAvailableTimeSlots = async () => {
  if (!selectedDate.value || !registrationForm.value.doctorId) {
    availableTimeSlots.value = [];
    return;
  }

  try {
    loading.value = true;
    const dateStr = formatDate(selectedDate.value);
    const response = await scheduleApi.getAvailableTimeSlots(
      registrationForm.value.doctorId,
      dateStr
    );
    
    if (response.code === 200 && response.data) {
      availableTimeSlots.value = response.data;
    } else {
      ElMessage.error('加载可用时间段失败: ' + (response.message || '未知错误'));
      availableTimeSlots.value = [];
    }
  } catch (error) {
    console.error('加载可用时间段异常:', error);
    ElMessage.error('加载可用时间段失败');
    availableTimeSlots.value = [];
  } finally {
    loading.value = false;
  }
};

// 格式化时间段显示
const formatTimeSlot = (slot: TimeSlotDTO) => {
  const start = new Date(slot.startTime);
  const end = new Date(slot.endTime);
  return `${start.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })} - ${end.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`;
};

// 计算属性
const canRegister = computed(() => {
  return registrationForm.value.department && 
         registrationForm.value.doctorId && 
         registrationForm.value.appointmentTime &&
         selectedDate.value &&
         registrationForm.value.patientName &&
         registrationForm.value.patientPhone;
});

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
      appointmentTime: registrationForm.value.appointmentTime as string,
      notes: registrationForm.value.notes,
      patientName: registrationForm.value.patientName,
      patientPhone: registrationForm.value.patientPhone
    };

    const response = await appointmentApi.createAppointment(request);
    
    if (response.code === 200) {
      ElMessage.success('挂号成功！');
      
      // 重新加载预约列表与可用时间段
      await loadAppointments();
      await loadAvailableTimeSlots();
      
      // 重置表单
      registrationForm.value = {
        department: null,
        doctorId: null,
        appointmentTime: null,
        notes: '',
        patientName: auth.userInfo?.name || '',
        patientPhone: auth.userInfo?.phone || ''
      };
      selectedDate.value = null;
      availableTimeSlots.value = [];
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

const loadDepartments = async () => {
  try {
    loading.value = true;
    const response = await departmentApi.getAll();
    if (response.code === 200) {
      departments.value = response.data || [];
    } else {
      ElMessage.error('加载科室失败: ' + (response.message || '未知错误'));
    }
  } catch (error) {
    console.error('加载科室异常:', error);
    const errorMessage = error instanceof Error ? error.message : '网络错误';
    ElMessage.error('加载科室失败: ' + errorMessage);
  } finally {
    loading.value = false;
  }
};

const handleLogout = () => {
  auth.logout();
  router.push('/login');
  ElMessage.success('已退出登录');
};

// 初始化加载数据
onMounted(async () => {
  await Promise.all([loadDepartments(), loadAppointments(), loadAllDoctors()]);
});
</script>

<style scoped>
.patient-dashboard {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f0fe 100%);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  padding: 24px 32px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.08);
}

.header h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 600;
  color: #303133;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 15px;
  color: #606266;
  font-weight: 500;
}

.main-content {
  display: grid;
  gap: 24px;
}

.registration-section,
.appointments-section {
  background: white;
  padding: 28px;
  border-radius: 12px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s ease;
}

.registration-section:hover,
.appointments-section:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.registration-section h2,
.appointments-section h2 {
  color: #303133;
  margin: 0 0 24px 0;
  font-size: 20px;
  font-weight: 600;
  border-bottom: 3px solid #67c23a;
  padding-bottom: 12px;
  display: inline-block;
}

.registration-card {
  max-width: 650px;
  border: none;
  box-shadow: none;
  padding: 0;
}

.notes-text {
  cursor: pointer;
  color: #606266;
  font-size: 12px;
}

.notes-text:hover {
  color: #409eff;
}

:deep(.time-slot-option.time-slot-working) {
  background-color: #f0f9eb;
}

:deep(.time-slot-option.time-slot-working span) {
  font-weight: 600;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
  font-size: 14px;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

:deep(.el-table) {
  margin-top: 16px;
  border-radius: 8px;
  overflow: hidden;
  font-size: 14px;
}

:deep(.el-table__header) {
  background-color: #f5f7fa;
}

:deep(.el-table th) {
  background-color: #f5f7fa !important;
  color: #606266;
  font-weight: 600;
  font-size: 14px;
  padding: 14px 0;
}

:deep(.el-table td) {
  padding: 14px 0;
  font-size: 14px;
}

:deep(.el-table__row:hover) {
  background-color: #f0f9ff;
}

:deep(.el-button) {
  border-radius: 6px;
  font-weight: 500;
  padding: 10px 20px;
  font-size: 14px;
  transition: all 0.3s ease;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  border: none;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #85ce61, #67c23a);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(103, 194, 58, 0.3);
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #f56c6c, #f78989);
  border: none;
}

:deep(.el-button--danger:hover) {
  background: linear-gradient(135deg, #f78989, #f56c6c);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(245, 108, 108, 0.3);
}

:deep(.el-button--small) {
  padding: 7px 14px;
  font-size: 13px;
}

:deep(.el-select),
:deep(.el-date-editor),
:deep(.el-input) {
  width: 100%;
}

:deep(.el-select .el-input__wrapper),
:deep(.el-input__wrapper),
:deep(.el-date-editor .el-input__wrapper) {
  border-radius: 6px;
  font-size: 14px;
}

:deep(.el-tag) {
  border-radius: 12px;
  font-weight: 500;
  padding: 4px 12px;
  font-size: 13px;
}

:deep(.el-card) {
  border-radius: 8px;
}

.loading-overlay {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

@media (max-width: 768px) {
  .patient-dashboard {
    padding: 12px;
  }
  
  .header {
    flex-direction: column;
    gap: 16px;
    padding: 20px;
  }
  
  .registration-section,
  .appointments-section {
    padding: 20px;
  }
  
  .registration-card {
    max-width: 100%;
  }
  
  :deep(.el-table) {
    font-size: 13px;
  }
  
  :deep(.el-table th),
  :deep(.el-table td) {
    padding: 10px 0;
  }
}
</style>