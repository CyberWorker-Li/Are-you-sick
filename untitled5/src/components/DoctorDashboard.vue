<template>
  <div class="doctor-dashboard">
    <div class="header">
      <h1>医生工作台</h1>
      <div class="user-info">
        <span>{{ userGreeting }}</span>
        <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="main-tabs">
      <!-- 我的排班 -->
      <el-tab-pane label="我的排班" name="schedules">
        <div class="section">
          <h2>本周排班</h2>
          <el-table :data="schedules" v-loading="loading" style="width: 100%">
            <el-table-column prop="dayOfWeek" label="星期" width="120" />
            <el-table-column prop="startTime" label="开始时间" width="150" />
            <el-table-column prop="endTime" label="结束时间" width="150" />
            <el-table-column prop="maxPatients" label="最大人数" width="100" />
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button size="small" @click="viewQueue(scope.row)">查看队列</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 就诊队列 -->
      <el-tab-pane label="就诊队列" name="queue">
        <div class="section">
          <div class="section-header">
            <h2>就诊队列</h2>
            <el-date-picker
              v-model="selectedQueueTime"
              type="datetime"
              placeholder="选择时间段"
              @change="handleQueueTimeChange"
            />
          </div>
          <el-table :data="queue" v-loading="loading" style="width: 100%">
            <el-table-column prop="queuePosition" label="排队位置" width="100">
              <template #default="scope">
                <el-tag>前面还有 {{ scope.row.queuePosition }} 人</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="patientName" label="患者姓名" width="120" />
            <el-table-column prop="patientPhone" label="手机号" width="150" />
            <el-table-column prop="appointmentTime" label="预约时间" width="180">
              <template #default="scope">
                {{ new Date(scope.row.appointmentTime).toLocaleString() }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button 
                  size="small" 
                  type="primary" 
                  @click="sendReminder(scope.row)"
                >发送提醒</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 排班调整申请 -->
      <el-tab-pane label="排班调整" name="adjustment">
        <div class="section">
          <div class="section-header">
            <h2>我的排班调整申请</h2>
            <el-button type="primary" @click="showAdjustmentDialog = true">申请调整</el-button>
          </div>
          <el-table :data="myAdjustmentRequests" v-loading="loading" style="width: 100%">
            <el-table-column prop="dayOfWeek" label="星期" width="120" />
            <el-table-column prop="startTime" label="开始时间" width="150" />
            <el-table-column prop="endTime" label="结束时间" width="150" />
            <el-table-column prop="reason" label="申请原因" width="200" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="adminResponse" label="管理员回复" width="200" />
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 排班调整申请对话框 -->
    <el-dialog v-model="showAdjustmentDialog" title="申请排班调整" width="600px">
      <el-form :model="adjustmentForm" label-width="100px">
        <el-form-item label="星期" required>
          <el-select v-model="adjustmentForm.dayOfWeek" placeholder="请选择星期">
            <el-option label="星期一" value="MONDAY" />
            <el-option label="星期二" value="TUESDAY" />
            <el-option label="星期三" value="WEDNESDAY" />
            <el-option label="星期四" value="THURSDAY" />
            <el-option label="星期五" value="FRIDAY" />
            <el-option label="星期六" value="SATURDAY" />
            <el-option label="星期日" value="SUNDAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-time-picker v-model="adjustmentForm.startTime" format="HH:mm" />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-time-picker v-model="adjustmentForm.endTime" format="HH:mm" />
        </el-form-item>
        <el-form-item label="申请原因">
          <el-input v-model="adjustmentForm.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdjustmentDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAdjustment">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../stores/auth';
import doctorDashboardApi from '../api/doctorDashboard';

const router = useRouter();
const auth = useAuthStore();
const userGreeting = computed(() => auth.greetingText);
const doctorId = ref(auth.userInfo?.id || 0);
const activeTab = ref('schedules');
const loading = ref(false);

const schedules = ref<any[]>([]);
const queue = ref<any[]>([]);
const myAdjustmentRequests = ref<any[]>([]);
const selectedQueueTime = ref<Date | null>(null);

const showAdjustmentDialog = ref(false);
const adjustmentForm = ref({
  dayOfWeek: '',
  startTime: null,
  endTime: null,
  reason: ''
});

const loadSchedules = async () => {
  try {
    loading.value = true;
    const response = await doctorDashboardApi.getMySchedules(doctorId.value);
    if (response.code === 200) {
      schedules.value = response.data || [];
    }
  } catch (error) {
    ElMessage.error('加载排班失败');
  } finally {
    loading.value = false;
  }
};

const loadQueue = async (options: { autoSelect?: boolean } = {}) => {
  const shouldAutoSelect = options.autoSelect || !selectedQueueTime.value;

  try {
    loading.value = true;
    const timeStr = !shouldAutoSelect && selectedQueueTime.value
      ? selectedQueueTime.value.toISOString()
      : undefined;
    const response = await doctorDashboardApi.getAppointmentQueue(doctorId.value, timeStr);
    if (response.code === 200) {
      queue.value = response.data || [];
      if (queue.value.length > 0) {
        const firstTime = queue.value[0].appointmentTime;
        if (firstTime) {
          const firstDate = new Date(firstTime);
          if (shouldAutoSelect || !selectedQueueTime.value) {
            selectedQueueTime.value = firstDate;
          }
        }
      } else if (shouldAutoSelect) {
        selectedQueueTime.value = null;
      }
    }
  } catch (error) {
    ElMessage.error('加载队列失败');
  } finally {
    loading.value = false;
  }
};

const handleQueueTimeChange = () => {
  loadQueue();
};

const loadAdjustmentRequests = async () => {
  try {
    loading.value = true;
    const response = await doctorDashboardApi.getMyAdjustmentRequests(doctorId.value);
    if (response.code === 200) {
      myAdjustmentRequests.value = response.data || [];
    }
  } catch (error) {
    ElMessage.error('加载申请列表失败');
  } finally {
    loading.value = false;
  }
};

const sendReminder = async (appointment: any) => {
  try {
    await doctorDashboardApi.sendAppointmentReminder(appointment.appointmentId);
    ElMessage.success('提醒发送成功');
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '发送失败');
  }
};

const submitAdjustment = async () => {
  try {
    const startTimeStr = adjustmentForm.value.startTime?.toTimeString().slice(0, 5) || '';
    const endTimeStr = adjustmentForm.value.endTime?.toTimeString().slice(0, 5) || '';
    
    await doctorDashboardApi.requestScheduleAdjustment(
      doctorId.value,
      adjustmentForm.value.dayOfWeek,
      startTimeStr,
      endTimeStr,
      adjustmentForm.value.reason
    );
    ElMessage.success('申请提交成功');
    showAdjustmentDialog.value = false;
    adjustmentForm.value = {
      dayOfWeek: '',
      startTime: null,
      endTime: null,
      reason: ''
    };
    await loadAdjustmentRequests();
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '提交失败');
  }
};

const dayOfWeekMap: Record<string, number> = {
  SUNDAY: 0,
  MONDAY: 1,
  TUESDAY: 2,
  WEDNESDAY: 3,
  THURSDAY: 4,
  FRIDAY: 5,
  SATURDAY: 6
};

const computeNextOccurrenceDate = (schedule: any) => {
  if (!schedule?.dayOfWeek) return null;
  const targetDay = dayOfWeekMap[schedule.dayOfWeek];
  if (targetDay === undefined) return null;

  const now = new Date();
  const currentDay = now.getDay();
  let diff = targetDay - currentDay;

  const scheduleTime = schedule?.startTime || '';
  const [hourStr = '0', minuteStr = '0'] = scheduleTime.split(':');
  const scheduleHours = parseInt(hourStr, 10) || 0;
  const scheduleMinutes = parseInt(minuteStr, 10) || 0;

  if (
    diff < 0 ||
    (diff === 0 &&
      (now.getHours() > scheduleHours ||
        (now.getHours() === scheduleHours && now.getMinutes() >= scheduleMinutes)))
  ) {
    diff += 7;
  }

  const targetDate = new Date(now);
  targetDate.setDate(now.getDate() + diff);
  targetDate.setHours(scheduleHours, scheduleMinutes, 0, 0);
  return targetDate;
};

const viewQueue = (schedule: any) => {
  activeTab.value = 'queue';
  const targetDate = computeNextOccurrenceDate(schedule);
  if (targetDate) {
    selectedQueueTime.value = targetDate;
    loadQueue({ autoSelect: false });
  } else {
    selectedQueueTime.value = null;
    loadQueue({ autoSelect: true });
  }
};

const getStatusType = (status: string) => {
  const map: { [key: string]: string } = {
    'PENDING': 'warning',
    'CONFIRMED': 'success',
    'COMPLETED': 'info',
    'CANCELLED': 'danger',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  };
  return map[status] || 'info';
};

const getStatusText = (status: string) => {
  const map: { [key: string]: string } = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'APPROVED': '已批准',
    'REJECTED': '已拒绝'
  };
  return map[status] || status;
};

const handleLogout = () => {
  auth.logout();
  router.push('/login');
  ElMessage.success('已退出登录');
};

onMounted(async () => {
  await loadSchedules();
  await loadAdjustmentRequests();
});
</script>

<style scoped>
.doctor-dashboard {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f0fe 100%);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
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

.section {
  background: white;
  padding: 28px;
  border-radius: 12px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
}

.section h2 {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  padding-bottom: 12px;
  border-bottom: 2px solid #e4e7ed;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  margin: 0;
  border-bottom: none;
  padding-bottom: 0;
}

.main-tabs {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.08);
}

.main-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.main-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  padding: 0 24px;
  height: 48px;
  line-height: 48px;
}

.main-tabs :deep(.el-tabs__item.is-active) {
  color: #409eff;
  font-weight: 600;
}

.main-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  background: linear-gradient(90deg, #409eff, #66b1ff);
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
  font-size: 14px;
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
  padding: 9px 18px;
  font-size: 14px;
  transition: all 0.3s ease;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  border: none;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #66b1ff, #409eff);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
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

:deep(.el-tag) {
  border-radius: 12px;
  font-weight: 500;
  padding: 4px 12px;
  font-size: 13px;
}

:deep(.el-date-editor) {
  border-radius: 6px;
}

:deep(.el-dialog) {
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

:deep(.el-dialog__header) {
  padding: 24px 24px 16px;
  border-bottom: 1px solid #e4e7ed;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
  font-size: 14px;
}

:deep(.el-select),
:deep(.el-input) {
  font-size: 14px;
}

:deep(.el-select .el-input__wrapper),
:deep(.el-input__wrapper) {
  border-radius: 6px;
}

@media (max-width: 768px) {
  .doctor-dashboard {
    padding: 12px;
  }

  .header {
    flex-direction: column;
    gap: 16px;
    padding: 20px;
  }

  .main-tabs {
    padding: 16px;
  }

  .section {
    padding: 20px;
  }
}
</style>

